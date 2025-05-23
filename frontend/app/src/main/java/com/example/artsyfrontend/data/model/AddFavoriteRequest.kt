// data/model/AddFavoriteRequest.kt (新建文件)
package com.example.artsyfrontend.data.model

// 用于 POST /api/favorites 的请求体
data class AddFavoriteRequest(
    val artistId: String,
    val artistName: String?, // 匹配 FavouriteItem
    val artistImage: String?,
    val nationality: String?,
    val birthday: String?,
    val deathday: String?
)

