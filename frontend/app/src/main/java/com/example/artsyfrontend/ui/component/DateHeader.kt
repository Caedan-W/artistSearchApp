package com.example.artsyfrontend.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember // <<< 导入 remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview // <<< 导入 Preview
import androidx.compose.ui.unit.dp
import java.time.LocalDate // <<< 导入 LocalDate
import java.time.format.DateTimeFormatter // <<< 导入 DateTimeFormatter
import java.util.Locale // <<< 导入 Locale

// --- 任务 3: 实现日期显示 ---

@Composable
fun DateHeader() {
    // 使用 remember 缓存计算结果，避免不必要的重算
    // 仅当 Composable 首次创建或其所在作用域重启时才会重新计算日期
    val formattedDate = remember {
        // 1. 获取当前日期 (需要 API 26+ 或启用 coreLibraryDesugaring)
        val currentDate = LocalDate.now()
        // 2. 定义日期格式 (例如: 03 May 2025)
        // 使用 "dd" 表示两位数的日期, "MMMM" 表示月份全名, "yyyy" 表示四位数年份
        val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())
        // 3. 格式化日期
        currentDate.format(formatter)
    }

    // 4. 使用 Text 组件显示格式化后的日期
    Text(
        text = formattedDate,
        style = MaterialTheme.typography.bodySmall, // 使用较小的字体样式
        color = MaterialTheme.colorScheme.onSurfaceVariant, // 使用次要文本颜色，使其不那么突出
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp) // 设置合适的内边距
    )
}

// 添加 Preview 以便单独预览 DateHeader
@Preview(showBackground = true)
@Composable
fun DateHeaderPreview() {
    // 可以包裹在 Theme 中预览
    // ArtsyFrontendTheme {
    DateHeader()
    // }
}