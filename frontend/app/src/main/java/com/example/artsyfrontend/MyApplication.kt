// file: app/src/main/java/com/example/artsyfrontend/MyApplication.kt
package com.example.artsyfrontend

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
// --- 修改：不再需要初始化 RetrofitInstance ---
// import com.example.artsyfrontend.data.network.RetrofitInstance
import com.example.artsyfrontend.repository.ArtistRepository

class MyApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        Log.d("MyApplication", "onCreate called.")

        // --- 修改：删除对 RetrofitInstance.initialize(this) 的调用 ---
        // RetrofitInstance 现在无需显式初始化，拦截器已在单例对象内生效

        // *** 保留：初始化 ArtistRepository ***
        ArtistRepository.initialize(this)
        Log.i("MyApplication", "ArtistRepository initialized.")
    }

    /**
     * Coil ImageLoader 工厂方法 (保持不变)
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .logger(DebugLogger())
            .build()
    }
}
