package com.example.artsyfrontend.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler // <<< 导入 LocalUriHandler
import androidx.compose.ui.text.font.FontStyle // <<< 导入 FontStyle 用于斜体
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// --- 任务 12: 实现 "Powered by Artsy" 链接 ---

@Composable
fun PoweredByArtsy() {
    // 1. 获取 UriHandler，用于打开链接
    val uriHandler = LocalUriHandler.current
    // 2. 定义要打开的 URL
    val artsyUrl = "https://www.artsy.net/"

    // 3. 使用 Box 实现居中布局
    Box(
        modifier = Modifier
            .fillMaxWidth() // 宽度占满
            .padding(vertical = 16.dp), // 设置垂直方向的内边距
        contentAlignment = Alignment.Center // 内容居中对齐
    ) {
        // 4. 显示文本，并使其可点击
        Text(
            text = "Powered by Artsy",
            style = MaterialTheme.typography.bodySmall, // 使用小号字体
            fontStyle = FontStyle.Italic, // 设置为斜体
            color = MaterialTheme.colorScheme.onSurfaceVariant, // 使用次要文本颜色
            modifier = Modifier.clickable { // 使文本可点击
                // 5. 点击时使用 UriHandler 打开链接
                try {
                    uriHandler.openUri(artsyUrl)
                } catch (e: Exception) {
                    // 可选：处理无法打开链接的异常情况 (例如没有浏览器应用)
                    // Log.e("PoweredByArtsy", "Could not open URL: $artsyUrl", e)
                }
            }
        )
    }
}

// 添加 Preview
@Preview(showBackground = true)
@Composable
fun PoweredByArtsyPreview() {
    // ArtsyFrontendTheme { // 应用主题
    PoweredByArtsy()
    // }
}