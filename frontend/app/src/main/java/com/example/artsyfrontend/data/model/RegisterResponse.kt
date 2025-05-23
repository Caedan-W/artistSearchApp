// data/model/RegisterResponse.kt (新建文件)
package com.example.artsyfrontend.data.model

// 用于 POST /api/auth/register 的成功响应体
// 根据后端代码: res.json({ message: "...", user: { ... } })
data class RegisterResponse(
    val message: String?,
    // 复用 User 模型来表示返回的 user 对象
    val user: User?,
    val token: String?
)