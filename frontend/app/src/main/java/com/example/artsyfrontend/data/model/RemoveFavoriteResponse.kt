package com.example.artsyfrontend.data.model

// 用于 DELETE /api/favorites/:artistId 的响应体
// 名称从 MessageResponse 改为 RemoveFavoriteResponse 更具体
data class RemoveFavoriteResponse(
    val message: String? // 字段保持不变
)