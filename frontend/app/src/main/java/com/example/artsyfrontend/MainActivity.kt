package com.example.artsyfrontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.artsyfrontend.ui.theme.ArtsyFrontendTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import com.example.artsyfrontend.ui.navigation.AppNavHost


class MainActivity : ComponentActivity() {
    // 一定要在类里声明，否则 onCreate 里找不到 keepSplashOn
    private var keepSplashOn = true
    override fun onCreate(savedInstanceState: Bundle?) {
        // 在 super.onCreate 之前安装 SplashScreen
        val splash = installSplashScreen()
        // 只要 keepSplashOn == true，就一直保留启动页
        splash.setKeepOnScreenCondition { keepSplashOn }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 初始化／延迟逻辑：固定延迟 2 秒后才让启动页退出
        // Coroutines 版
        lifecycleScope.launch {
            delay(1_000)
            keepSplashOn = false
        }

//        setContent {
//            ArtsyFrontendTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
//            }
//        }
        setContent {
            ArtsyFrontendTheme {
                // 创建并传入 NavController
                val navController = rememberNavController()
                AppNavHost(navController)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ArtsyFrontendTheme {
        Greeting("Android")
    }
}
