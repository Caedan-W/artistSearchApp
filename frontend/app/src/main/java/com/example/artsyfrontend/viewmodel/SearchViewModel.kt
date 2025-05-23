package com.example.artsyfrontend.viewmodel

import android.util.Log // 日志记录
import androidx.lifecycle.ViewModel // ViewModel 基类
import androidx.lifecycle.viewModelScope // ViewModel 协程作用域
// 导入 TextFieldValue 用于光标持久化
import androidx.compose.ui.text.input.TextFieldValue
// 数据模型导入
import com.example.artsyfrontend.data.model.AddFavoriteRequest // 添加收藏请求模型
import com.example.artsyfrontend.data.model.FavouriteItem // 收藏项模型 (fetchFavorites 需要)
import com.example.artsyfrontend.data.model.SearchArtist
import com.example.artsyfrontend.data.model.User // 用户模型
// 仓库导入
import com.example.artsyfrontend.repository.ArtistRepository
// Flow 和协程相关导入
import kotlinx.coroutines.Dispatchers // 调度器 (用于 IO 操作)
import kotlinx.coroutines.FlowPreview // 使用 debounce 需要
import kotlinx.coroutines.flow.* // Flow 操作符
import kotlinx.coroutines.launch // 启动协程

@OptIn(FlowPreview::class) // 启用 FlowPreview 功能 (debounce)
class SearchViewModel : ViewModel() {

    // --- 状态流定义 ---

    // 搜索结果列表 (内部可变，外部只读)
    private val _searchResults = MutableStateFlow<List<SearchArtist>>(emptyList())
    val searchResults: StateFlow<List<SearchArtist>> = _searchResults.asStateFlow()

    // 搜索查询状态 (使用 TextFieldValue 包含文本和光标信息)
    private val _query = MutableStateFlow(TextFieldValue("")) // 初始值为空的 TextFieldValue
    val searchQuery: StateFlow<TextFieldValue> = _query.asStateFlow() // 对外暴露只读 StateFlow

    // 登录状态 (直接从仓库衍生)
    val isLoggedIn: StateFlow<Boolean> = ArtistRepository.currentUser
        .map { it != null } // 用户非 null 即为登录
        .stateIn( // 转换为 StateFlow
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 5秒无活动则停止共享
            initialValue = ArtistRepository.currentUser.value != null // 初始检查
        )

    // 收藏的艺术家 ID 集合 (内部可变，外部只读)
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    // Snackbar 消息通道 (内部可变，外部只读)
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    // --- 内部状态标记 ---

    // 标记收藏在本 VM 生命周期内是否成功修改过
    private var favoritesSuccessfullyModified = false

    // --- 初始化块 ---
    init {
        Log.d("SearchViewModel", "ViewModel Initializing.")

        // 1. 设置搜索查询的 Flow 处理流水线
        _query
            .map { it.text } // 从 TextFieldValue 中提取文本字符串
            .debounce(500L) // 500ms 防抖
            .filter { queryText -> queryText.length >= 3 || queryText.isEmpty() } // 过滤短查询或空查询
            .distinctUntilChanged() // 避免重复处理相同文本
            .mapLatest { debouncedQuery -> // 处理最新查询文本，取消旧的API调用
                if (debouncedQuery.isEmpty()) {
                    Log.d("SearchViewModel", "Query text is empty, clearing search results.")
                    emptyList<SearchArtist>() // 返回空列表以清空UI
                } else {
                    Log.d("SearchViewModel", "Triggering debounced search for text: $debouncedQuery")
                    try {
                        ArtistRepository.searchArtists(debouncedQuery) // 调用仓库执行搜索
                    } catch (e: Exception) {
                        Log.e("SearchViewModel", "Search API call failed, query text: $debouncedQuery", e)
                        _snackbarMessage.emit("Search failed. Please try again later.") // 发送英文错误消息
                        emptyList<SearchArtist>() // 发生错误时返回空列表
                    }
                }
            }
            .onEach { results -> // 每次流水线产生结果时更新状态
                _searchResults.value = results
                Log.d("SearchViewModel", "Search results StateFlow updated, count: ${results.size}")
            }
            .launchIn(viewModelScope) // 在 ViewModel 的作用域内启动这个 Flow

        // 2. 启动协程观察用户登录状态，并据此获取收藏列表
        viewModelScope.launch {
            ArtistRepository.currentUser.collect { user ->
                Log.d("SearchViewModel", "Observed user status change: ${user?.email}")
                if (user != null) {
                    // 用户已登录，获取收藏 ID 列表
                    fetchFavorites()
                } else {
                    // 用户未登录或已登出，清空收藏 ID 并重置标记
                    _favoriteIds.value = emptySet()
                    favoritesSuccessfullyModified = false
                    Log.d("SearchViewModel", "User logged out/not logged in, cleared favorite status.")
                }
            }
        }
    }

    // --- 私有辅助函数 ---

    /**
     * 获取完整的收藏列表，并更新内部的 `_favoriteIds` StateFlow (仅包含 ID)。
     * 在 IO 线程执行。
     */
    private fun fetchFavorites() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("SearchViewModel", "Fetching favorites list to update ID set...")
                val favs: List<FavouriteItem> = ArtistRepository.fetchFavorites()
                // 从收藏项列表中提取所有非 null 的 ID，并转换为 Set
                _favoriteIds.value = favs.mapNotNull { it.id }.toSet()
                Log.d("SearchViewModel", "Favorite ID set updated. Count: ${_favoriteIds.value.size}")
            } catch (e: Exception) {
                Log.e("SearchViewModel", "Failed to fetch favorites to update IDs", e)
                _snackbarMessage.emit("Could not load favorite status.") // 英文提示
            }
        }
    }

    // --- 公开方法 (供 UI 调用) ---

    /**
     * 当 UI 层的 BasicTextField 值变化时调用此函数。
     * @param newValue 最新的 TextFieldValue 对象 (包含文本和光标/选区)。
     */
    fun onQueryChanged(newValue: TextFieldValue) {
        // 更新内部状态为新的 TextFieldValue 对象
        _query.value = newValue
        // Log.d("SearchViewModel", "Internal query TextFieldValue updated: ${newValue.text}, Selection: ${newValue.selection}") // 可选日志
    }

    /**
     * 切换搜索结果中某个艺术家的收藏状态。
     * @param artist 用户点击的 SearchArtist 对象。
     * @param currentIsFavorite 该艺术家当前是否已被收藏（基于 favoriteIds 判断）。
     */
    fun toggleFavorite(artist: SearchArtist, currentIsFavorite: Boolean) {
        // 1. 检查登录状态
        if (ArtistRepository.currentUser.value == null) {
            viewModelScope.launch { _snackbarMessage.emit("Please log in to manage favorites.") } // 英文
            Log.w("SearchViewModel", "Toggle favorite prevented: User not logged in.")
            return
        }
        // 2. 检查艺术家 ID 是否有效
        val artistId = artist.id
        if (artistId == null) {
            Log.e("SearchViewModel", "Toggle favorite failed: Artist ID is null. Artist name: ${artist.name}")
            viewModelScope.launch { _snackbarMessage.emit("Error: Cannot favorite artist with missing ID.") } // 英文
            return
        }

        // 3. 计算目标状态
        val newFavStatus = !currentIsFavorite

        // 4. 乐观 UI 更新: 立即更新 favoriteIds 集合
        _favoriteIds.update { currentIds ->
            if (newFavStatus) {
                currentIds + artistId // 添加 ID
            } else {
                currentIds - artistId // 移除 ID
            }
        }
        Log.d("SearchViewModel", "Optimistically updated favorite status (UI) for artist $artistId to $newFavStatus")

        // 5. 执行后台 API 调用 (在 IO 线程)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (newFavStatus) {
                    // --- 添加到收藏 ---
                    Log.d("SearchViewModel", "[API] Attempting to add favorite for artist $artistId...")
                    val request = AddFavoriteRequest(
                        artistId = artistId,
                        artistName = artist.name,
                        artistImage = artist.image,
                        nationality = null, // SearchArtist 无此信息
                        birthday = null,    // SearchArtist 无此信息
                        deathday = null     // SearchArtist 无此信息
                    )
                    ArtistRepository.addFavorite(request)
                    Log.i("SearchViewModel", "[API] Add artist $artistId to favorites successful.")
                    _snackbarMessage.emit("${artist.name} added to favorites.") // 英文

                } else {
                    // --- 从收藏移除 ---
                    Log.d("SearchViewModel", "[API] Attempting to remove favorite for artist $artistId...")
                    ArtistRepository.removeFavorite(artistId)
                    Log.i("SearchViewModel", "[API] Remove artist $artistId from favorites successful.")
                    _snackbarMessage.emit("${artist.name} removed from favorites.") // 英文
                }

                // 6a. API 调用成功: 设置修改标记
                favoritesSuccessfullyModified = true
                Log.d("SearchViewModel", "API call successful, setting favoritesSuccessfullyModified = true")

            } catch (e: Exception) { // 7. 处理 API 调用异常
                Log.e("SearchViewModel", "[API] Failed to toggle favorite status for artist $artistId", e)
                _snackbarMessage.emit("Error updating favorites for ${artist.name}.") // 英文

                // 7a. API 调用失败: 回滚乐观 UI 更新
                _favoriteIds.update { currentIds ->
                    if (newFavStatus) { // 如果是尝试添加失败，则从集合中移除 ID
                        currentIds - artistId
                    } else { // 如果是尝试移除失败，则将 ID 加回集合
                        currentIds + artistId
                    }
                }
                Log.d("SearchViewModel", "API call failed, rolled back optimistic UI update for artist $artistId.")

                // 7b. API 调用失败: 重置修改标记
                favoritesSuccessfullyModified = false
                Log.d("SearchViewModel", "API call failed, resetting favoritesSuccessfullyModified = false")
            }
        } // IO Coroutine 结束
    } // toggleFavorite 结束

    /**
     * 返回收藏状态在此 ViewModel 生命周期内是否被成功修改过。
     * 用于决定返回上一个屏幕 (HomeScreen) 时是否需要通知其刷新。
     * @return Boolean true 如果被修改过，false 如果没有。
     */
    fun getFavoritesModifiedStatus(): Boolean {
        Log.d("SearchViewModel", "getFavoritesModifiedStatus() called, returning: $favoritesSuccessfullyModified")
        return favoritesSuccessfullyModified
    }

} // SearchViewModel 类结束