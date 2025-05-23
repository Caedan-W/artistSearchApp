// file: app/src/main/java/com/example/artsyfrontend/ui/screen/DetailScreen.kt
package com.example.artsyfrontend.ui.screen

// 导入必要的库
import android.util.Log // 用于调试
import androidx.compose.foundation.ExperimentalFoundationApi // Pager 需要
import androidx.compose.foundation.background // 用于 Box 背景
import androidx.compose.foundation.layout.* // Box, Column, Row, Spacer, padding, size, fillMaxSize, fillMaxWidth, wrapContentHeight, height, heightIn
import androidx.compose.foundation.lazy.LazyColumn // Artworks/Similar 列表
import androidx.compose.foundation.lazy.items // Artworks/Similar 列表项
import androidx.compose.foundation.pager.HorizontalPager // 分类轮播
import androidx.compose.foundation.pager.rememberPagerState // 分类轮播状态
import androidx.compose.foundation.rememberScrollState // Details Tab 滚动
import androidx.compose.foundation.shape.RoundedCornerShape // Card 和 Box 圆角
import androidx.compose.foundation.verticalScroll // Details Tab 滚动
import androidx.compose.material.icons.Icons // Material 图标库
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 返回箭头图标 (自动根据左右布局调整方向)
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft // Pager 左箭头图标
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight // Pager 右箭头图标
import androidx.compose.material.icons.filled.Star // <<< 使用 Star (实心)
import androidx.compose.material.icons.outlined.StarBorder // <<< 使用 StarBorder (空心)
import androidx.compose.material3.AlertDialog // 分类 Dialog
import androidx.compose.material3.Button // Dialog 关闭按钮
import androidx.compose.material3.CircularProgressIndicator // 加载指示器
// import androidx.compose.material3.Divider // Details Tab 分隔线 (在 ArtistDetailsContent 内部)
import androidx.compose.material3.ExperimentalMaterial3Api // 实验性 Material3 API (TopAppBar, Tab, TabRow 等)
import androidx.compose.material3.Icon // 图标组件
import androidx.compose.material3.IconButton // 图标按钮组件
import androidx.compose.material3.LocalContentColor // 获取当前内容颜色
import androidx.compose.material3.MaterialTheme // 主题访问 (颜色, 字体等)
import androidx.compose.material3.Scaffold // Material Design 布局骨架
import androidx.compose.material3.SnackbarDuration // Snackbar 显示时长
import androidx.compose.material3.SnackbarHost // Snackbar 容器
import androidx.compose.material3.SnackbarHostState // Snackbar 状态管理
import androidx.compose.material3.Tab // Tab 组件
import androidx.compose.material3.TabRow // Tab 容器
import androidx.compose.material3.Text // 文本组件
// import androidx.compose.material3.TextButton // Dialog 关闭按钮（旧版，现在用 Button）
import androidx.compose.material3.TopAppBar // 顶部应用栏
import androidx.compose.material3.TopAppBarDefaults // 顶部应用栏默认颜色等
import androidx.compose.runtime.* // Composable, State, remember, LaunchedEffect, collectAsState, getValue, etc.
import androidx.compose.ui.Alignment // 对齐方式
import androidx.compose.ui.Modifier // Modifier 用于修饰 Composable
import androidx.compose.ui.draw.clip // 用于裁剪形状 (圆角)
import androidx.compose.ui.text.font.FontWeight // 字体粗细
import androidx.compose.ui.text.style.TextAlign // 文本对齐
import androidx.compose.ui.text.style.TextOverflow // 文本溢出处理
import androidx.compose.ui.unit.dp // 尺寸单位 dp
import androidx.lifecycle.viewmodel.compose.viewModel // 获取 ViewModel 实例
import androidx.navigation.NavHostController // 导航控制器
// 导入 ViewModel 和数据模型
import com.example.artsyfrontend.viewmodel.DetailScreenState
import com.example.artsyfrontend.viewmodel.DetailViewModel
import com.example.artsyfrontend.data.model.ArtistDetail
import com.example.artsyfrontend.data.model.Artwork
import com.example.artsyfrontend.data.model.Category
import com.example.artsyfrontend.data.model.FavouriteItem
import com.example.artsyfrontend.data.model.SimilarArtist
// 导入自定义 UI 组件
import com.example.artsyfrontend.ui.component.ArtworkCardItem
import com.example.artsyfrontend.ui.component.CategoryCardItem
import com.example.artsyfrontend.ui.component.SimilarArtistCardItem
// 导入协程相关库
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 艺术家详情页的 Composable 函数.
 *
 * @param navController 导航控制器.
 * @param viewModel DetailViewModel 实例.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // 启用实验性 API
@Composable
fun DetailScreen(
    navController: NavHostController,
    // 使用 compose-viewModel 库自动获取与此 Composable 关联的 DetailViewModel 实例
    // （它会自动处理 SavedStateHandle 的注入）
    viewModel: DetailViewModel = viewModel()
) {
    // --- 状态收集 ---
    // 从 ViewModel 收集需要在 UI 中观察的状态
    val screenState by viewModel.screenState.collectAsState() // 页面整体加载状态
    val artistDetail by viewModel.artistDetail.collectAsState() // 艺术家详情
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState() // 当前选中的 Tab
    val isLoggedIn by viewModel.isLoggedIn.collectAsState() // 用户是否登录
    val isFavorite by viewModel.isFavorite.collectAsState() // 当前艺术家是否被收藏
    val artworksLoading by viewModel.artworksLoading.collectAsState() // 作品列表是否加载中
    val artworks by viewModel.artworks.collectAsState() // 作品列表数据
    val artworksErrorMessage by viewModel.artworksErrorMessage.collectAsState() // 作品列表错误信息
    val showCategoryDialog by viewModel.showCategoryDialog.collectAsState() // 是否显示分类对话框
    val categoriesLoading by viewModel.categoriesLoading.collectAsState() // 分类是否加载中
    val categories by viewModel.categories.collectAsState() // 分类列表数据
    val categoriesErrorMessage by viewModel.categoriesErrorMessage.collectAsState() // 分类错误信息
    val visibleTabs by viewModel.visibleTabs.collectAsState() // 可见的 Tab 列表
    val similarArtistsLoading by viewModel.similarArtistsLoading.collectAsState() // 相似艺术家是否加载中
    val similarArtists by viewModel.similarArtists.collectAsState() // 相似艺术家列表数据
    val similarArtistsErrorMessage by viewModel.similarArtistsErrorMessage.collectAsState() // 相似艺术家错误信息
    val favorites by viewModel.favorites.collectAsState() // 完整的收藏列表（用于检查相似艺术家的状态）

    // --- Snackbar 设置 ---
    val snackbarHostState = remember { SnackbarHostState() } // 创建并记住 Snackbar 状态
    // 使用 LaunchedEffect 监听来自 ViewModel 的 Snackbar 消息
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            // 收到新消息时，先取消当前可能正在显示的 Snackbar (如果需要避免覆盖)
            snackbarHostState.currentSnackbarData?.dismiss()
            // 显示新的 Snackbar
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    // --- UI 布局：Scaffold ---
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // 将 SnackbarHost 关联到 Scaffold
        topBar = { // 配置顶部应用栏
            TopAppBar(
                // 标题显示艺术家名字，限制单行，超出部分显示省略号
                title = { Text(artistDetail?.name ?: "Artist Detail", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                // 导航图标（返回按钮）
                navigationIcon = {
                    IconButton(onClick = {
                        // --- MODIFICATION START (Refresh on Return - Step 2) ---
                        // 在执行向上导航之前，检查收藏状态是否在本页面被修改过
                        if (viewModel.getFavoritesModifiedStatus()) {
                            // 如果被修改过，就向上一个导航条目 (HomeScreen) 的 SavedStateHandle 中设置一个结果
                            // key 为 "favorites_updated"，值为 true
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("favorites_updated", true)
                            Log.d("DetailScreen", "收藏状态已修改，设置 favorites_updated=true 导航结果。")
                        } else {
                            Log.d("DetailScreen", "收藏状态未修改，直接返回。")
                        }
                        // 执行标准的向上导航操作
                        navController.navigateUp()
                        // --- MODIFICATION END ---
                    }) {
                        // 使用自动镜像的返回箭头图标
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // 右侧操作按钮 (收藏按钮)
                actions = {
                    // 仅当用户已登录时才显示收藏按钮
                    if (isLoggedIn) {
                        IconButton(onClick = { viewModel.toggleFavorite() }) { // 点击时调用 ViewModel 的切换收藏方法
                            Icon(
                                // 根据 isFavorite 状态显示实心或空心图标
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                                // (可选) 当已收藏时，使用主题的主色调强调
                                tint = if (isFavorite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                },
                // 设置 TopAppBar 颜色方案
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues -> // Scaffold 提供的内容区域的内边距

        // --- 主要内容区域：根据页面加载状态显示不同内容 ---
        when (screenState) {
            // 状态：加载中
            DetailScreenState.LOADING -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            // 状态：加载成功
            DetailScreenState.SUCCESS -> {
                // 使用 Column 垂直排列 TabRow 和 Tab 内容
                Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                    // TabRow 用于显示和管理 Tab
                    // selectedTabIndex 需要限制在可见 Tab 的有效索引范围内
                    TabRow(selectedTabIndex = selectedTabIndex.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0))) {
                        // 遍历可见的 Tab 信息，为每个信息创建一个 Tab
                        visibleTabs.forEachIndexed { index, tabInfo ->
                            Tab(
                                selected = selectedTabIndex == index, // 当前 Tab 是否被选中
                                onClick = { viewModel.changeTab(index) }, // 点击 Tab 时通知 ViewModel
                                text = { Text(tabInfo.title) }, // Tab 文本
                                icon = { Icon(tabInfo.icon, contentDescription = tabInfo.title) } // Tab 图标
                            )
                        }
                    } // End TabRow

                    // 根据选中的 Tab 索引显示不同的内容区域
                    // 同样需要限制索引范围
                    when (selectedTabIndex.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0))) {
                        // Tab 0: Details
                        0 -> ArtistDetailsContent(artistDetail)

                        // Tab 1: Artworks
                        1 -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) { // 内容顶部对齐
                                when {
                                    artworksLoading -> CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp)) // 显示加载指示器
                                    artworksErrorMessage != null -> Text(artworksErrorMessage ?: "Error", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error) // 显示错误信息
                                    artworks.isEmpty() -> // 列表为空时显示提示框
                                        Box(
                                            modifier = Modifier.fillMaxWidth(0.85f).padding(top = 24.dp).clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer).padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) { Text("No Artworks", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer) }
                                    else -> // 列表不为空时显示 LazyColumn
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.spacedBy(12.dp), // 项之间的垂直间距
                                            contentPadding = PaddingValues(vertical = 12.dp) // 列表的垂直内边距
                                        ) {
                                            items(artworks, key = { it.id }) { artwork -> // 使用 ArtworkCardItem 显示每一项
                                                ArtworkCardItem(
                                                    artwork = artwork,
                                                    onViewCategoriesClicked = { viewModel.showCategoriesDialog(it) } // 点击查看分类按钮的回调
                                                )
                                            }
                                        }
                                }
                            }
                        } // End case 1 (Artworks)

                        // Tab 2: Similar Artists
                        2 -> {
                            // 仅当用户登录时才显示此 Tab 内容
                            if (isLoggedIn) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    when {
                                        similarArtistsLoading -> CircularProgressIndicator() // 显示加载指示器
                                        similarArtistsErrorMessage != null -> Text(similarArtistsErrorMessage ?: "Error", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error) // 显示错误信息
                                        similarArtists.isEmpty() -> Text("No Similar Artists", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) // 列表为空提示
                                        else -> // 列表不为空时显示 LazyColumn
                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize(),
                                                contentPadding = PaddingValues(4.dp), // 列表内边距
                                                verticalArrangement = Arrangement.spacedBy(12.dp) // 项间距由 Card 内部处理
                                            ) {
                                                items(similarArtists, key = { it.id ?: it.name ?: "" }) { similarArtist ->
                                                    // 记住该相似艺术家的初始收藏状态 (依赖于完整的收藏列表 favorites)
                                                    val isInitiallyFavorite = remember(favorites, similarArtist.id) { favorites.any { it.id == similarArtist.id } }
                                                    // 使用 SimilarArtistCardItem 显示每一项
                                                    SimilarArtistCardItem(
                                                        artist = similarArtist,
                                                        navController = navController, // 传递导航控制器
                                                        isInitiallyFavorite = isInitiallyFavorite, // 传递初始收藏状态
                                                        onToggleFavorite = { viewModel.toggleSimilarArtistFavorite(similarArtist, it) } // 传递切换收藏的回调
                                                    )
                                                }
                                            }
                                    }
                                }
                            } // End if isLoggedIn
                        } // End case 2 (Similar)

                        // 其他可能的索引（理论上不应该出现）
                        else -> {}
                    } // End when (selectedTabIndex)
                } // End Column for SUCCESS state
            } // End SUCCESS case

            // 状态：加载失败
            DetailScreenState.ERROR -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text("Failed to load artist details.") // 显示错误提示
                }
            }
        } // End when (screenState)

        // --- 分类对话框 ---
        // 调用 CategoriesDialog Composable 来显示（如果 showCategoryDialog 为 true）
        CategoriesDialog(
            showDialog = showCategoryDialog,
            loading = categoriesLoading,
            categories = categories,
            errorMessage = categoriesErrorMessage,
            onDismiss = viewModel::dismissCategoryDialog // 传递关闭对话框的回调
        )

    } // End Scaffold
} // End DetailScreen Composable


/**
 * 用于显示 "Details" Tab 内容的 Composable.
 */
@Composable
fun ArtistDetailsContent(artistDetail: ArtistDetail?) {
    // 使用 Column 垂直排列，允许内容滚动，并添加内边距
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 使内容可垂直滚动
            .padding(16.dp)
    ) {
        // 如果详情为空，显示提示信息并返回
        if (artistDetail == null) {
            Text("Artist details not available.")
            return
        }
        // 显示艺术家姓名（居中，加粗）
        Text(artistDetail.name ?: "Unknown", style=MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp)) // 添加垂直间距

        // 构建并显示艺术家详情行（国籍，生卒年）
        val nationality = artistDetail.nationality?.takeIf{it.isNotBlank()}
        val birthday = artistDetail.birthday?.takeIf{it.isNotBlank()}
        val deathday = artistDetail.deathday?.takeIf{it.isNotBlank()}
        val lifespan = when {
            birthday != null && deathday != null -> "$birthday - $deathday"
            birthday != null -> "b. $birthday"
            deathday != null -> "d. $deathday"
            else -> null
        }
        val detailsLine = listOfNotNull(nationality, lifespan).joinToString(", ") // 将非空部分用逗号连接

        // 如果详情行不为空，则显示（居中）
        if (detailsLine.isNotBlank()) {
            Text(detailsLine, style=MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp)) // 添加与下方内容的间距
        } else {
            Spacer(modifier = Modifier.height(16.dp)) // 即使详情行为空，也保持一定的间距
        }

        // 显示艺术家简介（如果有）
        if (!artistDetail.biography.isNullOrBlank()) {
            Text(artistDetail.biography ?: "", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


/**
 * 用于显示作品分类的 AlertDialog 对话框 Composable.
 */
@OptIn(ExperimentalFoundationApi::class) // 需要 Pager
@Composable
fun CategoriesDialog(
    showDialog: Boolean,
    loading: Boolean,
    categories: List<Category>,
    errorMessage: String?,
    onDismiss: () -> Unit
) {
    // 仅当 showDialog 为 true 时才显示 AlertDialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss, // 点击对话框外部或返回键时的回调
            title = { Text("Categories") }, // 对话框标题
            text = { // 对话框内容区域
                Box( // 使用 Box 控制内容区域大小和对齐
                    modifier = Modifier
                        .fillMaxWidth() // 宽度占满对话框
                        .wrapContentHeight() // 高度自适应内容
                        .heightIn(min = 150.dp), // 限制最小高度，避免内容太少时过小
                    contentAlignment = Alignment.Center // 内容居中（用于加载指示器和错误/空状态文本）
                ) {
                    when {
                        loading -> CircularProgressIndicator() // 显示加载指示器
                        errorMessage != null -> Text(errorMessage, color = MaterialTheme.colorScheme.error) // 显示错误信息
                        categories.isEmpty() -> Text("No categories available") // 列表为空时显示提示
                        else -> { // 列表不为空时显示 Pager
                            val pagerState = rememberPagerState(pageCount = { categories.size }) // 创建并记住 Pager 状态
                            val coroutineScope = rememberCoroutineScope() // 获取协程作用域用于滚动 Pager
                            Box(modifier = Modifier.fillMaxWidth().wrapContentHeight()) { // 放置 Pager 和箭头的 Box
                                HorizontalPager( // 水平分页器
                                    state = pagerState,
                                    pageSpacing = 8.dp, // 页面间距
                                    contentPadding = PaddingValues(horizontal = 40.dp), // 左右内边距，为箭头留出空间
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight() // Pager 大小
                                ) { pageIndex ->
                                    // 为每一页渲染一个 CategoryCardItem
                                    CategoryCardItem(category = categories[pageIndex])
                                }
                                // 左箭头按钮
                                IconButton(
                                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage((pagerState.currentPage - 1).coerceAtLeast(0)) } }, // 点击向前滚动一页
                                    enabled = pagerState.canScrollBackward, // 只有可以向前滚动时才启用
                                    modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp) // 对齐到左侧
                                ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                                // 右箭头按钮
                                IconButton(
                                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage((pagerState.currentPage + 1).coerceAtMost(categories.size - 1)) } }, // 点击向后滚动一页
                                    enabled = pagerState.canScrollForward, // 只有可以向后滚动时才启用
                                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp) // 对齐到右侧
                                ) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)) }
                            }
                        }
                    }
                }
            },
            confirmButton = { // 对话框确认按钮（这里用作关闭按钮）
                Button(onClick = onDismiss) { // 点击时调用 onDismiss 回调
                    Text("Close")
                }
            }
        )
    }
}