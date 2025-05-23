package com.example.artsyfrontend.ui.component

import androidx.compose.foundation.Image // 用于显示 Logo
import androidx.compose.foundation.background // 用于设置背景色
import androidx.compose.foundation.clickable // 用于 Card 点击
import androidx.compose.foundation.layout.* // Box, Column, Row, Spacer, padding, size, fillMaxWidth, aspectRatio
import androidx.compose.foundation.shape.CircleShape // 用于按钮圆形背景
import androidx.compose.foundation.shape.RoundedCornerShape // 用于 Card 圆角
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight // V 形箭头
import androidx.compose.material.icons.filled.Star // 实心星
import androidx.compose.material.icons.outlined.StarBorder // 空心星
import androidx.compose.material3.Card // 卡片布局
import androidx.compose.material3.CardDefaults // 卡片默认值 (例如阴影)
import androidx.compose.material3.Icon // 图标
import androidx.compose.material3.IconButton // 图标按钮
import androidx.compose.material3.MaterialTheme // 用于获取主题颜色
import androidx.compose.material3.Surface // 用于 Preview 背景
import androidx.compose.material3.Text // 文本
import androidx.compose.runtime.* // Composable, remember, mutableStateOf, getValue, setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.clip // 图片不再需要 clip，由 Card 处理圆角
import androidx.compose.ui.graphics.Color // 用于自定义颜色 (按钮背景)
import androidx.compose.ui.layout.ContentScale // 图片缩放模式
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // 加载 Drawable 资源
import androidx.compose.ui.text.style.TextAlign // 文本对齐
import androidx.compose.ui.text.style.TextOverflow // 文本溢出
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // Coil 图片加载
import coil.request.ImageRequest
import com.example.artsyfrontend.R // R 文件，包含 drawable.artsy_logo
import com.example.artsyfrontend.data.model.SimilarArtist // 数据模型
import androidx.compose.runtime.* // 导入 remember, mutableStateOf, etc.
import androidx.compose.ui.text.font.FontWeight

/**
 * 用于 "Similar" Tab 显示单个相似艺术家的卡片 Composable
 * 样式根据 image_8890c8.png 调整:
 * - 图片上方叠加收藏按钮 (右上角)
 * - 图片下方叠加半透明信息条 (名字 + 箭头)
 */
@Composable
fun SimilarArtistCardItem(
    artist: SimilarArtist,
    navController: NavHostController,
    // TODO: 需要从 ViewModel 接收 isFavorite 状态和 onToggleFavorite 回调
    // --- MODIFICATION START (Task D17 - Functionality) ---
    isInitiallyFavorite: Boolean, // <<< 接收初始收藏状态
    onToggleFavorite: (currentState: Boolean) -> Unit // <<< 接收切换收藏的回调
    // --- MODIFICATION END ---
) {
    // 临时的本地收藏状态，仅用于预览图标切换效果
    //var isSimilarFavorite by remember { mutableStateOf(false) }

    // --- MODIFICATION START (Task D17 - Functionality) ---
    // 使用传入的初始状态初始化本地视觉状态，并用 initial state 作为 key 确保外部刷新时能更新
    var isFavorite by remember(isInitiallyFavorite) { mutableStateOf(isInitiallyFavorite) }
    // --- MODIFICATION END ---

    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp) // 卡片左右外边距
            .fillMaxWidth() // 卡片宽度占满父容器（通常是 LazyColumn 的宽度减去 Padding）
            .clickable { // 使整个卡片可点击以导航
                artist.id?.let { artistId -> // 仅当 ID 存在时导航
                    navController.navigate("detail/$artistId")
                }
            },
        shape = RoundedCornerShape(8.dp), // 卡片圆角
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp) // 设置卡片阴影
    ) {
        // 使用 Box 实现图片和按钮/文字栏的叠加
        Box(
            modifier = Modifier.fillMaxWidth() // Box 宽度占满 Card
        ) {
            // 1. 图片区域 (作为 Box 的背景层)
            val imageModifier = Modifier
                .fillMaxWidth() // 宽度占满
                .aspectRatio(16f / 9f) // 设置图片容器宽高比 (例如 16:9)

            // 条件渲染图片或 Logo
            if (artist.image.isNullOrBlank() || artist.image == "/assets/shared/missing_image.png") {
                // 无图或无效路径时显示 Logo
                Image(
                    painter = painterResource(id = R.drawable.artsy_logo),
                    contentDescription = "Artsy Logo Placeholder",
                    contentScale = ContentScale.Fit, // Fit 保证 Logo 完整显示
                    modifier = imageModifier
                        .background(MaterialTheme.colorScheme.surfaceVariant) // Logo 背景色
                        .padding(16.dp) // 给 Logo 留白，让它居中一些
                )
            } else {
                // 有图时加载网络图片
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(artist.image)
                        .crossfade(true)
                        .placeholder(R.drawable.artsy_logo) // 加载中和失败时显示 Logo
                        .error(R.drawable.artsy_logo)
                        .build(),
                    contentDescription = artist.name ?: "Similar artist image",
                    contentScale = ContentScale.Crop, // Crop 裁剪填充图片区域
                    modifier = imageModifier // 应用定义的 Modifier
                )
            }

            // 2. 收藏按钮 (星星)，叠加在图片右上角

            IconButton(
                onClick = {
                    // --- MODIFICATION START (Task D17 - Functionality) ---
                    val currentState = isFavorite
                    // 1. 乐观更新本地 UI
                    isFavorite = !currentState
                    // 2. 调用回调，将操作前的状态传递给 ViewModel
                    onToggleFavorite(currentState)
                    // --- MODIFICATION END ---
                },
                modifier = Modifier
                    .align(Alignment.TopEnd) // 对齐到 Box 的右上角
                    .padding(8.dp) // 与角落的距离
                    // 给按钮本身添加半透明圆形背景
                    .background(
                        color = Color.Black.copy(alpha = 0.4f), // <<< 尝试半透明黑色背景
                        // color = Color.White.copy(alpha = 0.5f), // 或者半透明白色
                        shape = CircleShape // 圆形
                    )
                    .size(36.dp) // 控制按钮触摸区域和背景大小
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Toggle Favorite",
                    // 图标颜色需要能在半透明背景和各种图片上都可见
                    tint = Color.White, // <<< 尝试用白色图标
                    modifier = Modifier.size(20.dp) // 控制图标本身的大小
                )
            }

            // 3. 底部名字栏 Row，叠加在图片底部
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter) // 对齐到底部
                    .fillMaxWidth() // 宽度占满
                    // 设置半透明背景，颜色接近顶部栏，可调整 alpha
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)) // <<< 半透明背景
                    .padding(horizontal = 16.dp, vertical = 8.dp), // 行内边距
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 艺术家名字
                Text(
                    text = artist.name ?: "Unknown Artist",
                    style = MaterialTheme.typography.titleMedium, // 字体样式
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f), // 占据左侧空间
                    // 确保文字颜色在背景上可见
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // V 形箭头图标 (保持不变)
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "View Details",
                    // 确保图标颜色在背景上可见
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } // End Bottom Row
        } // End Box
    } // End Card
}


// Preview
@Preview(showBackground = true)
@Composable
fun SimilarArtistCardItemPreview() {
    // ... Preview 可以保持，或者传入固定的 isInitiallyFavorite 和空 lambda
    val sampleArtist = SimilarArtist(id="s1", name="Test", image="")
    val navController = rememberNavController()
    Surface{
        SimilarArtistCardItem(
            artist = sampleArtist,
            navController = navController,
            isInitiallyFavorite = false, // 示例初始状态
            onToggleFavorite = {} // 空回调
        )
    }
}