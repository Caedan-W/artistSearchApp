package com.example.artsyfrontend.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType // <<< (可选) 如果需要更复杂的参数类型
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument // <<< (可选) 如果需要参数约束
import androidx.navigation.NavGraph.Companion.findStartDestination // <<< (L-E) 需要导入

import com.example.artsyfrontend.ui.screen.HomeScreen
import com.example.artsyfrontend.ui.screen.SearchScreen
import com.example.artsyfrontend.ui.screen.DetailScreen // <<< 确保 DetailScreen 不再强制要求 artistId 参数
import com.example.artsyfrontend.ui.screen.LoginScreen
import com.example.artsyfrontend.ui.screen.RegisterScreen



@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        // Home
        composable("home") {
            HomeScreen(navController)
        }
        // Login
        composable("login") {
            LoginScreen(navController)
        }

        // Detail 路由定义修改
        composable(
            route = "detail/{artistId}", // 路由路径和参数占位符
            arguments = listOf(navArgument("artistId") { type = NavType.StringType }) // (可选但推荐) 定义参数类型
        ) { backStackEntry -> // <<< 使用 backStackEntry (虽然这里没直接用，但它是触发自动注入的关键)
            // 直接调用 DetailScreen，不手动传递 artistId
            // DetailScreen 内部的 viewModel<DetailViewModel>() 会自动获取 SavedStateHandle
            DetailScreen(navController = navController)
        }

        // Search 路由定义
        composable("search") {
            SearchScreen(navController)
        }

        // Register 路由定义
        composable("register") {
            RegisterScreen(navController)
        }
    }
}