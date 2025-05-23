package com.example.artsyfrontend.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController // Preview 需要
import androidx.navigation.compose.rememberNavController // Preview 需要
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.artsyfrontend.R
import com.example.artsyfrontend.data.model.Category


@Composable
fun CategoryCardItem(category: Category) {
    Card(
        modifier = Modifier
            // --- MODIFICATION START (Task SU12 Rev 4) ---
            // 保持宽度 (如果之前设置了固定宽度，例如 150.dp，就保留它，否则让 Pager 控制)
            // .width(150.dp)
            .fillMaxWidth() // 或者让它填满 Pager 页面宽度 (减去 Padding)
            // 设置一个固定的、较大的高度
            .height(380.dp), // <<< 设置固定高度，例如 380.dp (需要调整)
            // --- MODIFICATION END ---
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        // Column 填满 Card
        Column(modifier = Modifier.fillMaxSize()) { // 高度现在由 Card 的固定高度决定
            // 1. 图片区域
            val imageModifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f) // 保持宽高比

            if (category.imageUrl.isNullOrBlank() || category.imageUrl == "/default-category.png") {
                Image(
                    painter = painterResource(id = R.drawable.artsy_logo),
                    contentDescription = "Artsy Logo Placeholder",
                    contentScale = ContentScale.Fit,
                    modifier = imageModifier
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp)
                )
            } else {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(category.imageUrl)
                        .crossfade(true)
                        .placeholder(R.drawable.artsy_logo)
                        .error(R.drawable.artsy_logo)
                        .build(),
                    contentDescription = category.name ?: "Category image",
                    contentScale = ContentScale.Crop,
                    modifier = imageModifier
                )
            }

            // 2. 分类名称
            Text(
                text = category.name ?: "Unknown",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(top = 8.dp, start = 12.dp, end = 12.dp, bottom = 4.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            // 3. 分类描述
            if (!category.description.isNullOrBlank()) {
                Text(
                    text = category.description,
                    style = MaterialTheme.typography.bodySmall,
                    //maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // --- MODIFICATION START (Task SU12 Rev 4) ---
            // 添加带权重的 Spacer，将内容推向顶部，占据所有剩余垂直空间
            Spacer(modifier = Modifier.weight(1f))
            // --- MODIFICATION END ---

        } // End Column
    } // End Card
}


// Preview
@Preview(showBackground = true)
@Composable
fun CategoryCardItemPreview() {
    val sampleCategory = Category(
        id="1",
        name="1860-1969",
        imageUrl = "",
        description = "All art, design, decorative art, and architecture produced from roughly 1860 to 1970."
    )
    MaterialTheme {
        Surface(color = Color.LightGray, modifier = Modifier.padding(8.dp)) {
            // 在 Preview 中模拟固定尺寸
            Box(modifier = Modifier.width(150.dp).height(380.dp)) {
                CategoryCardItem(category = sampleCategory)
            }
        }
    }
}