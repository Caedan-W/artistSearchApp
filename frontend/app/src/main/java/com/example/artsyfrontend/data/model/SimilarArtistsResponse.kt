// data/model/SimilarArtistsResponse.kt
package com.example.artsyfrontend.data.model

import com.google.gson.annotations.SerializedName

// 对应后端 /api/artist/:id/similar 返回列表中的单项
data class SimilarArtist(
    @SerializedName("id")
    val id: String?, // 艺术家 ID
    @SerializedName("name")
    val name: String?, // 艺术家名字
    @SerializedName("image")
    val image: String? // 艺术家图片 URL
)


// 对应后端 GET /api/artist/:id/similar 返回的 JSON 结构 { "similar": [...] }
data class SimilarArtistsResponse(
    @SerializedName("similar") // 匹配 JSON 中的 "similar" 键
    val artists: List<SimilarArtist> // 包含一个 SimilarArtist 列表
    // 属性名可以叫 artists 或 similar，取决于你想在 Kotlin 中如何称呼它
)