package com.example.artsyfrontend.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.artsyfrontend.data.model.*
import java.time.Duration
import java.time.Instant

/**
 * 收藏艺术家列表
 */
@Composable
fun FavoritesList(
    items: List<Artist>,
    onItemClick: (Artist) -> Unit
) {
    LazyColumn {
        items(items) { artist ->
            FavoriteArtistListItem(artist = artist, onClick = onItemClick)
        }
    }
}

/**
 * 单个艺术家条目
 */
@Composable
fun FavoriteArtistListItem(
    artist: Artist,
    onClick: (Artist) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(artist) }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(artist.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${artist.nationality}, ${artist.birthday}",
                style = MaterialTheme.typography.bodySmall
            )
            // 计算“seconds ago”
            val secondsAgo = Duration.between(artist.addedAt, Instant.now()).seconds
            Text("$secondsAgo seconds ago", style = MaterialTheme.typography.bodySmall)
        }
        Icon(
            Icons.Filled.KeyboardArrowRight,
            contentDescription = "Detail",
            modifier = Modifier.size(24.dp)
        )
    }
}
