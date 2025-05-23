// file: app/src/main/java/com/example/artsyfrontend/repository/ArtistRepository.kt
package com.example.artsyfrontend.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import com.example.artsyfrontend.data.datastore.UserPreferenceKeys
import com.example.artsyfrontend.data.datastore.userDataStore
import com.example.artsyfrontend.data.model.User
import com.example.artsyfrontend.data.model.LoginRequest
import com.example.artsyfrontend.data.model.LoginResponse
import com.example.artsyfrontend.data.model.RegisterRequest
import com.example.artsyfrontend.data.model.RegisterResponse
import com.example.artsyfrontend.data.model.SearchResponse
import com.example.artsyfrontend.data.model.SearchArtist
import com.example.artsyfrontend.data.model.FavoritesResponse
import com.example.artsyfrontend.data.model.AddFavoriteRequest
import com.example.artsyfrontend.data.model.FavouriteItem
import com.example.artsyfrontend.data.model.RemoveFavoriteResponse
import com.example.artsyfrontend.data.model.ArtistDetail
import com.example.artsyfrontend.data.model.ArtworksResponse
import com.example.artsyfrontend.data.model.Artwork
import com.example.artsyfrontend.data.model.CategoriesResponse
import com.example.artsyfrontend.data.model.Category
import com.example.artsyfrontend.data.model.SimilarArtistsResponse
import com.example.artsyfrontend.data.model.SimilarArtist
import com.example.artsyfrontend.data.network.AuthState
import com.example.artsyfrontend.data.network.RetrofitInstance
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Global singleton repository for user session management,
 * favorites, and artist data fetching.
 */
object ArtistRepository {

    // ----- Initialization -----

    private lateinit var appContext: Context
    private var isInitialized = false

    /**
     * Must be called once in Application.onCreate(context).
     * Initializes DataStore and context for persistence.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        isInitialized = true
        Log.i("ArtistRepository", "Initialized with application context.")
    }

    // ----- DataStore & Persistence -----

    /** Lazily obtain DataStore<Preferences> after initialization. */
    private val dataStore: DataStore<Preferences> by lazy {
        if (!isInitialized) throw IllegalStateException("ArtistRepository not initialized.")
        appContext.userDataStore
    }

    /**
     * Save or clear persisted user info and JWT token.
     * @param user     non-null to save user; null to clear all.
     * @param rawToken non-null to save token; null to clear.
     */
    private suspend fun saveUserToLocal(user: User?, rawToken: String?) {
        dataStore.edit { prefs ->
            if (user != null && !rawToken.isNullOrBlank()) {
                prefs[UserPreferenceKeys.USER_ID]       = user.id.orEmpty()
                prefs[UserPreferenceKeys.USER_EMAIL]    = user.email.orEmpty()
                prefs[UserPreferenceKeys.USER_FULLNAME] = user.fullname.orEmpty()
                prefs[UserPreferenceKeys.USER_IMAGE_URL]= user.profileImageUrl.orEmpty()
                prefs[UserPreferenceKeys.AUTH_TOKEN]    = rawToken
                AuthState.token = rawToken
                Log.i("ArtistRepository", "Saved user & token for ${user.email}")
            } else {
                prefs.remove(UserPreferenceKeys.USER_ID)
                prefs.remove(UserPreferenceKeys.USER_EMAIL)
                prefs.remove(UserPreferenceKeys.USER_FULLNAME)
                prefs.remove(UserPreferenceKeys.USER_IMAGE_URL)
                prefs.remove(UserPreferenceKeys.AUTH_TOKEN)
                AuthState.token = null
                Log.i("ArtistRepository", "Cleared user session & token.")
            }
        }
    }

    /**
     * Read persisted token from DataStore and cache in AuthState.
     * @return the token, or null if absent.
     */
    private suspend fun restoreTokenFromLocal(): String? {
        val prefs = dataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences()) else throw e
            }
            .first()
        val token = prefs[UserPreferenceKeys.AUTH_TOKEN]
        AuthState.token = token
        Log.i("ArtistRepository", "Restored token: $token")
        return token
    }

    // ----- In-memory user state -----

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Retrofit service
    private val api = RetrofitInstance.api

    // ----- Session Management -----

    /**
     * Attempt to restore saved token and validate session via /me endpoint.
     * @return User if successful, null otherwise.
     */
    suspend fun restoreAndCheck(): User? {
        val token = restoreTokenFromLocal()
        return if (!token.isNullOrBlank()) checkSessionAndFetchUser() else null
    }

    /**
     * Call backend /me to fetch current user and refresh persistence.
     * Clears session on 401.
     */
    suspend fun checkSessionAndFetchUser(): User? {
        return try {
            val user = api.getCurrentUser()
            _currentUser.value = user
            saveUserToLocal(user, AuthState.token)
            Log.i("ArtistRepository", "/me success for ${user.email}")
            user
        } catch (e: Exception) {
            if (e is HttpException && e.code() == 401) {
                saveUserToLocal(null, null)
                Log.w("ArtistRepository", "Session expired (401)")
            } else {
                Log.e("ArtistRepository", "Error on /me", e)
            }
            _currentUser.value = null
            null
        }
    }

    /**
     * Perform login, persist user & token on success.
     */
    suspend fun login(request: LoginRequest): LoginResponse {
        val resp = api.login(request)
        _currentUser.value = resp.user
        saveUserToLocal(resp.user, resp.token)
        Log.i("ArtistRepository", "Login success for ${resp.user.email}")
        return resp
    }

    /**
     * Perform registration, persist user & token on success.
     */
    suspend fun register(request: RegisterRequest): RegisterResponse {
        val resp = api.register(request)
        _currentUser.value = resp.user
        saveUserToLocal(resp.user, resp.token)
        Log.i("ArtistRepository", "Registration success for ${resp.user?.email}")
        return resp
    }

    /**
     * Logout locally by clearing session data.
     */
    suspend fun logoutUser() {
        _currentUser.value = null
        saveUserToLocal(null, null)
        Log.i("ArtistRepository", "User logged out")
    }

    /**
     * Delete account via backend, then clear local session.
     * @return true if deletion successful.
     */
    suspend fun deleteUserAccount(): Boolean {
        return try {
            val resp: Response<Unit> = api.deleteAccount()
            if (resp.isSuccessful) {
                logoutUser()
                Log.i("ArtistRepository", "Account deleted successfully")
                true
            } else {
                Log.w("ArtistRepository", "Account deletion failed: ${resp.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("ArtistRepository", "Error deleting account", e)
            false
        }
    }

    // ----- Data Fetching Methods -----

    /** Search artists by query string. */
    suspend fun searchArtists(query: String): List<SearchArtist> {
        return api.searchArtists(query).artists
    }

    /** Fetch current user's favorites list. */
    suspend fun fetchFavorites(): List<FavouriteItem> {
        return api.getFavorites().favorites
    }

    /** Fetch detailed info for a specific artist. */
    suspend fun getArtistDetails(artistId: String): ArtistDetail {
        return api.getArtistDetails(artistId)
    }

    /** Add a favorite artist. */
    suspend fun addFavorite(request: AddFavoriteRequest): FavouriteItem {
        return api.addFavorite(request)
    }

    /** Remove a favorite artist by ID. */
    suspend fun removeFavorite(artistId: String): RemoveFavoriteResponse {
        return api.removeFavorite(artistId)
    }

    /** Get artworks for a specific artist. */
    suspend fun getArtworks(artistId: String): List<Artwork> {
        return api.getArtworks(artistId).artworks
    }

    /** Get categories for a specific artwork. */
    suspend fun getArtworkCategories(artworkId: String): List<Category> {
        return api.getArtworkCategories(artworkId).categories
    }

    /** Get artists similar to the specified artist. */
    suspend fun getSimilarArtists(artistId: String): List<SimilarArtist> {
        return api.getSimilarArtists(artistId).artists
    }
}
