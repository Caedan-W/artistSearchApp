// data/model/LoginResponse.kt (新建文件)
package com.example.artsyfrontend.data.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    val user: User,
    @SerializedName("token")
    val token: String
)
