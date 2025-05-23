plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.artsyfrontend"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.artsyfrontend"
        minSdk = 34
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        // 【修改1】启用 Core Library Desugaring，以支持 java.time 等新 API
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "11"
        // 下面这一行打开 ExperimentalMaterial3Api 全局 opt-in
        freeCompilerArgs += listOf(
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // 持久化 login data
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("com.google.android.material:material:1.9.0") // For XML Theme Adapters maybe

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // Networking (使用 OkHttp BOM 统一版本 - 假设用 4.9.3)
    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.9.3")) // <<< 添加 OkHttp BOM
    implementation("com.squareup.okhttp3:okhttp")                     // <<< 添加 OkHttp Core (无版本号)
    implementation("com.squareup.okhttp3:logging-interceptor")        // <<< 添加 Logger (无版本号)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.franmontiel:PersistentCookieJar:v1.0.1") // Cookie Jar

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.3.0")

    // Compose (使用 Compose BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended") // Icons Extended
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.compose)


    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}