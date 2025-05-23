package com.example.artsyfrontend.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
// --- MODIFICATION START (Task SU7) ---
import androidx.compose.material3.Button // <<< 使用 Button 替换 TextButton
// import androidx.compose.material3.TextButton // 移除 TextButton 导入 (如果用了 Button)
// import androidx.compose.material3.FilledTonalButton // 或者尝试 Tonal 按钮
// import androidx.compose.material3.OutlinedButton // 或者尝试 Outlined 按钮
// --- MODIFICATION END ---
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.artsyfrontend.R
import com.example.artsyfrontend.data.model.Artwork // <<< 导入 Artwork 模型

// --- Task D12: 实现 Artwork 卡片 ---

@Composable
fun ArtworkCardItem(
    artwork: Artwork,
    onViewCategoriesClicked: (artworkId: String) -> Unit // 回调函数，传递 Artwork ID
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 12.dp) // 卡片左右外边距 (LazyColumn 的 spacedBy 提供垂直间距)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 1. 作品图片
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artwork.image)
                    .crossfade(true)
                    .placeholder(R.drawable.artsy_logo) // 使用 Logo 作为占位符
                    .error(R.drawable.artsy_logo)
                    // fallback 对非 null data 无效，error 会处理加载失败
                    .build(),
                contentDescription = artwork.title ?: "Artwork image",
                contentScale = ContentScale.Crop, // 裁剪填满
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 12f) // 设置 9:12 的宽高比
            )

            // --- MODIFICATION START (Task SU7) ---
            // 2. 组合标题和日期
            val displayTitle = buildString {
                append(artwork.title ?: "Untitled") // 添加标题，或默认"Untitled"
                // 如果日期存在且非空，添加日期
                if (!artwork.date.isNullOrBlank()) {
                    append(", ${artwork.date}") // 用逗号和空格分隔
                }
            }

            Text(
                text = displayTitle, // 显示组合后的字符串
                style = MaterialTheme.typography.titleMedium, // 可以调整样式
                fontWeight = FontWeight.SemiBold, // 可以尝试 SemiBold
                maxLines = 2, // 允许最多两行以防万一
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth() // <<< 让 Text 占满宽度以应用居中
            )


            // 3. 查看分类按钮 (居中)
            Button( // <<< 使用 Button (或 FilledTonalButton / OutlinedButton)
                onClick = {
                    // 点击时调用回调，传递 artwork ID
                    onViewCategoriesClicked(artwork.id) // 假设 artwork.id 始终有效
                },
                // 让按钮在 Column 内水平居中
                modifier = Modifier
                    .align(Alignment.CenterHorizontally) // <<< 居中对齐
                    .padding(bottom = 8.dp, top = 4.dp) // 调整上下边距
            ) {
                Text("View categories")
            }
            // --- MODIFICATION END ---
        } // End Column
    } // End Card
}

// Preview
@Preview(showBackground = true)
@Composable
fun ArtworkCardItemPreview() {
    val sampleArtwork = Artwork(
        id = "sample-artwork-id",
        title = "Artwork Title Example That Might Be Long",
        date = "ca. 1900",
        image = "" // Use empty string to test placeholder/error
    )
    // ArtsyFrontendTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
        ArtworkCardItem(artwork = sampleArtwork, onViewCategoriesClicked = {})
    }
    // }
}

@Preview(showBackground = true)
@Composable
fun ArtworkCardItemNoDatePreview() {
    val sampleArtwork = Artwork(
        id = "sample-artwork-id-2",
        title = "Untitled Artwork",
        date = null, // 测试日期为空
        image = ""
    )
    // ArtsyFrontendTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
        ArtworkCardItem(artwork = sampleArtwork, onViewCategoriesClicked = {})
    }
    // }
}