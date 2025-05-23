package com.example.artsyfrontend.ui.component

import android.util.Log
import androidx.compose.foundation.clickable // <<< 新增：如果需要点击效果
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box // <<< 新增 for Dropdown anchor
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // <<< 新增：用于异步加载图片
import com.example.artsyfrontend.data.model.User // <<< 新增：导入 User 模型


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    navController: NavHostController,
    currentUser: User?,
    // --- MODIFICATION START (Requirement 5.8.3 - Action Callbacks) ---
    // 添加回调函数参数
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
    // --- MODIFICATION END ---
) {
    TopAppBar(
        title = { Text("Artist Search") }, // 标题可以保持不变，或根据需要修改
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        actions = {
            // --- 搜索按钮 (保持不变) ---
            IconButton(onClick = {
                Log.d("HomeTopBar", "Search icon clicked")
                navController.navigate("search")
            }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search Artists"
                )
            }

            // --- 用户区域 ---
            // --- MODIFICATION START (Requirement 5.8.3 - Dropdown Menu) ---
            // 使用 Box 作为 DropdownMenu 的锚点容器
            Box {
                // 用于控制菜单展开/折叠的状态
                var menuExpanded by remember { mutableStateOf(false) }

                if (currentUser != null) {
                    // --- 用户已登录：显示头像按钮，点击打开菜单 ---
                    IconButton(onClick = {
                        Log.d("HomeTopBar", "Avatar clicked, expanding menu.")
                        menuExpanded = true // 点击头像时展开菜单
                    }) {
                        AsyncImage(
                            model = currentUser.profileImageUrl,
                            contentDescription = "User Avatar",
                            modifier = Modifier.size(32.dp).clip(CircleShape),
                        )
                    }
                } else {
                    // --- 用户未登录：显示登录图标按钮 ---
                    IconButton(onClick = { navController.navigate("login") }) {
                        Icon(Icons.Filled.AccountCircle, "Log In")
                    }
                }

                // --- 下拉菜单 ---
                DropdownMenu(
                    expanded = menuExpanded, // 由状态变量控制是否展开
                    onDismissRequest = { menuExpanded = false } // 点击菜单外部时关闭菜单
                ) {
                    // --- 登出 菜单项 ---
                    DropdownMenuItem(
                        text = { Text("Log Out") },
                        onClick = {
                            Log.d("HomeTopBar", "Log Out menu item clicked.")
                            menuExpanded = false // 关闭菜单
                            onLogoutClick()      // 调用 ViewModel 的登出函数
                        },
                    )
                    // --- 删除账户 菜单项 ---
                    DropdownMenuItem(
                        text = {
                            Text("Delete Account", color = Color.Red)
                               },
                        onClick = {
                            Log.d("HomeTopBar", "Delete Account menu item clicked.")
                            menuExpanded = false // 关闭菜单
                            onDeleteAccountClick() // 调用 ViewModel 的删除函数
                        },
                    )
                }
            } // End Box
            // --- MODIFICATION END ---
        }
    )
}

// --- Preview 需要更新以包含新的回调参数 ---
@Preview(name = "Logged In State", showBackground = true)
@Composable
fun HomeTopBarLoggedInPreview() {
    val navController = rememberNavController()
    val sampleUser = User(id="1", fullname="Jane Doe", email="jane@example.com", profileImageUrl = "https://www.gravatar.com/avatar/placeholder?d=identicon")
    HomeTopBar(
        navController = navController,
        currentUser = sampleUser,
        onLogoutClick = { Log.d("Preview", "Logout clicked") }, // 提供空实现或日志
        onDeleteAccountClick = { Log.d("Preview", "Delete clicked") } // 提供空实现或日志
    )
}

@Preview(name = "Logged Out State", showBackground = true)
@Composable
fun HomeTopBarLoggedOutPreview() {
    val navController = rememberNavController()
    HomeTopBar(
        navController = navController,
        currentUser = null,
        onLogoutClick = {}, // 提供空实现
        onDeleteAccountClick = {} // 提供空实现
    )
}