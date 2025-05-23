// file: app/src/main/java/com/example/artsyfrontend/data/network/RetrofitInstance.kt
package com.example.artsyfrontend.data.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// --- 修改：引入 AuthInterceptor，用于统一注入 Bearer Token ---
import com.example.artsyfrontend.data.network.AuthInterceptor

/**
 * RetrofitInstance 负责创建并配置 Retrofit + OkHttpClient 实例。
 * 核心改动：不再使用 CookieJar，会话完全基于存储在 DataStore 中的 JWT Token，
 * 通过 AuthInterceptor 自动添加到每个请求的 Authorization 头。
 */
object RetrofitInstance {

    // 后端 API 的基础 URL（确保结尾带斜杠）
    //private const val BASE_URL = "http://10.0.2.2:3000/api/"
    // google cloud address
    private const val BASE_URL = "https://focal-shape-449106-v9.wl.r.appspot.com/api/"

    /**
     * OkHttpClient 单例。
     * - 添加 AuthInterceptor 在每次请求时注入 "Authorization: Bearer <token>"
     * - 添加 HttpLoggingInterceptor 以打印网络请求日志（Level.BASIC）
     * --- 删除：之前的 PersistentCookieJar 配置已移除 ---
     */
    private val client: OkHttpClient by lazy {
        // (可选) HTTP 日志拦截器，打印 URL、响应码、头信息
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())    // ← **修改**：注入全局 Token
            .addInterceptor(logging)              // ← (可选) 打印基础日志
            .build()
    }

    /**
     * Retrofit 单例。
     * - 使用上面配置好的 OkHttpClient
     * - GsonConverterFactory 用于 JSON 序列化/反序列化
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)                   // 基础 URL
            .client(client)                      // 自定义的 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * 对外暴露的 API 服务实例，用来执行各个网络请求方法。
     */
    val api: BackendApi by lazy {
        retrofit.create(BackendApi::class.java)
    }
}
