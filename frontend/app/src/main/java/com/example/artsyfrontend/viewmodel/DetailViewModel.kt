// file: app/src/main/java/com/example/artsyfrontend/viewmodel/DetailViewModel.kt
package com.example.artsyfrontend.viewmodel

// 导入 Compose 图标库
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.vector.ImageVector
// 导入 ViewModel 和生命周期相关库
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
// 导入数据模型和仓库
import com.example.artsyfrontend.data.model.*
import com.example.artsyfrontend.repository.ArtistRepository
// 导入协程库
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// 导入日志库
import android.util.Log
// 导入网络异常库
import retrofit2.HttpException
// 导入通用异常库
import java.lang.Exception

// 定义页面加载状态的枚举
enum class DetailScreenState { LOADING, SUCCESS, ERROR }

// 定义 Tab 显示信息的数据类
data class TabInfo(val title: String, val icon: ImageVector)

/**
 * DetailScreen 的 ViewModel.
 * 负责处理艺术家详情页的业务逻辑和状态管理。
 */
class DetailViewModel(
    // SavedStateHandle 用于从导航参数中安全地获取 artistId
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 从导航参数中获取当前页面的艺术家 ID，如果获取不到则抛出异常
    private val artistId: String = checkNotNull(savedStateHandle["artistId"]) {
        "Artist ID not found in navigation arguments."
    }

    // --- 内部可变状态流 (MutableStateFlow) ---
    // 这些状态由 ViewModel 内部管理和修改

    private val _screenState = MutableStateFlow(DetailScreenState.LOADING) // 页面整体加载状态
    private val _artistDetail = MutableStateFlow<ArtistDetail?>(null) // 艺术家详细信息
    private val _selectedTabIndex = MutableStateFlow(0) // 当前选中的 Tab 索引
    private val _isFavorite = MutableStateFlow(false) // 当前艺术家是否已被收藏
    private val _snackbarMessage = MutableSharedFlow<String>() // 用于发送 Snackbar 消息的通道
    private val _artworksLoading = MutableStateFlow(false) // "Artworks" Tab 是否正在加载
    private val _artworks = MutableStateFlow<List<Artwork>>(emptyList()) // "Artworks" Tab 的数据列表
    private val _artworksErrorMessage = MutableStateFlow<String?>(null) // "Artworks" Tab 的错误消息
    private var artworksLoaded = false // 标记 "Artworks" Tab 是否已加载过数据
    private val _showCategoryDialog = MutableStateFlow(false) // 是否显示分类对话框
    private val _selectedArtworkIdForCategories = MutableStateFlow<String?>(null) // 当前查看分类的作品 ID
    private val _categoriesLoading = MutableStateFlow(false) // 分类对话框是否正在加载
    private val _categories = MutableStateFlow<List<Category>>(emptyList()) // 分类对话框的数据列表
    private val _categoriesErrorMessage = MutableStateFlow<String?>(null) // 分类对话框的错误消息
    private val _similarArtistsLoading = MutableStateFlow(false) // "Similar" Tab 是否正在加载
    private val _similarArtists = MutableStateFlow<List<SimilarArtist>>(emptyList()) // "Similar" Tab 的数据列表
    private val _similarArtistsErrorMessage = MutableStateFlow<String?>(null) // "Similar" Tab 的错误消息
    private var similarArtistsLoaded = false // 标记 "Similar" Tab 是否已加载过数据
    private val _favorites = MutableStateFlow<List<FavouriteItem>>(emptyList()) // 存储完整的收藏列表（主要用于检查相似艺术家的收藏状态）

    // --- MODIFICATION START (Refresh on Return - Step 1a) ---
    /**
     * 标记收藏状态在此 ViewModel 实例的生命周期内是否被成功修改过。
     * 用于决定返回上一个屏幕 (HomeScreen) 时是否需要通知其刷新列表。
     * 初始化为 false。
     */
    private var favoritesSuccessfullyModified = false
    // --- MODIFICATION END ---

    // --- 对外暴露的只读状态流 (StateFlow / SharedFlow) ---
    // UI 通过观察这些 Flow 来获取状态并自动更新

    val screenState: StateFlow<DetailScreenState> = _screenState.asStateFlow()
    val artistDetail: StateFlow<ArtistDetail?> = _artistDetail.asStateFlow()
    val selectedTabIndex: StateFlow<Int> = _selectedTabIndex.asStateFlow()
    // 直接暴露来自 Repository 的全局用户状态
    val currentUser: StateFlow<User?> = ArtistRepository.currentUser
    // 从全局用户状态衍生出布尔型的登录状态
    val isLoggedIn: StateFlow<Boolean> = ArtistRepository.currentUser
        .map { it != null } // 如果 currentUser 不是 null，则表示已登录
        .stateIn( // 将衍生的 Flow 转换为 StateFlow
            scope = viewModelScope, // 使用 ViewModel 的生命周期作用域
            started = SharingStarted.WhileSubscribed(5000), // 订阅者停止后 5 秒停止共享
            initialValue = false // 初始假设未登录
        )
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow() // 当前艺术家是否收藏的状态
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow() // Snackbar 消息通道
    val artworksLoading: StateFlow<Boolean> = _artworksLoading.asStateFlow()
    val artworks: StateFlow<List<Artwork>> = _artworks.asStateFlow()
    val artworksErrorMessage: StateFlow<String?> = _artworksErrorMessage.asStateFlow()
    val showCategoryDialog: StateFlow<Boolean> = _showCategoryDialog.asStateFlow()
    val categoriesLoading: StateFlow<Boolean> = _categoriesLoading.asStateFlow()
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    val categoriesErrorMessage: StateFlow<String?> = _categoriesErrorMessage.asStateFlow()
    val similarArtistsLoading: StateFlow<Boolean> = _similarArtistsLoading.asStateFlow()
    val similarArtists: StateFlow<List<SimilarArtist>> = _similarArtists.asStateFlow()
    val similarArtistsErrorMessage: StateFlow<String?> = _similarArtistsErrorMessage.asStateFlow()
    val favorites: StateFlow<List<FavouriteItem>> = _favorites.asStateFlow() // 完整的收藏列表状态

    // --- Tab 定义 ---
    // 所有可能的 Tab
    private val allTabsInfo = listOf(
        TabInfo("Details", Icons.Filled.Info),
        TabInfo("Artworks", Icons.Filled.List),
        TabInfo("Similar", Icons.Filled.People)
    )
    // 根据登录状态动态决定可见的 Tab
    val visibleTabs: StateFlow<List<TabInfo>> = isLoggedIn
        .map { loggedIn -> if (loggedIn) allTabsInfo else allTabsInfo.take(2) } // 登录时显示所有，否则只显示前两个
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = allTabsInfo.take(2) // 初始显示前两个
        )

    // --- 初始化代码块 ---
    /**
     * ViewModel 创建时执行的初始化逻辑。
     */
    init {
        Log.d("DetailViewModel", "ViewModel 初始化，艺术家 ID: $artistId")
        // 1. 获取艺术家详情是首要任务
        fetchArtistDetails()
        // 2. 启动协程观察全局用户登录状态的变化
        viewModelScope.launch {
            ArtistRepository.currentUser.collect { user -> // 收集用户状态流
                Log.d("DetailViewModel", "收集到 currentUser 变化: ${user?.email}")
                if (user != null) {
                    // 如果用户已登录，则获取完整的收藏列表，并更新当前艺术家的收藏状态 (_isFavorite)
                    fetchFavoritesAndUpdateStatus()
                } else {
                    // 如果用户未登录或已登出，重置收藏状态和可能依赖登录状态的其他数据
                    _isFavorite.value = false
                    Log.d("DetailViewModel", "用户已登出，设置 isFavorite 为 false。")
                    similarArtistsLoaded = false // 重置 Similar Artists Tab 的加载标记
                    _similarArtists.value = emptyList() // 清空 Similar Artists 列表
                }
            }
        }
    }

    // --- 私有数据获取/处理函数 ---

    /**
     * 从仓库获取当前艺术家的详细信息。
     * 更新 _artistDetail 和 _screenState。
     */
    private fun fetchArtistDetails() {
        _screenState.value = DetailScreenState.LOADING
        Log.d("DetailViewModel", "[FetchDetails] 设置状态为 LOADING")
        viewModelScope.launch { // 启动协程执行网络请求
            Log.d("DetailViewModel", "[FetchDetails] 协程已启动")
            try {
                Log.d("DetailViewModel", "[FetchDetails] 调用仓库获取 ID: $artistId 的详情...")
                val detail = ArtistRepository.getArtistDetails(artistId) // 调用仓库方法
                Log.d("DetailViewModel", "[FetchDetails] 仓库调用成功。详情: ${detail?.name}")
                _artistDetail.value = detail // 更新艺术家详情状态
                _screenState.value = DetailScreenState.SUCCESS // 更新页面状态为成功
                Log.d("DetailViewModel", "[FetchDetails] 状态更新为 SUCCESS。")
                // 详情获取成功后，不再需要在这里单独检查登录状态，由 currentUser 的 collect 逻辑处理
            } catch (e: Exception) {
                Log.e("DetailViewModel", "[FetchDetails] 捕获到异常", e)
                _screenState.value = DetailScreenState.ERROR // 更新页面状态为错误
                Log.d("DetailViewModel", "[FetchDetails] 状态更新为 ERROR。")
            }
        }
    }

    /**
     * 当用户登录状态确认后，获取完整的收藏列表，并据此更新当前艺术家的 _isFavorite 状态。
     * 同时更新 _favorites 状态（用于检查相似艺术家的收藏状态）。
     */
    private fun fetchFavoritesAndUpdateStatus() {
        // 在 IO 线程执行网络请求
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d("DetailViewModel", "[fetchFavAndUpdate] 检查艺术家 $artistId 的收藏状态，开始获取完整收藏列表...")
                val favoritesList = ArtistRepository.fetchFavorites() // 获取完整列表
                _favorites.value = favoritesList // 更新 ViewModel 内的完整列表状态
                // 检查当前艺术家 ID 是否在列表中（使用 FavouriteItem.id，因为它映射自 artistId）
                val isFav = favoritesList.any { it.id == artistId }
                _isFavorite.value = isFav // 更新当前艺术家是否被收藏的状态
                Log.d("DetailViewModel", "[fetchFavAndUpdate] 艺术家 $artistId 的收藏状态: $isFav")
            } catch (favError: Exception) {
                // 处理获取收藏列表时可能发生的错误
                Log.e("DetailViewModel", "[fetchFavAndUpdate] 获取收藏列表失败", favError)
                _isFavorite.value = false // 获取失败时，假定未收藏
            }
        }
    }

    /**
     * 获取当前艺术家的作品列表。
     * 包含防止重复加载的逻辑。
     */
    private fun fetchArtworks() {
        if (artworksLoaded || _artworksLoading.value) {
            Log.d("DetailViewModel", "[FetchArtworks] 跳过获取。loaded=$artworksLoaded, loading=${_artworksLoading.value}")
            return
        }
        _artworksLoading.value = true
        _artworksErrorMessage.value = null
        Log.d("DetailViewModel", "[FetchArtworks] 设置 artworksLoading=true，启动协程...")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val artworksList = ArtistRepository.getArtworks(artistId)
                _artworks.value = artworksList
                artworksLoaded = true // 标记已加载
                Log.d("DetailViewModel", "[FetchArtworks] 成功。获取到 ${artworksList.size} 个作品。")
            } catch (e: Exception) {
                Log.e("DetailViewModel", "[FetchArtworks] 获取作品失败", e)
                _artworksErrorMessage.value = "Failed to load artworks."
            } finally {
                _artworksLoading.value = false
                Log.d("DetailViewModel", "[FetchArtworks] 设置 artworksLoading=false。")
            }
        }
    }

    /**
     * 根据作品 ID 获取其分类列表。
     */
    private fun fetchCategories(artworkId: String) {
        if (_categoriesLoading.value) return
        _categoriesLoading.value = true
        _categoriesErrorMessage.value = null
        _categories.value = emptyList() // 清空旧数据
        Log.d("DetailViewModel", "[FetchCategories] 设置 categoriesLoading=true，作品 ID: $artworkId")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val categoryList = ArtistRepository.getArtworkCategories(artworkId)
                _categories.value = categoryList
                Log.d("DetailViewModel", "[FetchCategories] 成功。获取到 ${categoryList.size} 个分类。")
            } catch (e: Exception) {
                Log.e("DetailViewModel", "[FetchCategories] 获取分类失败，作品 ID $artworkId", e)
                _categoriesErrorMessage.value = "Failed to load categories."
            } finally {
                _categoriesLoading.value = false
                Log.d("DetailViewModel", "[FetchCategories] 设置 categoriesLoading=false。")
            }
        }
    }

    /**
     * 获取与当前艺术家相似的艺术家列表。
     * 包含防止重复加载和检查登录状态的逻辑。
     */
    private fun fetchSimilarArtists() {
        // 检查是否已加载、正在加载或未登录
        if (similarArtistsLoaded || _similarArtistsLoading.value || ArtistRepository.currentUser.value == null) {
            Log.d("DetailViewModel", "[FetchSimilar] 跳过获取。loaded=$similarArtistsLoaded, loading=${_similarArtistsLoading.value}, loggedIn=${ArtistRepository.currentUser.value != null}")
            return
        }
        _similarArtistsLoading.value = true
        _similarArtistsErrorMessage.value = null
        Log.d("DetailViewModel", "[FetchSimilar] 设置 similarArtistsLoading=true，启动协程...")
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val similarList = ArtistRepository.getSimilarArtists(artistId)
                _similarArtists.value = similarList
                similarArtistsLoaded = true // 标记已加载
                Log.d("DetailViewModel", "[FetchSimilar] 成功。获取到 ${similarList.size} 个相似艺术家。")
            } catch (e: Exception) {
                Log.e("DetailViewModel", "[FetchSimilar] 获取相似艺术家失败", e)
                _similarArtistsErrorMessage.value = "Failed to load similar artists."
            } finally {
                _similarArtistsLoading.value = false
                Log.d("DetailViewModel", "[FetchSimilar] 设置 similarArtistsLoading=false。")
            }
        }
    }


    // --- 公开方法 (供 UI 调用) ---

    /**
     * 切换当前显示艺术家的收藏状态。
     * 包括乐观 UI 更新、调用后端 API、处理 API 结果（成功则标记修改，失败则回滚 UI）。
     */
    fun toggleFavorite() {
        // 必须登录才能操作
        if (ArtistRepository.currentUser.value == null) {
            Log.w("DetailViewModel", "ToggleFavorite 忽略：用户未登录。")
            viewModelScope.launch { _snackbarMessage.emit("Please log in first") }
            return
        }
        // 必须有艺术家信息才能操作
        val currentArtist = _artistDetail.value
        if (currentArtist?.id == null || currentArtist.name == null) {
            Log.e("DetailViewModel", "ToggleFavorite 忽略：艺术家详情（ID 或名称）为空。")
            viewModelScope.launch { _snackbarMessage.emit("Error: Artist details not available") }
            return
        }

        // 获取当前状态并计算目标状态
        val currentFavStatus = _isFavorite.value
        val newFavStatus = !currentFavStatus

        // 1. 乐观更新 UI (立即改变心形图标状态)
        _isFavorite.value = newFavStatus
        Log.d("DetailViewModel", "[ToggleFavorite] 乐观地将艺术家 ${currentArtist.id} 的收藏状态设置为 $newFavStatus")

        // 2. 在 IO 线程执行后台 API 调用
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (newFavStatus) {
                    // --- 添加到收藏 ---
                    Log.d("DetailViewModel", "[ToggleFavorite] 调用 addFavorite API...")
                    val request = AddFavoriteRequest( // 构建请求体
                        artistId = currentArtist.id,
                        artistName = currentArtist.name,
                        artistImage = currentArtist.imageUrl,
                        nationality = currentArtist.nationality,
                        birthday = currentArtist.birthday,
                        deathday = currentArtist.deathday
                    )
                    ArtistRepository.addFavorite(request) // 调用仓库方法
                    Log.i("DetailViewModel", "[ToggleFavorite] API addFavorite 成功。")
                    _snackbarMessage.emit("Added to favorites") // 发送成功提示

                } else {
                    // --- 从收藏移除 ---
                    Log.d("DetailViewModel", "[ToggleFavorite] 调用 removeFavorite API...")
                    ArtistRepository.removeFavorite(currentArtist.id) // 调用仓库方法
                    Log.i("DetailViewModel", "[ToggleFavorite] API removeFavorite 成功。")
                    _snackbarMessage.emit("Removed from favorites") // 发送成功提示
                }

                // --- MODIFICATION START (Refresh on Return - Step 1b) ---
                // *** API 调用成功后，设置修改标志位 ***
                favoritesSuccessfullyModified = true
                Log.d("DetailViewModel", "[ToggleFavorite] API 调用成功，设置 favoritesSuccessfullyModified = true")
                // --- MODIFICATION END ---

            } catch (e: Exception) { // 处理 API 调用过程中可能发生的任何异常
                Log.e("DetailViewModel", "[ToggleFavorite] API 调用失败", e)
                // 3. API 调用失败，需要回滚 UI 状态到操作前的状态
                _isFavorite.value = currentFavStatus
                Log.d("DetailViewModel", "[ToggleFavorite] API 调用失败，回滚 isFavorite 状态到 $currentFavStatus")
                // 发送错误提示
                _snackbarMessage.emit("Error: Could not update favorites")

                // --- MODIFICATION START (Refresh on Return - Step 1c) ---
                // *** API 调用失败后，重置修改标志位 ***
                favoritesSuccessfullyModified = false
                Log.d("DetailViewModel", "[ToggleFavorite] API 调用失败，重置 favoritesSuccessfullyModified = false")
                // --- MODIFICATION END ---
            }
        }
    }

    // --- MODIFICATION START (Refresh on Return - Step 1d) ---
    /**
     * 返回收藏状态在此 ViewModel 生命周期内是否被成功修改过。
     * 供 DetailScreen 在导航返回前调用，以决定是否需要通知 HomeScreen 刷新。
     * @return Boolean true 如果被修改过，false 如果没有。
     */
    fun getFavoritesModifiedStatus(): Boolean {
        Log.d("DetailViewModel", "getFavoritesModifiedStatus() called, returning: $favoritesSuccessfullyModified")
        return favoritesSuccessfullyModified
    }
    // --- MODIFICATION END ---

    /**
     * 切换“相似艺术家”列表项的收藏状态。
     * 注意：这个操作的 UI 回滚比较复杂，因为我们不为每个相似艺术家维护独立的 StateFlow。
     */
    fun toggleSimilarArtistFavorite(artist: SimilarArtist, currentIsFavorite: Boolean) {
        if (ArtistRepository.currentUser.value == null) { /* ... (登录检查) ... */ return }
        if (artist.id == null || artist.name == null) { /* ... (数据检查) ... */ return }

        val newFavStatus = !currentIsFavorite
        Log.d("DetailViewModel", "[ToggleSimilar] 尝试设置艺术家 ${artist.id} (${artist.name}) 的收藏状态为 $newFavStatus")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (newFavStatus) {
                    // 添加收藏 (注意 AddFavoriteRequest 可能字段不全)
                    Log.d("DetailViewModel", "[ToggleSimilar] 调用 addFavorite API...")
                    val request = AddFavoriteRequest(artist.id, artist.name, artist.image, null, null, null)
                    ArtistRepository.addFavorite(request)
                    Log.i("DetailViewModel", "[ToggleSimilar] API addFavorite 成功。")
                    _snackbarMessage.emit("Added '${artist.name}' to favorites")
                    // *** 注意：这里也应该设置 favoritesSuccessfullyModified = true ***
                    // favoritesSuccessfullyModified = true // 如果希望这个操作也触发刷新

                } else {
                    // 移除收藏
                    Log.d("DetailViewModel", "[ToggleSimilar] 调用 removeFavorite API...")
                    ArtistRepository.removeFavorite(artist.id)
                    Log.i("DetailViewModel", "[ToggleSimilar] API removeFavorite 成功。")
                    _snackbarMessage.emit("Removed '${artist.name}' from favorites")
                    // *** 注意：这里也应该设置 favoritesSuccessfullyModified = true ***
                    // favoritesSuccessfullyModified = true // 如果希望这个操作也触发刷新
                }
                // --- MODIFICATION START: 在 API 成功后设置标志位 ---
                // 无论添加还是移除成功，都标记收藏已被修改，以便返回时刷新 HomeScreen
                favoritesSuccessfullyModified = true
                Log.d("DetailViewModel", "[ToggleSimilar] Marked favoritesSuccessfullyModified = true")
                // --- MODIFICATION END ---
                // 成功后可以考虑调用 fetchFavoritesAndUpdateStatus() 来刷新 _favorites 列表，
                // 这样 Similar Artists Tab 中其他项的状态也能（可能）更新。
                // fetchFavoritesAndUpdateStatus()
            } catch (e: Exception) {
                Log.e("DetailViewModel", "[ToggleSimilar] API 调用失败，艺术家 ID ${artist.id}", e)
                _snackbarMessage.emit("Error: Could not update favorites for '${artist.name}'")
                // --- MODIFICATION START: 失败时重置标志位 (可选但推荐) ---
                // 如果 API 调用失败，不应认为收藏被成功修改
                favoritesSuccessfullyModified = false
                // --- MODIFICATION END ---
            }
        }
    }

    /**
     * 处理 Tab 切换逻辑，并在需要时触发数据加载。
     */
    fun changeTab(index: Int) {
        // 检查索引是否有效且与当前不同
        if (index in visibleTabs.value.indices && _selectedTabIndex.value != index) {
            _selectedTabIndex.value = index // 更新选中的 Tab 索引
            Log.d("DetailViewModel", "Tab 切换到索引: $index")

            // 如果切换到 "Artworks" Tab (索引 1) 且数据未加载过，则加载
            if (index == 1 && !artworksLoaded) {
                fetchArtworks()
            }

            // 如果切换到 "Similar" Tab (检查标题) 且数据未加载过并且用户已登录，则加载
            val selectedTabInfo = visibleTabs.value.getOrNull(index)
            if (selectedTabInfo?.title == "Similar" && !similarArtistsLoaded && isLoggedIn.value) {
                fetchSimilarArtists()
            }
        }
    }

    /**
     * 请求显示指定作品的分类对话框。
     */
    fun showCategoriesDialog(artworkId: String) {
        _selectedArtworkIdForCategories.value = artworkId // 记录要显示分类的作品 ID
        _categories.value = emptyList() // 清空旧数据
        _categoriesErrorMessage.value = null // 清空旧错误
        _showCategoryDialog.value = true // 标记显示对话框
        Log.d("DetailViewModel", "显示作品 $artworkId 的分类对话框")
        fetchCategories(artworkId) // 开始加载分类数据
    }

    /**
     * 关闭分类对话框，并重置相关状态。
     */
    fun dismissCategoryDialog() {
        _showCategoryDialog.value = false // 标记关闭对话框
        _selectedArtworkIdForCategories.value = null // 清空记录的作品 ID
        _categoriesLoading.value = false // 重置加载状态
        _categoriesErrorMessage.value = null // 清空错误
        _categories.value = emptyList() // 清空数据
        Log.d("DetailViewModel", "分类对话框已关闭。")
    }

} // End DetailViewModel class