package com.example.artsyfrontend.data.model

import com.google.gson.annotations.SerializedName // 如果字段名需要映射，需要导入

/**
 * 数据类，用于 GSON 解析从后端 /api/artist/:id 获取到的艺术家详情 JSON 数据
 * 字段设为可空类型 (String?) 以增加对 API 可能返回 null 的健壮性
 */
data class ArtistDetail(
    // 使用 @SerializedName 可以确保 JSON 字段名与 Kotlin 属性名正确映射
    // 如果后端返回的 JSON 键名与 Kotlin 变量名完全相同，可以省略 @SerializedName
    @SerializedName("id")
    val id: String?,

    @SerializedName("name")
    val name: String?, // 艺术家名字

    @SerializedName("nationality")
    val nationality: String?, // 国籍

    @SerializedName("birthday")
    val birthday: String?, // 出生年份

    @SerializedName("deathday")
    val deathday: String?, // 逝世年份

    @SerializedName("biography")
    val biography: String?, // 艺术家简介

    // 后端 JSON 中是 'image' 字段
    @SerializedName("image")
    val imageUrl: String? // 艺术家图片 URL
)