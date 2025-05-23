// file: app/src/main/java/com/example/artsyfrontend/data/datastore/UserPreferenceKeys.kt
package com.example.artsyfrontend.data.datastore

import androidx.datastore.preferences.core.stringPreferencesKey

object UserPreferenceKeys {
    val USER_ID        = stringPreferencesKey("user_id")
    val USER_EMAIL     = stringPreferencesKey("user_email")
    val USER_FULLNAME  = stringPreferencesKey("user_fullname")
    val USER_IMAGE_URL = stringPreferencesKey("user_image_url")
    // --- 修改：新增 Token 键，用于持久化后端颁发的访问令牌 ---
    val AUTH_TOKEN     = stringPreferencesKey("auth_token")
}
