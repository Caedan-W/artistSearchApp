// data/model/LoginRequest.kt (新建文件)
package com.example.artsyfrontend.data.model

// 用于 POST /api/auth/login 的请求体
data class LoginRequest(
    val email: String,
    val password: String
)