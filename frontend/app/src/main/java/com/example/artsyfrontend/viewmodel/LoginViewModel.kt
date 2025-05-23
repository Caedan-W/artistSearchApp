// viewmodel/LoginViewModel.kt (修改)
package com.example.artsyfrontend.viewmodel

import retrofit2.HttpException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import java.lang.Exception
// 导入 Android 工具类进行 Email 格式验证
import android.util.Patterns
import com.example.artsyfrontend.data.model.LoginRequest
import com.example.artsyfrontend.repository.ArtistRepository

// UI 状态数据类 (保持不变)
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val apiError: String? = null,
    val emailInteracted: Boolean = false, // <<< 新增：Email 字段是否失去过焦点
    val passwordInteracted: Boolean = false // <<< 新增：Password 字段是否失去过焦点
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    private val _loginSuccessEvent = MutableSharedFlow<Unit>()
    val loginSuccessEvent: SharedFlow<Unit> = _loginSuccessEvent.asSharedFlow()
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()


    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, emailError = null, apiError = null) }
    }

    fun onPasswordChange(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, passwordError = null, apiError = null) }
    }

    /**
     * 当 Email 字段失去焦点时，进行非空和格式验证
     */
    fun onEmailFocusLost() {
        val currentEmail = _uiState.value.email
        var error: String? = null
        if (currentEmail.isBlank()) {
            error = "Email cannot be empty"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches()) {
            // <<< 在失去焦点时也检查格式 >>>
            error = "Invalid email format"
        }
        _uiState.update { it.copy(emailInteracted = true, emailError = error) }
        Log.d("LoginViewModel", "Email focus lost. email='${currentEmail}', error='$error'")
    }

    /**
     * 当 Password 字段失去焦点时，仅进行非空验证
     */
    fun onPasswordFocusLost() {
        val currentPassword = _uiState.value.password
        val error = if (currentPassword.isBlank()) "Password cannot be empty" else null
        _uiState.update { it.copy(passwordInteracted = true, passwordError = error) }
        Log.d("LoginViewModel", "Password focus lost. error='$error'")
    }

    // --- MODIFICATION START (Task L-C) ---
    /**
     * 验证输入字段
     * @return Boolean: true 如果所有输入都有效，false 如果有任何一个无效
     */
    private fun validateInputs(): Boolean {
        val currentState = _uiState.value
        var isValid = true
        var emailErrorMessage: String? = null
        var passwordErrorMessage: String? = null

        // 1. 验证 Email
        if (currentState.email.isBlank()) {
            emailErrorMessage = "Email cannot be empty"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(currentState.email).matches()) { // 使用 Android 内建工具验证格式
            emailErrorMessage = "Invalid email format"
            isValid = false
        }

        // 2. 验证 Password
        if (currentState.password.isBlank()) {
            passwordErrorMessage = "Password cannot be empty"
            isValid = false
        }
        // 可以添加其他密码规则，例如最小长度
        // else if (currentState.password.length < 6) {
        //     passwordErrorMessage = "Password must be at least 6 characters"
        //     isValid = false
        // }

        // 3. 更新 UI 状态以显示错误（如果存在）
        // 即使 isValid 为 true，也要用 null 更新错误状态以清除旧错误
        _uiState.update {
            it.copy(
                emailError = emailErrorMessage,
                passwordError = passwordErrorMessage,
                apiError = null // 清除之前的 API 错误
            )
        }
        Log.d("LoginViewModel", "Validation result: isValid=$isValid, emailError=$emailErrorMessage, passwordError=$passwordErrorMessage")
        return isValid
    }
    // --- MODIFICATION END ---


    fun login() {
        Log.d("LoginViewModel", "Login attempt started.")
        // --- MODIFICATION START (Task L-C) ---
        // 1. 在发起 API 请求前先验证输入
        if (!validateInputs()) {
            Log.d("LoginViewModel", "Input validation failed. Aborting login.")
            return // 验证失败，直接返回，不继续执行
        }
        // --- MODIFICATION END ---

        // 2. (Task L-D) 开始执行异步登录操作
        _uiState.update { it.copy(isLoading = true, apiError = null) }
        Log.d("LoginViewModel", "Set isLoading = true. Attempting API call...")

        viewModelScope.launch {
            // 3. (Task L-D) 调用 Repository
            try {
                // --- MODIFICATION START (Task L-D) ---
                // 4. 创建登录请求体
                val request = LoginRequest(
                    email = _uiState.value.email.trim(), // 去除前后空格
                    password = _uiState.value.password // 密码通常不需要 trim
                )
                // 5. 调用 Repository 的 login 方法
                val response = ArtistRepository.login(request) // 假设返回 LoginResponse

                // 6. 处理成功响应
                Log.i("LoginViewModel", "Login API call successful for user: ${response.user.email}")
                // 在这里可以处理用户信息，例如保存到某个地方，但对于这个任务，我们只发送成功事件
                _loginSuccessEvent.emit(Unit) // <<< 发送登录成功事件给 UI
                // 成功后不需要重置 isLoading，finally 会处理

                // --- MODIFICATION END ---
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Login API call failed", e)
                val errorMessage = when (e) {
                    // 后端对于无效凭据返回 400 和 { password: "..." }
                    is HttpException -> when (e.code()) {
                        400 -> "Username or password is incorrect." // <<< 使用要求的文本
                        else -> "Login failed (${e.code()})."
                    }
                    is java.net.ConnectException, is java.net.UnknownHostException -> "Connection error."
                    else -> "An unknown error occurred."
                }
                // 更新 UI State 中的 apiError
                _uiState.update { it.copy(apiError = errorMessage) } // <<< 更新 apiError
            } finally {
                // --- MODIFICATION START (Task L-D) ---
                // 8. 无论成功或失败，最终都要结束加载状态
                _uiState.update { it.copy(isLoading = false) }
                Log.d("LoginViewModel", "Login attempt finished. isLoading = false")
                // --- MODIFICATION END ---
            }
        } // End viewModelScope.launch
    } // End login()
}