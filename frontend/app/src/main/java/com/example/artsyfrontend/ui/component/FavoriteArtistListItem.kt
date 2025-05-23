package com.example.artsyfrontend.ui.component

import android.util.Log // 日志记录
import androidx.compose.foundation.clickable // 使 Row 可点击
import androidx.compose.foundation.layout.* // 布局组件 (Row, Column, Spacer, padding, etc.)
// Material 3 和图标导入
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight // 自动镜像箭头
// Compose 运行时和 UI 工具导入
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect // 可选，用于调试打印 item
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight // 字体加粗
import androidx.compose.ui.tooling.preview.Preview // 预览
import androidx.compose.ui.unit.dp // 尺寸单位
// Navigation 和数据模型导入
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.artsyfrontend.data.model.FavouriteItem // 收藏项数据模型
// 时间处理相关导入 (需要 API 26+)
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException // 用于错误处理
import java.time.temporal.ChronoUnit // 用于预览生成示例时间
import java.lang.Exception // 通用异常

// 可选：导入 Coil 和相关组件以显示图片
// import coil.compose.AsyncImage
// import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.ui.draw.clip
// import androidx.compose.ui.layout.ContentScale
// import androidx.compose.ui.res.painterResource
// import com.example.artsyfrontend.R

/**
 * 用于在 HomeScreen 显示单个收藏艺术家的列表项 Composable.
 * 包含了布局调整、逗号分隔详情、时间位置、垂直居中和英文文本。
 */
@Composable
fun FavoriteArtistListItem(
    item: FavouriteItem,
    navController: NavHostController // 用于点击时导航
) {
    // 可选：添加 LaunchedEffect 在 item 变化时打印日志，方便调试数据
    LaunchedEffect(item) {
        Log.d("FavoriteItemData", "Rendering item: ID=${item.id}, Name=${item.name}, Nat=${item.nationality}, Bday=${item.birthday}, Added=${item.addedAt}")
    }

    // 主 Row 布局，包含所有内容，并响应点击事件
    Row(
        modifier = Modifier
            .fillMaxWidth() // 充满可用宽度
            .clickable { // 使整行可点击
                Log.d("FavoriteArtistListItem", "Navigating to detail/${item.id}")
                navController.navigate("detail/${item.id}") // 导航到详情页
            }
            .padding(horizontal = 16.dp, vertical = 12.dp), // 行的内边距
        verticalAlignment = Alignment.CenterVertically // 垂直居中对齐所有子项
    ) {

        // 可选：在此处添加艺术家图片 (如果需要)
        /*
        AsyncImage(
            model = item.image, // 使用 FavouriteItem 中的 image 字段
            contentDescription = item.name, // 图片描述
            modifier = Modifier
                .size(48.dp) // 设置图片大小
                .clip(RoundedCornerShape(4.dp)), // 可选：设置圆角
            contentScale = ContentScale.Crop, // 裁剪图片以填充
            placeholder = painterResource(id = R.drawable.artsy_logo), // 占位图
            error = painterResource(id = R.drawable.artsy_logo) // 错误图
        )
        Spacer(modifier = Modifier.width(12.dp)) // 图片和文本之间的间距
        */

        // 左侧内容列 (艺术家名字和详情)
        Column(
            // 使用 weight(1f) 使其占据除右侧时间和箭头外的所有剩余空间
            modifier = Modifier.weight(1f)
        ) {
            // 艺术家名字
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium, // 中号标题样式
                fontWeight = FontWeight.Bold // 加粗
            )
            Spacer(modifier = Modifier.height(4.dp)) // 名字和详情间的垂直间距

            // 构建并显示详情文本 (国籍, 生卒年)
            val nationality = item.nationality?.takeIf { it.isNotBlank() }
            val birthday = item.birthday?.takeIf { it.isNotBlank() }
            val deathday = item.deathday?.takeIf { it.isNotBlank() }

            val lifespan = when {
                birthday != null && deathday != null -> "$birthday – $deathday"
                birthday != null -> "b. $birthday"
                deathday != null -> "d. $deathday"
                else -> null
            }

            // 使用 ", " (逗号加空格) 连接国籍和生卒年 (仅当两者都存在时)
            val detailsText = listOfNotNull(nationality, lifespan).joinToString(", ")

            // 仅当详情文本不为空时显示
            if (detailsText.isNotEmpty()) {
                Text(
                    text = detailsText,
                    style = MaterialTheme.typography.bodyMedium, // 中号正文样式
                    color = MaterialTheme.colorScheme.onSurfaceVariant // 使用次要文本颜色
                )
            }
        } // 左侧内容列结束

        // 右侧内容 Row (添加时间和导航箭头)
        Row(
            verticalAlignment = Alignment.CenterVertically // 确保时间和箭头垂直对齐
        ) {
            // 显示添加时间 (如果 addedAt 不为 null)
            item.addedAt?.let { addedAtString ->
                Text(
                    text = formatTimeAgo(addedAtString), // 调用辅助函数格式化时间
                    style = MaterialTheme.typography.bodySmall, // 小号字体
                    color = MaterialTheme.colorScheme.outline, // 使用轮廓颜色 (或 onSurfaceVariant)
                    modifier = Modifier.padding(end = 4.dp) // 与箭头之间的间距
                )
            }

            // 导航箭头图标
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, // 自动镜像箭头
                contentDescription = "View Details", // 英文内容描述 (用于无障碍)
                tint = MaterialTheme.colorScheme.onSurfaceVariant, // 图标颜色
                modifier = Modifier.size(24.dp) // 图标大小
            )
        } // 右侧内容 Row 结束
    } // 主 Row 结束
}

/**
 * 将 ISO 8601 时间戳字符串格式化为 "X time ago" 格式的辅助函数。
 * 需要 API level 26+。
 * 包含英文提示和错误处理。
 */
@Composable
private fun formatTimeAgo(isoTimestamp: String?): String {
    if (isoTimestamp == null) return "Added time unknown" // 处理 null 情况

    return try {
        val instant = Instant.parse(isoTimestamp)
        val now = Instant.now()
        val duration = Duration.between(instant, now)

        when {
            // 增加更友好的单数形式
            duration.seconds < 2 -> "1 second ago"
            duration.seconds < 60 -> "${duration.seconds} seconds ago"
            duration.toMinutes() < 2 -> "1 minute ago"
            duration.toMinutes() < 60 -> "${duration.toMinutes()} minutes ago"
            duration.toHours() < 2 -> "1 hour ago"
            duration.toHours() < 24 -> "${duration.toHours()} hours ago"
            duration.toDays() < 2 -> "1 day ago"
            duration.toDays() < 7 -> "${duration.toDays()} days ago"
            duration.toDays() < 14 -> "1 week ago" // 一周左右
            duration.toDays() < 30 -> "${duration.toDays() / 7} weeks ago" // 几周前
            duration.toDays() < 60 -> "1 month ago" // 一个月左右
            duration.toDays() < 365 -> "${duration.toDays() / 30} months ago" // 几个月前 (近似值)
            else -> "Over a year ago" // 超过一年
        }
    } catch (e: DateTimeParseException) { // 更精确地捕获解析异常
        Log.e("FormatTimeAgo", "Failed to parse timestamp: $isoTimestamp", e)
        "Invalid date format" // 返回英文错误提示
    } catch (e: Exception) { // 捕获其他可能的异常
        Log.e("FormatTimeAgo", "Error formatting time ago for: $isoTimestamp", e)
        "Error formatting date" // 通用错误提示
    }
}

// --- Preview 代码 ---
// (确保预览数据结构与 FavouriteItem 一致)
@Preview(showBackground = true, name="有详情和时间的预览")
@Composable
fun FavoriteArtistListItemPreview() {
    val navController = rememberNavController()
    val sampleItem = FavouriteItem(
        id = "1",
        name = "Leonardo da Vinci",
        nationality = "Italian",
        birthday = "1452",
        deathday = "1519",
        image = null, // 预览时可以提供 URL
        addedAt = Instant.now().minus(2, ChronoUnit.DAYS).toString() // 模拟 "2 days ago"
    )
    MaterialTheme { // 建议包裹在 Theme 中以获得正确的样式
        FavoriteArtistListItem(item = sampleItem, navController = navController)
    }
}

@Preview(showBackground = true, name="无详情仅有时间的预览")
@Composable
fun FavoriteArtistListItemPreview_NoDetails() {
    val navController = rememberNavController()
    val sampleItem = FavouriteItem(
        id = "2",
        name = "Artist Without Details",
        nationality = null,
        birthday = null,
        deathday = null,
        image = null,
        addedAt = Instant.now().minus(5, ChronoUnit.HOURS).toString() // 模拟 "5 hours ago"
    )
    MaterialTheme {
        FavoriteArtistListItem(item = sampleItem, navController = navController)
    }
}

@Preview(showBackground = true, name="有国籍无生卒年")
@Composable
fun FavoriteArtistListItemPreview_NationalityOnly() {
    val navController = rememberNavController()
    val sampleItem = FavouriteItem(
        id = "3",
        name = "Artist With Nationality",
        nationality = "French",
        birthday = null,
        deathday = null,
        image = null,
        addedAt = Instant.now().minus(3, ChronoUnit.WEEKS).toString() // 模拟 "3 weeks ago"
    )
    MaterialTheme {
        FavoriteArtistListItem(item = sampleItem, navController = navController)
    }
}