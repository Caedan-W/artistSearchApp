// file: app/src/main/java/com/example/artsyfrontend/viewmodel/RegisterViewModel.kt
package com.example.artsyfrontend.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import android.util.Patterns
import com.example.artsyfrontend.data.model.RegisterRequest
import com.example.artsyfrontend.repository.ArtistRepository // 假设这是您的 Repository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.ConnectException
import java.net.UnknownHostException
// NEW/ADJUSTED COMMENT: 引入必要的Android工具类和协程库。
// 对于错误处理中的JSON解析，如果后端返回的错误体结构复杂，建议使用专门的JSON库（如 kotlinx.serialization, Moshi, Gson）。
// 此处针对您后端明确的 {"email":"Email already exists"} 响应，采用了精确的字符串匹配。

/**
 * 注册屏幕的UI状态。
 */
data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val fullNameError: String? = null,
    val emailError: String? = null,      // 用于显示邮箱相关的错误，包括客户端验证和服务器端（如 "Email Already Exists"）
    val passwordError: String? = null,
    val apiError: String? = null,        // 用于显示非特定字段的通用API错误
    val fieldInteracted: FieldInteraction = FieldInteraction() // 跟踪用户与字段的交互状态
)

/**
 * 跟踪用户已与之交互的字段，
 * 以避免在用户接触或提交之前过早显示错误。
 */
data class FieldInteraction(
    val fullName: Boolean = false,
    val email: Boolean = false,
    val password: Boolean = false
)

/**
 *驱动 RegisterScreen UI 的 ViewModel。
 */
class RegisterViewModel : ViewModel() {

    // --- UI状态流 ---
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    // --- 一次性事件流 ---
    // 用于通知UI注册成功，以便进行导航
    private val _registrationSuccessEvent = MutableSharedFlow<Unit>() // 使用 SharedFlow 保证事件只被消费一次
    val registrationSuccessEvent: SharedFlow<Unit> = _registrationSuccessEvent.asSharedFlow()

    // 用于显示一次性的 Snackbar 消息
    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()


    // --- 输入变化处理器 ---

    /** 用户在全名输入框中输入时调用 */
    fun onFullNameChange(new: String) {
        _uiState.update {
            it.copy(
                fullName = new,
                fullNameError = null, // 用户开始输入时，清除该字段之前的客户端验证错误
                apiError = null,      // 清除通用的API错误，因为用户正在修改输入
                fieldInteracted = it.fieldInteracted.copy(fullName = true) // 标记该字段已交互
            )
        }
    }

    /** 用户在邮箱输入框中输入时调用 */
    fun onEmailChange(new: String) {
        _uiState.update { currentState ->
            // NEW/ADJUSTED COMMENT: 当用户再次编辑邮箱时，清除之前可能存在的 "Email Already Exists" 服务器错误
            // 或其他本地邮箱格式错误。新的内容将在失去焦点或提交时重新验证。
            val newEmailError = if (currentState.emailError == "Email Already Exists" ||
                !Patterns.EMAIL_ADDRESS.matcher(new).matches() && new.isNotBlank() || // 如果新内容不为空但格式不对，也暂时清除旧错，待失焦时重判
                new.isBlank() && currentState.emailError != null) { // 如果清空了，也清除旧错
                null
            } else {
                currentState.emailError // 保留非上述情况的错误（理论上此时应为null或服务器错误已清除）
            }
            currentState.copy(
                email = new,
                emailError = null, // 主要目标：用户输入时，清除之前的错误状态，失焦时再验证
                apiError = null,
                fieldInteracted = currentState.fieldInteracted.copy(email = true)
            )
        }
    }

    /** 用户在密码输入框中输入时调用 */
    fun onPasswordChange(new: String) {
        _uiState.update {
            it.copy(
                password = new,
                passwordError = null, // 用户开始输入时，清除该字段之前的客户端验证错误
                apiError = null,
                fieldInteracted = it.fieldInteracted.copy(password = true)
            )
        }
    }


    // --- 失去焦点时的验证逻辑 ---

    /** 全名输入框失去焦点时验证 */
    fun onFullNameFocusLost() {
        val s = _uiState.value
        // MODIFICATION: 移除了之前可能存在的 if (s.fieldInteracted.fullName) 条件判断。
        // 现在，只要失去焦点，就执行验证并更新交互状态。
        val error = if (s.fullName.isBlank()) "Full name cannot be empty" else null
        _uiState.update {
            it.copy(
                fullNameError = error,
                fieldInteracted = it.fieldInteracted.copy(fullName = true) // 关键：确保在此处将交互状态设为true
            )
        }
    }

    /** 邮箱输入框失去焦点时验证 */
    fun onEmailFocusLost() {
        val s = _uiState.value
        // MODIFICATION: 移除了之前可能存在的 if (s.fieldInteracted.email) 条件判断。
        // NEW/ADJUSTED COMMENT: 优先保留已从服务器获取的 "Email Already Exists" 错误。
        // 如果没有此类服务器错误，则执行本地客户端验证（是否为空、格式是否正确）。
        val currentError = if (s.emailError == "Email Already Exists") {
            s.emailError // 保留服务器端错误
        } else {
            when {
                s.email.isBlank() -> "Email cannot be empty"
                !Patterns.EMAIL_ADDRESS.matcher(s.email).matches() -> "Invalid email format"
                else -> null // 本地验证通过
            }
        }
        _uiState.update {
            it.copy(
                emailError = currentError,
                fieldInteracted = it.fieldInteracted.copy(email = true) // 关键：确保在此处将交互状态设为true
            )
        }
    }

    /** 密码输入框失去焦点时验证 */
    fun onPasswordFocusLost() {
        val s = _uiState.value
        // MODIFICATION: 移除了之前可能存在的 if (s.fieldInteracted.password) 条件判断。
        val error = if (s.password.isBlank()) "Password cannot be empty" else null
        _uiState.update {
            it.copy(
                passwordError = error,
                fieldInteracted = it.fieldInteracted.copy(password = true) // 关键：确保在此处将交互状态设为true
            )
        }
    }


    // --- 提交前的最终客户端验证 ---
    /**
     * 检查所有输入字段是否通过客户端验证。
     * 如果未通过，则更新uiState以显示相应的字段错误，并确保所有字段都标记为已交互。
     * @return Boolean 返回true表示所有字段均通过客户端验证，否则返回false。
     */
    private fun validateInputs(): Boolean {
        val s = _uiState.value
        var isValid = true

        // 初始化新的错误状态，优先保留已有的服务器错误（特别是针对Email）
        var newFullNameError: String? = if (s.fullNameError != null && s.fullNameError != "Full name cannot be empty") s.fullNameError else null
        var newEmailError: String? = if (s.emailError == "Email Already Exists") s.emailError else null
        var newPasswordError: String? = if (s.passwordError != null && s.passwordError != "Password cannot be empty") s.passwordError else null


        if (s.fullName.isBlank()) {
            newFullNameError = "Full name cannot be empty"
            isValid = false
        }

        // 如果邮箱错误不是 "Email Already Exists"，则进行本地的空检查和格式检查
        if (newEmailError != "Email Already Exists") {
            if (s.email.isBlank()) {
                newEmailError = "Email cannot be empty"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(s.email).matches()) {
                newEmailError = "Invalid email format"
                isValid = false
            }
        } // 如果 newEmailError 是 "Email Already Exists"，isValid 不会因此变为 false，允许提交以便服务器再次确认

        if (s.password.isBlank()) {
            newPasswordError = "Password cannot be empty"
            isValid = false
        }

        // 如果验证未通过，或者任何错误状态发生了变化，则更新UI状态
        // 确保所有字段都被标记为已交互，以便错误信息能够显示
        if (!isValid || newFullNameError != s.fullNameError || newEmailError != s.emailError || newPasswordError != s.passwordError) {
            _uiState.update {
                it.copy(
                    fullNameError = newFullNameError, // 如果 newFullNameError 为 null，则不改变原有的 s.fullNameError (除非原错误是空相关的)
                    emailError = newEmailError,       // 同上
                    passwordError = newPasswordError, // 同上
                    apiError = null, // 客户端验证阶段不应设置apiError
                    fieldInteracted = FieldInteraction(fullName = true, email = true, password = true) // 确保所有字段都已交互
                )
            }
        }
        Log.d("RegisterViewModel", "Client-side input validation result: $isValid. Email Error: ${newEmailError ?: s.emailError}")
        return isValid
    }


    // --- 注册操作 ---
    /**
     * 当用户点击注册按钮时触发。
     * 首先执行客户端输入验证，如果通过，则调用 ArtistRepository.register() 发起网络请求。
     * 处理成功响应：发出 registrationSuccessEvent 事件。
     * 处理错误响应：根据错误类型更新 uiState.emailError（针对邮箱已存在）或 uiState.apiError（其他错误），并发出 snackbarMessage。
     */
    fun register() {
        Log.d("RegisterViewModel", "Attempting registration")

        // 1. 执行客户端输入验证。
        // 如果验证失败，validateInputs() 内部已更新UI状态以显示字段错误，并将字段标记为已交互。
        if (!validateInputs()) {
            return // 客户端验证失败，不继续执行注册
        }

        // 2. 显示加载指示器，并清除之前的通用API错误。
        // 注意：此处不应清除字段特定的错误（如 uiState.emailError），因为 "Email Already Exists"
        // 可能来自上一次尝试，并且仍然相关。validateInputs() 已处理本地验证错误的清除。
        _uiState.update { it.copy(isLoading = true, apiError = null) }

        viewModelScope.launch {
            try {
                val req = RegisterRequest(
                    fullname = _uiState.value.fullName.trim(),
                    email    = _uiState.value.email.trim(),
                    password = _uiState.value.password
                )

                // 调用 Repository 中的注册方法
                // 假设 ArtistRepository.register() 是一个挂起函数，它会处理网络调用和响应
                val resp = ArtistRepository.register(req) // 假设成功时 resp 包含有用的信息，如 resp.message
                Log.i("RegisterViewModel", "Registered successfully: ${resp.message}") // 使用后端返回的成功消息

                // 发出注册成功事件，UI层可以据此进行导航等操作
                _registrationSuccessEvent.emit(Unit)

            } catch (e: Exception) { // 捕获所有类型的异常
                Log.e("RegisterViewModel", "Registration error", e)

                // NEW/ADJUSTED COMMENT: 详细的错误处理逻辑开始
                var specificEmailExistsErrorHandled = false // 标记是否已作为“邮箱已存在”错误处理
                var errorMessageToDisplay: String? = null    // 用于 Snackbar 和/或通用的 apiError

                if (e is HttpException) { // 处理HTTP相关的异常
                    val httpCode = e.code()
                    // 关键：安全地读取errorBody，因为它是一个流，理想情况下只读一次
                    val errorBodyString = try {
                        e.response()?.errorBody()?.string()?.trim() // trim()去除可能的首尾空格
                    } catch (ioe: Exception) {
                        Log.e("RegisterViewModel", "Error reading error body string from HttpException", ioe)
                        null
                    }
                    Log.w("RegisterViewModel", "HttpException caught: Code $httpCode, Body: $errorBodyString")

                    // --- 核心修改：检查是否为“邮箱已存在”的特定错误 ---
                    // 根据您的后端逻辑：成功时返回 HTTP 400 和 JSON 体 {"email": "Email already exists"}
                    var isSpecificEmailExistsError = false
                    if (httpCode == 400 && errorBodyString != null) {
                        val expectedJsonForEmailExists = "{\"email\":\"Email already exists\"}" // 后端返回的确切JSON字符串
                        if (errorBodyString == expectedJsonForEmailExists) {
                            isSpecificEmailExistsError = true
                        }
                        // 可选的更健壮检查（如果不想依赖精确字符串匹配且不想引入完整JSON库）：
                        // if (errorBodyString.contains("\"email\"") && errorBodyString.contains("Email already exists")) {
                        //    isSpecificEmailExistsError = true;
                        // }
                        // 若引入 org.json 库 (在 build.gradle 中添加 implementation("org.json:json:20231013"))，可以这样检查：
                        /*
                        try {
                            val jsonObj = org.json.JSONObject(errorBodyString)
                            if (jsonObj.optString("email") == "Email already exists") {
                                isSpecificEmailExistsError = true
                            }
                        } catch (jsonEx: org.json.JSONException) {
                            Log.w("RegisterViewModel", "Error body was not the expected JSON for email exists: $errorBodyString")
                        }
                        */
                    }

                    if (isSpecificEmailExistsError) {
                        _uiState.update {
                            it.copy(
                                emailError = "Email Already Exists", // 设置特定于字段的错误
                                fieldInteracted = it.fieldInteracted.copy(email = true), // 确保UI上显示此错误
                                apiError = null // 清除通用API错误，因为这是一个特定字段的错误
                            )
                        }
                        errorMessageToDisplay = "该邮箱地址已被注册。" // 用于Snackbar的更友好的消息
                        specificEmailExistsErrorHandled = true
                    } else {
                        // 处理其他类型的 HttpException（例如，后端返回的“All fields are required”或其他HTTP状态码）
                        // 尝试从错误体中提取有意义的消息，特别是如果它是JSON格式
                        var generalHttpErrorMessage = "注册失败 (服务器错误: ${httpCode})" // 默认的HTTP错误消息
                        if (errorBodyString != null && errorBodyString.isNotBlank()) {
                            // 尝试解析常见的 {"error": "message"} 结构
                            if (errorBodyString.startsWith("{\"error\":\"") && errorBodyString.endsWith("\"}")) {
                                try {
                                    // 基本提取方法，更复杂JSON应使用库
                                    generalHttpErrorMessage = errorBodyString.substringAfter("{\"error\":\"").substringBeforeLast("\"}")
                                } catch (subEx: Exception) { /* 提取失败，则使用原始 errorBodyString 或默认消息 */
                                    if (errorBodyString.length < 100) generalHttpErrorMessage = errorBodyString // 如果不太长，用原始的
                                }
                            } else if (errorBodyString.length < 100) { // 如果不是特定结构但不太长，也用原始的
                                generalHttpErrorMessage = errorBodyString
                            }
                        }
                        errorMessageToDisplay = generalHttpErrorMessage
                    }
                    // --- “邮箱已存在”错误处理结束 ---

                } else if (e is ConnectException || e is UnknownHostException) { // 处理网络连接相关的异常
                    errorMessageToDisplay = "网络错误，请检查您的连接。"
                } else { // 处理其他所有类型的未知异常
                    errorMessageToDisplay = "注册过程中发生意外错误。"
                }

                // 如果错误没有被作为“邮箱已存在”的特定错误处理，并且存在要显示的错误消息，
                // 则将其设置为通用的 apiError。
                if (!specificEmailExistsErrorHandled && errorMessageToDisplay != null) {
                    _uiState.update {
                        it.copy(apiError = errorMessageToDisplay)
                    }
                }

                // 如果有任何错误消息，都在 Snackbar 中显示出来。
                if (errorMessageToDisplay != null) {
                    _snackbarMessage.emit(errorMessageToDisplay)
                }

            } finally {
                // 无论成功或失败，最后都要确保加载指示器被清除
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}