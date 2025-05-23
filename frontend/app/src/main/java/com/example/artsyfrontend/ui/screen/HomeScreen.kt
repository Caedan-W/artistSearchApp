// file: app/src/main/java/com/example/artsyfrontend/ui/screen/HomeScreen.kt
package com.example.artsyfrontend.ui.screen

// 导入必要的 Compose 和 Android 相关库
import android.util.Log // 用于日志记录
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // 导入布局组件 (Box, Column, PaddingValues, etc.)
import androidx.compose.foundation.lazy.LazyColumn // 导入 LazyColumn 用于可滚动列表
import androidx.compose.foundation.lazy.items // 导入 items 扩展函数用于 LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.* // 导入 Material 3 组件 (Scaffold, Button, Text, CircularProgressIndicator, etc.)
import androidx.compose.runtime.* // 导入运行时组件 (Composable, remember, collectAsState, LaunchedEffect, rememberCoroutineScope, getValue, etc.)
import androidx.compose.ui.Alignment // 导入 Alignment 用于定位
import androidx.compose.ui.Modifier // 导入 Modifier 用于样式/布局
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp // 导入 dp 单位
// 导入 ViewModel 和 Navigation 相关库
import androidx.lifecycle.viewmodel.compose.viewModel // 导入 viewModel() Composable 函数
import androidx.navigation.NavHostController // 导入 NavController 用于导航操作
// 导入特定的 ViewModel
import com.example.artsyfrontend.viewmodel.HomeViewModel
// 导入自定义的 UI 组件
import com.example.artsyfrontend.ui.component.HomeTopBar
import com.example.artsyfrontend.ui.component.DateHeader
import com.example.artsyfrontend.ui.component.FavoriteArtistListItem
import com.example.artsyfrontend.ui.component.PoweredByArtsy
// 导入数据模型 (如果直接在 Composable 中需要，虽然此文件中可能性不大)
import com.example.artsyfrontend.data.model.User
// 导入协程启动库
import kotlinx.coroutines.launch

/**
 * Home Screen 的 Composable 函数.
 * 这是应用的主屏幕，显示用户信息、收藏夹，并提供导航入口。
 *
 * @param navController 导航控制器，用于处理页面跳转。
 * @param viewModel HomeViewModel 实例，提供 UI 状态和业务逻辑。
 */
@Composable
fun HomeScreen(
    navController: NavHostController, // 从导航图中接收 NavController
    viewModel: HomeViewModel = viewModel() // 使用 compose-viewModel 库获取 ViewModel 实例
) {
    // --- 状态收集 ---
    // 使用 collectAsState 从 ViewModel 的 StateFlow 中安全地读取状态。
    // 当 StateFlow 发出新值时，这将触发 Composable 的重组。
    val currentUser by viewModel.currentUser.collectAsState() // 当前登录用户
    val favorites by viewModel.favorites.collectAsState() // 收藏列表
    val isLoadingInitialData by viewModel.isLoadingInitialData.collectAsState() // 初始用户检查是否完成
    val isLoadingFavorites by viewModel.isLoadingFavorites.collectAsState() // 收藏列表是否正在加载

    // --- Snackbar 设置 ---
    // 创建并记住 SnackbarHostState，它持有 Snackbar 队列并控制其显示。
    val snackbarHostState = remember { SnackbarHostState() }
    // 获取与当前 Composable 生命周期绑定的协程作用域，用于启动显示 Snackbar 的协程。
    val scope = rememberCoroutineScope()

    // --- 副作用处理 (LaunchedEffect) ---
    // LaunchedEffect 用于在 Composable 的生命周期内执行挂起函数或启动协程。

    // Effect 1: 收集来自 ViewModel 的通用 Snackbar 消息。
    // key 为 Unit 表示此 Effect 只在 Composable 首次进入组合时启动一次。
    LaunchedEffect(Unit, block = {
        viewModel.snackbarMessage.collect { message -> // 持续收集消息流
            Log.d("HomeScreen", "收到通用 Snackbar 消息: $message")
            // 在 Composable 的作用域内启动协程来显示 Snackbar。
            scope.launch {
                snackbarHostState.showSnackbar(message = message) // 显示 Snackbar
            }
        }
    })

    // Effect 2: 处理从 LoginScreen 返回的登录成功导航结果。
    // key 设置为 navController.currentBackStackEntry，确保在导航到此屏幕时检查结果。
    LaunchedEffect(navController.currentBackStackEntry, block = {
        // 尝试从上一个屏幕的 SavedStateHandle 中获取 "login_success" 标记。
        val loginSuccessResult = navController.currentBackStackEntry
            ?.savedStateHandle?.get<Boolean>("login_success")

        if (loginSuccessResult == true) { // 如果标记存在且为 true
            Log.d("HomeScreen", "收到 login_success 导航结果。")
            viewModel.showLoginSuccessSnackbar() // 通知 ViewModel 显示登录成功消息。
            // *** 处理完后立即移除标记，防止重复处理 ***
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("login_success")
            Log.d("HomeScreen", "已移除 login_success 导航结果。")
        }
    })

    // Effect 3: 处理从 DetailScreen 返回的收藏列表已更新导航结果。
    // key 同样设置为 navController.currentBackStackEntry。
    LaunchedEffect(navController.currentBackStackEntry, block = {
        // 尝试从上一个屏幕的 SavedStateHandle 中获取 "favorites_updated" 标记。
        val favoritesUpdatedResult = navController.currentBackStackEntry
            ?.savedStateHandle?.get<Boolean>("favorites_updated")

        if (favoritesUpdatedResult == true) { // 如果标记存在且为 true
            Log.d("HomeScreen", "收到 favorites_updated 导航结果。")
            viewModel.refreshFavorites() // 通知 ViewModel 强制刷新收藏列表。
            // *** 处理完后立即移除标记，防止重复处理 ***
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("favorites_updated")
            Log.d("HomeScreen", "已移除 favorites_updated 导航结果。")
        }
    })

    // Effect 4: (可选) 记录状态变化以供调试。
    // 当 key 中的任何一个状态发生变化时，此 Effect 会重新执行。
    LaunchedEffect(currentUser, favorites, isLoadingInitialData, isLoadingFavorites, block = {
        Log.d("HomeScreen", "状态更新 -> isLoadingInitial: $isLoadingInitialData, isLoadingFav: $isLoadingFavorites, currentUser: ${currentUser?.email}, Favorites count: ${favorites.size}")
    })

    // --- UI 布局：Scaffold ---
    // Scaffold 提供符合 Material Design 指南的应用基础布局结构。
    Scaffold(
        // 在 Scaffold 底部指定 SnackbarHost，用于显示 Snackbar 消息。
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        // 设置顶部应用栏 (TopAppBar)。
        topBar = {
            // 使用自定义的 HomeTopBar 组件。
            HomeTopBar(
                navController = navController, // 传递导航控制器给 TopBar 用于导航操作。
                currentUser = currentUser,     // 传递当前用户状态给 TopBar 以显示头像或登录图标。
                onLogoutClick = { viewModel.logout() }, // 将 ViewModel 的登出函数作为回调传递。
                onDeleteAccountClick = { viewModel.deleteAccount() } // 将 ViewModel 的删除账户函数作为回调传递。
            )
        }
    ) { paddingValues -> // Scaffold 的内容区域 lambda 会接收一个 PaddingValues 参数。

        // --- 主要滚动内容：LazyColumn ---
        // 使用 LazyColumn 来高效地显示可能包含大量收藏项的列表。
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues) // 应用 Scaffold 计算出的内边距，防止内容与 AppBar 重叠。
                .fillMaxSize(),        // 使列表占据所有可用垂直和水平空间。
            contentPadding = PaddingValues(bottom = 16.dp) // 在列表内容的底部添加额外的内边距。
        ) {
            // --- 列表项定义 ---

            // 列表项 1: 显示日期的自定义组件。
            item {
                DateHeader()
            }

            // 列表项 2: "Favorites" 灰色矩形长条标题
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth() // 1. 让 Box (长条) 占据屏幕全部宽度
                        .padding(top = 4.dp, bottom = 4.dp) // 2. 保留原有的顶部和底部外边距，控制与其他元素的距离
                        .background(MaterialTheme.colorScheme.surfaceVariant) // 3. 设置背景为灰色 (surfaceVariant 是常用的灰色调，您也可以用 Color.LightGray 或其他)
                        // 4. 设置 Box 内部的垂直内边距，这会影响长条的视觉高度
                        .padding(vertical = 2.dp),
                    // 5. 设置内容对齐方式为居中，这样内部的 Text 就会居中
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Favorites",
                        // 6. 应用字体样式 (可以保持 titleMedium 或根据需要调整)
                        style = MaterialTheme.typography.titleMedium,
                        // 7. 设置字体加粗
                        fontWeight = FontWeight.Bold,
                        // 8. 设置文本颜色，需要确保在灰色背景上清晰可见
                        //    onSurfaceVariant 通常是 surfaceVariant 背景的好选择
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                        // 您也可以尝试 onSurface:
                        // color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 列表项 3: 根据不同的加载和登录状态，条件性地显示内容。
            when {
                // 情况 1: 正在进行初始用户检查 (isLoadingInitialData 为 true)。
                isLoadingInitialData -> {
                    item { // 在 LazyColumn 中，独立的 UI 元素需要包裹在 item { } 中。
                        // 使用 Box 将加载指示器居中显示。
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth() // 宽度填充 LazyColumn 的可用宽度。
                                .padding(vertical = 50.dp), // 提供垂直间距。
                            contentAlignment = Alignment.Center // 内容居中对齐。
                        ) {
                            CircularProgressIndicator() // 显示 Material Design 的圆形加载指示器。
                            Log.d("HomeScreen", "UI 状态: 显示初始加载指示器")
                        }
                    }
                }
                // 情况 2: 初始检查完成，但用户未登录 (currentUser 为 null)。
                currentUser == null -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxWidth()
                                .padding(vertical = 50.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // 显示一个按钮，提示用户登录。
                            Button(onClick = { navController.navigate("login") }) { // 点击按钮导航到 "login" 路由。
                                Text("Log in to see favorites")
                            }
                            Log.d("HomeScreen", "UI 状态: 显示 'Log in' 按钮")
                        }
                    }
                }
                // 情况 3: 初始检查完成，且用户已登录 (currentUser 不为 null)。
                else -> {
                    // 记录当前状态，便于调试。
                    Log.d("HomeScreen", "UI 状态: 渲染已登录部分。isLoadingFav: $isLoadingFavorites, Fav count: ${favorites.size}")
                    // 进一步检查收藏列表是否正在加载。
                    if (isLoadingFavorites) {
                        // 子情况 3a: 正在加载收藏列表。
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .padding(vertical = 50.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator() // 显示加载指示器。
                                Log.d("HomeScreen", "UI 状态: 显示收藏加载指示器")
                            }
                        }
                    } else if (favorites.isEmpty()) {
                        // 子情况 3b: 收藏列表加载完成，但列表为空。
                        item {
                            // --- 修改开始 (应用淡蓝色圆角矩形样式) ---
                            // 1. 添加外部 Padding，用于控制矩形距离屏幕边缘和上下元素的间距
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth() // 让外部 Box 充满宽度以便应用内边距
                                    // 设置外边距 (调整 vertical 值控制上下间距)
                                    .padding(horizontal = 16.dp, vertical = 24.dp),
                                contentAlignment = Alignment.Center // 使内部的蓝色 Box 居中（如果它不充满宽度）
                            ) {
                                // 2. 创建内部 Box 作为蓝色圆角矩形背景
                                Box(
                                    modifier = Modifier
                                        // 让蓝色矩形也充满可用宽度 (在外边距内部)
                                        .fillMaxWidth()
                                        // 应用背景颜色和圆角形状
                                        .background(
                                            // 使用 primaryContainer 作为淡蓝色背景 (您可以根据主题调整)
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            // 设置圆角大小 (例如 12.dp)
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        // 设置矩形内部的 Padding，给文字留出空间
                                        // 可以调整 vertical 值控制矩形的视觉高度
                                        .padding(horizontal = 32.dp, vertical = 24.dp),
                                    // 设置内部内容 (Text) 居中对齐
                                    contentAlignment = Alignment.Center
                                ) {
                                    // 3. 显示 "No favorites" 文本
                                    Text(
                                        text = "No favorites",
                                        // 选择合适的字体样式，例如 bodyLarge 或 titleMedium
                                        style = MaterialTheme.typography.bodyLarge,
                                        // 确保文本颜色在 primaryContainer 背景上清晰可见
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            // 更新日志信息
                            Log.d("HomeScreen", "UI 状态: 显示 'No favorites' 蓝色圆角矩形消息")
                            // --- 修改结束 ---
                        }
                    } else {
                        // 子情况 3c: 收藏列表加载完成，且列表不为空。
                        Log.d("HomeScreen", "UI 状态: 渲染包含 ${favorites.size} 个项目的收藏列表")
                        // 使用 `items` 扩展函数来高效地渲染列表项。
                        items(
                            items = favorites, // 提供数据列表。
                            key = { favoriteItem -> favoriteItem.id } // 提供唯一稳定的 key，用于优化性能和动画。
                        ) { favItem -> // 这个 lambda 对列表中的每一项执行。
                            // 使用自定义的 FavoriteArtistListItem 组件来显示每个收藏项。
                            FavoriteArtistListItem(
                                item = favItem,
                                navController = navController // 传递 NavController，允许列表项触发导航。
                            )
                            // 在每个列表项下方添加一个分隔线。
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }
                    }
                } // End else (用户已登录)
            } // End when

            // 列表项 4: "Powered by Artsy" 页脚组件。
            item {
                PoweredByArtsy()
            }

        } // End LazyColumn
    } // End Scaffold
} // End HomeScreen Composable