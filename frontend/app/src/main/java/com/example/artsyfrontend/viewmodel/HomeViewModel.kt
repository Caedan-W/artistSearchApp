// file: app/src/main/java/com/example/artsyfrontend/viewmodel/HomeViewModel.kt
package com.example.artsyfrontend.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artsyfrontend.data.model.FavouriteItem
import com.example.artsyfrontend.data.model.User
import com.example.artsyfrontend.repository.ArtistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Home Screen 的 ViewModel.
 * 负责：
 * 1) App 启动时恢复会话并校验 (/me)
 * 2) 监听用户登录/登出状态，自动加载或清空收藏列表
 * 3) 提供刷新、登出、删除账户等操作
 */
class HomeViewModel : ViewModel() {

    // --- 当前登录的用户信息 (直接引用仓库中的 StateFlow) ---
    val currentUser: StateFlow<User?> = ArtistRepository.currentUser

    // --- 收藏列表状态 (私有 _favorites, 对外 favorites) ---
    private val _favorites = MutableStateFlow<List<FavouriteItem>>(emptyList())
    val favorites: StateFlow<List<FavouriteItem>> = _favorites

    // --- 标记 App 启动时是否在进行会话恢复与校验 ---
    private val _isLoadingInitialData = MutableStateFlow(true)
    val isLoadingInitialData: StateFlow<Boolean> = _isLoadingInitialData

    // --- 标记收藏列表是否正在加载 ---
    private val _isLoadingFavorites = MutableStateFlow(false)
    val isLoadingFavorites: StateFlow<Boolean> = _isLoadingFavorites

    // --- 用于一次性 Snackbar 提示消息 ---
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage

    init {
        Log.d("HomeViewModel", "初始化 ViewModel — 开始恢复会话并校验")

        // --- 修改：App 启动时先从 DataStore 恢复 token 并校验会话 (/me) ---
        viewModelScope.launch(Dispatchers.IO) {
            val user = ArtistRepository.restoreAndCheck()  // ← 新增：一键恢复 & 校验
            _isLoadingInitialData.value = false           // ← 新增：关闭启动加载状态
            Log.d("HomeViewModel", "恢复 & 校验完成, user=${user?.email}")
        }

        // 观察用户状态变化，登录时加载收藏，登出时清空
        observeUserAndLoadFavorites()
    }

    /**
     * 监听仓库中的 currentUser:
     * - user != null 时，加载收藏列表
     * - user == null 时，清空收藏列表
     */
    private fun observeUserAndLoadFavorites() {
        viewModelScope.launch {
            ArtistRepository.currentUser.collect { user ->
                Log.d("HomeViewModel", "observeUser: user=$user")
                if (user != null) {
                    // 登录态：拉取收藏
                    if (!_isLoadingFavorites.value) {
                        _isLoadingFavorites.value = true
                        viewModelScope.launch(Dispatchers.IO) {
                            try {
                                val favs = ArtistRepository.fetchFavorites()
                                _favorites.value = favs
                                Log.d("HomeViewModel", "获取到 ${favs.size} 条收藏")
                            } catch (e: Exception) {
                                Log.e("HomeViewModel", "获取收藏失败", e)
                                _favorites.value = emptyList()
                            } finally {
                                _isLoadingFavorites.value = false
                            }
                        }
                    }
                } else {
                    // 未登录态：清空收藏
                    _favorites.value = emptyList()
                    _isLoadingFavorites.value = false
                    Log.d("HomeViewModel", "用户未登录，已清空收藏")
                }
            }
        }
    }

    /**
     * 主动刷新收藏列表（例如详情页变动后调用）。
     */
    fun refreshFavorites() {
        if (currentUser.value != null && !_isLoadingFavorites.value) {
            Log.d("HomeViewModel", "手动刷新收藏列表")
            viewModelScope.launch(Dispatchers.IO) {
                _isLoadingFavorites.value = true
                try {
                    val favs = ArtistRepository.fetchFavorites()
                    _favorites.value = favs
                    Log.d("HomeViewModel", "刷新成功，共 ${favs.size} 条")
                } catch (e: Exception) {
                    Log.e("HomeViewModel", "刷新收藏失败", e)
                } finally {
                    _isLoadingFavorites.value = false
                }
            }
        }
    }

    /**
     * 处理用户登出操作，清除会话并给出提示。
     */
    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            ArtistRepository.logoutUser()               // 清本地 & Cookie
            _snackbarMessage.emit("Logged out successfully")
            Log.d("HomeViewModel", "用户已登出")
        }
    }

    /**
     * 处理用户删除账户操作，成功后给出提示。
     */
    fun deleteAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            val success = ArtistRepository.deleteUserAccount()
            if (success) {
                _snackbarMessage.emit("Account deleted successfully")
                Log.d("HomeViewModel", "账户删除成功")
            } else {
                _snackbarMessage.emit("Failed to delete account")
                Log.d("HomeViewModel", "账户删除失败")
            }
        }
    }

    /**
     * 登录成功后，UI 层可调用此方法显示 Snackbar。
     */
    fun showLoginSuccessSnackbar() {
        viewModelScope.launch {
            _snackbarMessage.emit("Logged in successfully")
            Log.d("HomeViewModel", "显示登录成功提示")
        }
    }
}
