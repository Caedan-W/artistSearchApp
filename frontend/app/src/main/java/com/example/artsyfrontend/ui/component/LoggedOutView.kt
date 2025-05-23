package com.example.artsyfrontend.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 未登录态下的提示视图，显示“Log in to see favorites”按钮
 */
@Composable
fun LoggedOutView(onLoginClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onLoginClick) {
            Text("Log in to see favorites")
        }
    }
}
