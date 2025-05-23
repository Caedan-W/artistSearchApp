// data/model/FavouriteItem.kt
package com.example.artsyfrontend.data.model

import com.google.gson.annotations.SerializedName // <<< 导入 SerializedName

//此类现在更准确地反映了从后端接收的数据结构（来自 Mongoose schema）
data class FavouriteItem(

    // 使用 @SerializedName 将 JSON 中的 "artistId" 映射到 Kotlin 中的 "id" 属性
    @SerializedName("artistId")
    val id: String, // artistId 在后端是 String

    // 将 "artistName" 映射到 "name"
    @SerializedName("artistName")
    val name: String,

    // --- MODIFICATION START ---
    // 添加 artistImage 字段
    @SerializedName("artistImage")
    val image: String?, // 对应后端的 artistImage, 设为可空更安全
    // --- MODIFICATION END ---

    val nationality: String?, // 与后端匹配
    val birthday: String?,    // 与后端匹配

    // --- MODIFICATION START ---
    // 添加 deathday 字段
    val deathday: String?,    // 对应后端的 deathday, 默认为 null
    // --- MODIFICATION END ---

    // --- MODIFICATION START ---
    // 接收原始的 addedAt (很可能是 ISO 8601 格式的字符串)
    // 不再期望预先格式化的 "X time ago"
    // 字段名改为 addedAt 以匹配后端 Schema
    val addedAt: String? // 后端是 Date, Gson 默认会序列化为 String
    // --- MODIFICATION END ---

    // val userId: String? // userId 通常不需要在 App UI 中直接使用
)