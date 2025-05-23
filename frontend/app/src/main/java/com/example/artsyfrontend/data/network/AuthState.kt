// file: app/src/main/java/com/example/artsyfrontend/data/network/AuthState.kt
package com.example.artsyfrontend.data.network

/**
 * 全局缓存当前登录 Token，供 AuthInterceptor 使用
 */
object AuthState {
    @Volatile
    var token: String? = null
}
