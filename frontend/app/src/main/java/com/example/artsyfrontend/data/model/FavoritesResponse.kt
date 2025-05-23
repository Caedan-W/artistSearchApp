// data/model/FavoritesResponse.kt (新建文件)
package com.example.artsyfrontend.data.model

// 这个类匹配后端 GET /api/favorites 返回的 JSON 结构 { "favorites": [...] }
data class FavoritesResponse(
    val favorites: List<FavouriteItem> // 包含一个 FavouriteItem 列表
)