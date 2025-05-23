// file: app/src/main/java/com/example/artsyfrontend/data/datastore/AppDataStore.kt
package com.example.artsyfrontend.data.datastore

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// 定义 DataStore 委托，名称 "user_prefs" 是文件名
val Context.userDataStore by preferencesDataStore(name = "user_prefs")