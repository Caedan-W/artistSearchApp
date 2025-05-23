package com.example.artsyfrontend.ui.screen

import android.util.Log // 日志记录
// Compose 布局和基础组件导入
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField // 用于自定义输入框
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
// Material 3 组件导入
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 返回箭头 (自动适应 RTL)
import androidx.compose.material.icons.filled.Close // 清除图标
import androidx.compose.material.icons.filled.Search // 搜索图标
// Compose 运行时和状态管理导入
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester // 用于请求焦点
import androidx.compose.ui.focus.focusRequester // Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor // 光标颜色
import androidx.compose.ui.platform.LocalSoftwareKeyboardController // 控制键盘显示/隐藏
import androidx.compose.ui.text.TextStyle // 文本样式
// 导入 TextFieldValue 用于光标持久化
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.ImeAction // 键盘操作类型 (Search)
import androidx.compose.ui.tooling.preview.Preview // 预览
import androidx.compose.ui.unit.dp // 尺寸单位
// ViewModel 和 Navigation 导入
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
// 自定义组件和 ViewModel 导入
import com.example.artsyfrontend.ui.component.SearchResultItem // 搜索结果列表项组件
import com.example.artsyfrontend.viewmodel.SearchViewModel // 对应的 ViewModel
// Coroutine 导入
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class) // 使用实验性的 Material3 API
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: SearchViewModel = viewModel() // 获取 ViewModel 实例
) {

    // --- 状态收集 ---
    // 从 ViewModel 收集状态，确保 UI 与 ViewModel 同步
    val queryValue by viewModel.searchQuery.collectAsState() // 收集 TextFieldValue (包含文本和光标)
    val searchResults by viewModel.searchResults.collectAsState() // 搜索结果列表
    val isLoggedIn by viewModel.isLoggedIn.collectAsState() // 用户登录状态
    val favoriteIds by viewModel.favoriteIds.collectAsState() // 收藏的艺术家 ID 集合

    // --- 本地 UI 状态和控制器 ---
    val keyboardController = LocalSoftwareKeyboardController.current // 用于控制键盘
    val focusRequester = remember { FocusRequester() } // 用于请求输入框焦点
    val snackbarHostState = remember { SnackbarHostState() } // 用于显示 Snackbar
    val scope = rememberCoroutineScope() // 用于启动协程 (例如显示 Snackbar)

    // --- 副作用 (LaunchedEffect) ---

    // 请求初始焦点，当屏幕首次组合时让输入框自动获得焦点
    LaunchedEffect(Unit) {
        delay(100) // 短暂延迟以确保 UI 准备就绪
        try { // 添加 try-catch 以防焦点请求失败
            focusRequester.requestFocus()
        } catch (e: Exception){
            Log.e("SearchScreen", "Requesting focus failed", e)
        }
    }

    // 收集来自 ViewModel 的 Snackbar 消息并显示
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.currentSnackbarData?.dismiss() // 如果有正在显示的，先隐藏
            snackbarHostState.showSnackbar(message) // 显示新消息
        }
    }

    // --- UI 布局 ---
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }, // 定义 Snackbar 的容器
        topBar = {
            // 顶部应用栏
            TopAppBar(
                // 左侧导航图标 (返回按钮)
                navigationIcon = {
                    IconButton(onClick = {
                        // 点击返回时，检查收藏状态是否有修改
                        if (viewModel.getFavoritesModifiedStatus()) {
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("favorites_updated", true) // 通知 HomeScreen 刷新
                            Log.d("SearchScreen", "Favorites modified, setting result for HomeScreen.")
                        } else {
                            Log.d("SearchScreen", "Favorites not modified, navigating back.")
                        }
                        navController.navigateUp() // 执行返回操作
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // 使用自动镜像的返回图标
                            contentDescription = "Back" // 英文内容描述
                        )
                    }
                },
                // 中间标题区域（包含搜索框）
                title = {
                    Row( // 使用 Row 排列图标和输入框
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 搜索图标
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null, // 装饰性图标，无需描述
                            tint = LocalContentColor.current // 使用 TopAppBar 的默认内容颜色
                        )
                        Spacer(Modifier.width(8.dp)) // 图标和输入框间距

                        // 输入框容器
                        Box(modifier = Modifier.weight(1f)) {
                            // 自定义文本输入框
                            BasicTextField(
                                value = queryValue, // 绑定从 ViewModel 收集的 TextFieldValue
                                onValueChange = { newValue ->
                                    // 当用户输入时，直接通知 ViewModel 更新完整的 TextFieldValue
                                    viewModel.onQueryChanged(newValue)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(focusRequester), // 应用焦点请求器
                                singleLine = true, // 单行输入
                                textStyle = TextStyle( // 设置输入文本的样式
                                    color = LocalContentColor.current, // 使用 TopAppBar 的内容颜色
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize // 字体大小
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary), // 光标颜色
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // 设置键盘动作为 "搜索"
                                keyboardActions = KeyboardActions( // 处理键盘动作
                                    onSearch = {
                                        // 用户点击键盘上的搜索键时，主要隐藏键盘（搜索由 debounce 处理）
                                        keyboardController?.hide() // 隐藏软键盘
                                    }
                                ),
                                decorationBox = { innerTextField -> // 自定义输入框外观，用于实现 Placeholder
                                    Box(contentAlignment = Alignment.CenterStart) {
                                        // 当输入框文本为空时显示 Placeholder
                                        if (queryValue.text.isEmpty()) {
                                            Text(
                                                text = "Search artists...", // 英文 Placeholder
                                                style = MaterialTheme.typography.bodyLarge, // 样式与输入文本匹配
                                                color = LocalContentColor.current.copy(alpha = 0.6f) // 颜色稍暗淡
                                            )
                                        }
                                        innerTextField() // 实际的输入字段渲染位置
                                    }
                                }
                            )
                        } // 输入框容器 Box 结束

                        // 清除按钮 (仅当输入框文本非空时显示)
                        if (queryValue.text.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp)) // 与输入框的间距
                            IconButton(onClick = {
                                // 点击清除按钮时:
                                // 1. 通知 ViewModel 清空文本和光标状态
                                viewModel.onQueryChanged(TextFieldValue("")) // 传递空的 TextFieldValue
                                // 2. 导航回上一个屏幕 (HomeScreen)
                                navController.navigateUp()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close, // 清除图标
                                    contentDescription = "Clear search and navigate back", // 英文内容描述
                                    tint = LocalContentColor.current // 使用 TopAppBar 的内容颜色
                                )
                            }
                        }
                    } // 标题区域 Row 结束
                },
                // TopAppBar 颜色设置
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer, // 输入框文本/图标会继承此颜色
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues -> // Scaffold 内容区域
        // 使用 LazyColumn 显示搜索结果列表
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues) // 应用 Scaffold 提供的内边距，避免内容与 TopAppBar 重叠
                .fillMaxSize(), // 占据所有可用空间
            // 设置列表项之间的垂直间距
            verticalArrangement = Arrangement.spacedBy(8.dp),
            // 设置列表内容的内边距
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
        ) {
            // 1. 空状态或无结果状态显示
            //    检查查询文本长度和搜索结果是否为空
            if (queryValue.text.length >= 3 && searchResults.isEmpty()) {
                item { // 在 LazyColumn 中，每个独立元素都需要放在 item {} 里
                    Box( // 使用 Box 将提示文本居中
                        modifier = Modifier
                            .fillParentMaxWidth() // 宽度填充 LazyColumn
                            .padding(top = 50.dp), // 与上方留出距离
                        contentAlignment = Alignment.Center // 内容居中
                    ) {
                        Text( // 显示无结果提示
                            // 使用 queryValue.text 获取当前查询文本
                            text = "No Result Found", // 英文提示
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant // 使用次要颜色
                        )
                    }
                }
            } else { // 2. 显示搜索结果列表
                items(
                    items = searchResults, // 数据源
                    key = { artist -> artist.id ?: artist.name ?: artist.hashCode() } // 提供稳定唯一的 Key
                ) { artist ->
                    // 计算当前艺术家是否在收藏夹中
                    val isFavorite = favoriteIds.contains(artist.id)
                    // 调用 SearchResultItem Composable 显示每个艺术家
                    SearchResultItem(
                        artist = artist,
                        navController = navController,
                        isLoggedIn = isLoggedIn, // 传递登录状态
                        isFavorite = isFavorite, // 传递当前收藏状态
                        onToggleFavorite = { // 传递切换收藏的回调 lambda
                            // 在回调中调用 ViewModel 的方法
                            if (artist.id != null) {
                                viewModel.toggleFavorite(artist, isFavorite)
                            } else {
                                // 如果 ID 为空，显示错误提示
                                scope.launch { snackbarHostState.showSnackbar("Error: Cannot favorite artist with missing ID.")}
                            }
                        }
                    )
                }
            }
        } // LazyColumn 结束
    } // Scaffold 结束
} // SearchScreen Composable 结束

// --- Preview ---
@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    // Preview 通常无法完全模拟 ViewModel 和 Navigation，但可以检查基本布局
    val navController = rememberNavController()
    // ArtsyFrontendTheme { // 如果有自定义主题，应用它
    SearchScreen(navController = navController)
    // }
}