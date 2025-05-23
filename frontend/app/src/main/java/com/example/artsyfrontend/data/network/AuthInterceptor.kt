// file: app/src/main/java/com/example/artsyfrontend/data/network/AuthInterceptor.kt
package com.example.artsyfrontend.data.network

import okhttp3.Interceptor
import okhttp3.Response

/**
 * 在每个请求中自动添加 Authorization: Bearer <token> 头
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val t = AuthState.token
        return if (!t.isNullOrBlank()) {
            chain.proceed(
                original.newBuilder()
                    .addHeader("Authorization", "Bearer $t")  // --- 修改：注入 Token 头 ---
                    .build()
            )
        } else {
            chain.proceed(original)
        }
    }
}
