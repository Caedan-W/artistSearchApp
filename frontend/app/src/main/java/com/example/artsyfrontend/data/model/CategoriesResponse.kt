// data/model/CategoriesResponse.kt

package com.example.artsyfrontend.data.model

import com.google.gson.annotations.SerializedName

// 对应后端 /api/artwork/:id/categories 返回的列表中的单项
data class Category(
    @SerializedName("id") // 来自后端 gene.id
    val id: String?,
    @SerializedName("name") // 来自后端 gene.name
    val name: String?,
    // 后端也映射了 image，类型设为可空 String
    @SerializedName("image") // 来自后端 gene._links?.thumbnail?.href
    val imageUrl: String?,

    @SerializedName("description") // 假设后端 JSON 键名也是 description
    val description: String? // <<< 添加 description 字段，设为可空
)


// 对应后端 GET /api/artwork/:id/categories 返回的 JSON 结构 { "categories": [...] }
data class CategoriesResponse(
    val categories: List<Category>
)