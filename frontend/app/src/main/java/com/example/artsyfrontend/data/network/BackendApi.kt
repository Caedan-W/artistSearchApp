package com.example.artsyfrontend.data.network

// --- 导入所有需要的数据模型 ---
// 基本
import com.example.artsyfrontend.data.model.SearchResponse
import com.example.artsyfrontend.data.model.User
import com.example.artsyfrontend.data.model.FavoritesResponse
import com.example.artsyfrontend.data.model.AddFavoriteRequest
import com.example.artsyfrontend.data.model.FavouriteItem // AddFavorite 成功时返回
import com.example.artsyfrontend.data.model.RemoveFavoriteResponse
import com.example.artsyfrontend.data.model.ArtistDetail // (D1)
import com.example.artsyfrontend.data.model.ArtworksResponse // <<< (D11) 导入 ArtworksResponse
import com.example.artsyfrontend.data.model.CategoriesResponse
import com.example.artsyfrontend.data.model.LoginRequest
import com.example.artsyfrontend.data.model.LoginResponse
import com.example.artsyfrontend.data.model.RegisterRequest
import com.example.artsyfrontend.data.model.RegisterResponse
import com.example.artsyfrontend.data.model.SimilarArtistsResponse

// 后续任务可能需要的
// import com.example.artsyfrontend.data.model.CategoriesResponse // (D13)
// import com.example.artsyfrontend.data.model.SimilarArtistsResponse // (D16)

// --- 导入 Retrofit 注解 ---
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.Response

/**
 * 后端服务接口 —— 定义所有与后端交互的 API 端点
 */
interface BackendApi {

    // --- 搜索 ---
    // GET /api/search/:query
    @GET("search/{query}")
    suspend fun searchArtists(@Path("query") query: String): SearchResponse

    // --- 用户认证与信息 ---
    // GET /api/me
    @GET("me")
    suspend fun getCurrentUser(): User

    // --- 收藏夹 ---
    // GET /api/favorites
    @GET("favorites")
    suspend fun getFavorites(): FavoritesResponse

    // POST /api/favorites (D10)
    @POST("favorites")
    suspend fun addFavorite(@Body favoriteData: AddFavoriteRequest): FavouriteItem

    // DELETE /api/favorites/:artistId (D10)
    @DELETE("favorites/{artistId}")
    suspend fun removeFavorite(@Path("artistId") artistId: String): RemoveFavoriteResponse

    // --- 艺术家详情与相关信息 ---
    // GET /api/artist/:id (D1)
    @GET("artist/{id}")
    suspend fun getArtistDetails(@Path("id") artistId: String): ArtistDetail

    // GET /api/artist/:id/artworks
    @GET("artist/{id}/artworks")
    suspend fun getArtworks(@Path("id") artistId: String): ArtworksResponse // <<< 添加此方法

    // GET /api/artwork/:id/categories
    @GET("artwork/{id}/categories")
    suspend fun getArtworkCategories(@Path("id") artworkId: String): CategoriesResponse

    // GET /api/artist/:id/similar
    @GET("artist/{id}/similar")
    suspend fun getSimilarArtists(@Path("id") artistId: String): SimilarArtistsResponse

    // POST /api/auth/login
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): LoginResponse

    // POST /api/auth/register
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): RegisterResponse

    /**
     * 调用后端登出接口
     * POST /api/auth/logout
     * 后端通常返回成功消息或无内容 (2xx)，失败时返回错误 (4xx, 5xx)
     * 使用 Response<Unit> 可以处理无响应体但需要检查 HTTP 状态码的情况
     */
    @POST("auth/logout")
    suspend fun logout(): Response<Unit> // 返回 Response<Unit> 检查状态码

    /**
     * 调用后端删除账户接口
     * POST /api/auth/delete
     * 需要认证 (Cookie 会自动发送)
     * 同样使用 Response<Unit>
     */
    @POST("auth/delete")
    suspend fun deleteAccount(): Response<Unit> // 返回 Response<Unit> 检查状态码

}