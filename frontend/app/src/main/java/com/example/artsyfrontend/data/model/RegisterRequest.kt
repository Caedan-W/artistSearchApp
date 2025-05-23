// data/model/RegisterRequest.kt (新建文件)
package com.example.artsyfrontend.data.model

// 用于 POST /api/auth/register 的请求体
data class RegisterRequest(
    val fullname: String,
    val email: String,
    val password: String
)
