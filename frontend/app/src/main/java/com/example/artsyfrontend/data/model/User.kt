// data/model/User.kt
package com.example.artsyfrontend.data.model

data class User(
    val id: String?, // 后端是 _id，但这里用 id 也可以，Gson 默认能匹配
    val fullname: String?,
    val email: String?,
    val profileImageUrl: String?
    // 根据需要定义字段，确保与后端 /me 接口返回的 JSON 字段匹配
    // 设为可空类型更安全
)

