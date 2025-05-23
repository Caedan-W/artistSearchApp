package com.example.artsyfrontend.data.model

import java.time.Instant

/**
 * 艺术家数据模型
 * @param id 艺术家唯一标识
 * @param name 艺术家姓名
 * @param nationality 国籍
 * @param birthday 生日或出生年份
 * @param addedAt 收藏时间
 */
data class Artist(
    val id: String,
    val name: String,
    val nationality: String,
    val birthday: String,
    val addedAt: Instant
)
