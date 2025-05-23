package com.example.artsyfrontend.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape // 用于按钮背景
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// --- 修改开始 (样式统一: 导入所需图标) ---
import androidx.compose.material.icons.filled.ChevronRight // V形箭头
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
// --- 修改结束 ---
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip // 如果 Logo 需要裁剪
import androidx.compose.ui.graphics.Color // 用于自定义颜色
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.artsyfrontend.R
import com.example.artsyfrontend.data.model.SearchArtist
import android.util.Log
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SearchResultItem(
    artist: SearchArtist,
    navController: NavHostController,
    isLoggedIn: Boolean,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {
    // 日志 (保持不变)
    LaunchedEffect(artist) {
        Log.d("SearchResultItem", "Displaying artist: Name='${artist.name}', Image='${artist.image}'")
    }

    Card(
        modifier = Modifier
            // 卡片外边距 (保持 SearchResultItem 原有的)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
            .clickable { navController.navigate("detail/${artist.id}") }, // 整体可点击
        shape = RoundedCornerShape(8.dp), // 圆角
        // --- 修改开始 (样式统一: 调整阴影与 SimilarArtistCardItem 一致) ---
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // 使用 1.dp 阴影
        // --- 修改结束 ---
    ) {
        // 使用 Box 布局来叠加元素
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1. 图片区域 (作为背景层)
            val imageModifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f) // 保持 16:9 宽高比

            // 条件渲染图片或 Logo (逻辑与 SimilarArtistCardItem 保持一致)
            if (artist.image.isNullOrBlank() || artist.image == "/assets/shared/missing_image.png") {
                Image(
                    painter = painterResource(id = R.drawable.artsy_logo),
                    contentDescription = "Artsy Logo Placeholder",
                    contentScale = ContentScale.Fit, // Logo 使用 Fit
                    modifier = imageModifier
                        .background(MaterialTheme.colorScheme.surfaceVariant) // Logo 背景
                        .padding(16.dp) // 给 Logo 一些内边距，使其居中感更强
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artist.image)
                        .crossfade(true)
                        .placeholder(R.drawable.artsy_logo) // 加载/错误时显示 Logo
                        .error(R.drawable.artsy_logo)
                        .build(),
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop, // 图片使用 Crop
                    modifier = imageModifier
                )
            }

            // --- 修改开始 (样式统一: 调整收藏按钮样式) ---
            // 2. 收藏按钮 (星星)，叠加在图片右上角
            if (isLoggedIn) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd) // 对齐到右上角
                        .padding(8.dp) // 与角落的距离 (调整为 8dp)
                        // 添加半透明圆形背景
                        .background(
                            color = Color.Black.copy(alpha = 0.4f), // 半透明黑色背景
                            shape = CircleShape // 圆形
                        )
                        .size(36.dp) // 按钮整体大小 (触摸区域)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = Color.White, // 固定为白色图标，以便在各种背景下可见
                        modifier = Modifier.size(20.dp) // 图标本身的大小
                    )
                }
            }
            // --- 修改结束 ---

            // --- 修改开始 (样式统一: 添加底部信息栏 Overlay) ---
            // 3. 底部名字栏 Row，叠加在图片底部
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // 对齐到底部
                    .fillMaxWidth() // 宽度占满
                    // 设置半透明背景 (与 SimilarArtistCardItem 一致)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                    .padding(horizontal = 16.dp, vertical = 8.dp), // 行内边距 (调整垂直边距)
                verticalAlignment = Alignment.CenterVertically // 内部元素垂直居中
            ) {
                // 艺术家名字
                Text(
                    text = artist.name,
                    style = MaterialTheme.typography.titleMedium, // 字体
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f), // 占据左侧空间
                    color = MaterialTheme.colorScheme.onSurfaceVariant // 确保颜色可见
                )
                // 添加 V 形箭头图标 (与 SimilarArtistCardItem 一致)
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "View Details", // 描述可以为空或 "查看详情"
                    tint = MaterialTheme.colorScheme.onPrimaryContainer // 确保颜色可见
                )
            }
            // --- 修改结束 ---

        } // Box 结束
    } // Card 结束
}


// Preview (保持不变或按需调整)
@Preview(showBackground = true)
@Composable
fun SearchResultItemPreview() {
    val sampleArtistWithImage = SearchArtist(id = "sample-1", name = "Claude Monet With A Very Long Name To Test Ellipsis", image = "http://example.com/image.jpg")
    val sampleArtistNoImage = SearchArtist(id = "sample-2", name = "Artist Without Image", image = null)
    val navController = rememberNavController()

    Surface(color = Color.LightGray, modifier = Modifier.padding(16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SearchResultItem(
                artist = sampleArtistWithImage,
                navController = navController,
                isLoggedIn = true,
                isFavorite = false,
                onToggleFavorite = {}
            )
            SearchResultItem(
                artist = sampleArtistNoImage,
                navController = navController,
                isLoggedIn = true,
                isFavorite = true,
                onToggleFavorite = {}
            )
            SearchResultItem(
                artist = sampleArtistWithImage,
                navController = navController,
                isLoggedIn = false,
                isFavorite = false,
                onToggleFavorite = {}
            )
        }
    }
}