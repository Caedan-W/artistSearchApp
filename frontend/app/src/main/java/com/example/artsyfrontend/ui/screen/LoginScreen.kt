// file: app/src/main/java/com/example/artsyfrontend/ui/screen/LoginScreen.kt
package com.example.artsyfrontend.ui.screen

// 导入基础布局和组件
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.clickable // 导入 clickable
// 导入图标
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 返回箭头
import androidx.compose.material.icons.filled.Visibility        // 可见图标
import androidx.compose.material.icons.filled.VisibilityOff     // 不可见图标
// 导入状态管理
import androidx.compose.runtime.* // remember, mutableStateOf, collectAsState, getValue, setValue, rememberCoroutineScope
// 导入键盘和输入相关
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation // 密码隐藏
import androidx.compose.ui.text.input.VisualTransformation // 可见性切换
import androidx.compose.ui.text.style.TextDecoration // 导入下划线
// 导入其他 UI 工具
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// 导入 ViewModel 和导航
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController // 确保导入
import androidx.navigation.compose.rememberNavController
import com.example.artsyfrontend.viewmodel.LoginViewModel // 导入 LoginViewModel

import kotlinx.coroutines.flow.collectLatest // 导入 collectLatest
import kotlinx.coroutines.launch // 需要 launch
// 导入 AnnotatedString 相关
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
// 导入 onFocusChanged 和 FocusState
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
// 导入焦点管理
import androidx.compose.foundation.interaction.MutableInteractionSource // 导入 InteractionSource
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.platform.LocalFocusManager // 导入 LocalFocusManager
import androidx.compose.ui.text.style.TextAlign
// 移除 delay 导入
// import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    // 收集 ViewModel 的 UI 状态
    val uiState by viewModel.uiState.collectAsState()

    // 本地状态，用于控制密码输入框的可见性
    var passwordVisible by remember { mutableStateOf(false) }

    // 移除 LoginScreen 自己的 SnackbarHostState，因为它不再在此屏幕显示 Snackbar
    // val snackbarHostState = remember { SnackbarHostState() }

    // 获取当前 Composable 的 CoroutineScope (如果其他地方还需要)
    // val scope = rememberCoroutineScope() // 如果不再需要 scope.launch，可以移除

    // 记住 Email 和 Password 上次的焦点状态
    var previousEmailFocusState by remember { mutableStateOf<FocusState?>(null) }
    var previousPasswordFocusState by remember { mutableStateOf<FocusState?>(null) }

    // 获取焦点管理器实例
    val focusManager = LocalFocusManager.current

    // 监听登录成功事件，用于设置结果并导航
    LaunchedEffect(Unit) {
        viewModel.loginSuccessEvent.collectLatest {
            Log.d("LoginScreen", "Login success event received.")

            // 1. 设置导航结果，通知 HomeScreen 登录成功
            navController.previousBackStackEntry?.savedStateHandle?.set("login_success", true)
            Log.d("LoginScreen", "Set login_success result for previous back stack entry.")

            // 2. 执行导航到 HomeScreen，并从后台堆栈中移除 LoginScreen
            Log.d("LoginScreen", "Navigating to home and popping login screen.")
            navController.navigate("home") {
                // 从导航图中找到 "login" 路由并将其弹出 (inclusive = true 表示包括自身)
                popUpTo("login") { inclusive = true }
                // 如果 HomeScreen 已在堆栈顶部，避免重复创建
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        // 移除 LoginScreen 的 SnackbarHost
        // snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            // 顶部应用栏
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->

        // 表单内容的垂直布局 Column
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .clickable( // 点击空白处清除焦点
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusManager.clearFocus()
                }
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 1. Email 输入框
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { currentFocusState -> // 失去焦点时验证
                        if (previousEmailFocusState?.isFocused == true && !currentFocusState.isFocused) {
                            viewModel.onEmailFocusLost()
                        }
                        previousEmailFocusState = currentFocusState
                    },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = {
                    uiState.emailError?.let { // 显示错误信息
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // 2. Password 输入框
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { currentFocusState -> // 失去焦点时验证
                        if (previousPasswordFocusState?.isFocused == true && !currentFocusState.isFocused) {
                            viewModel.onPasswordFocusLost()
                        }
                        previousPasswordFocusState = currentFocusState
                    },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = { // 密码可见性切换图标
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                isError = uiState.passwordError != null,
                supportingText = {
                    uiState.passwordError?.let { // 显示错误信息
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // 间距
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Login Button
            Button(
                onClick = { viewModel.login() },
                enabled = !uiState.isLoading, // 加载时禁用
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) { // 显示加载指示器
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login") // 显示登录文本
                }
            }

            // 显示 API 错误信息（如果有）
            val apiError = uiState.apiError
            if (apiError != null) {
                Text(
                    text = apiError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp)) // 错误和注册链接间距
            } else {
                Spacer(modifier = Modifier.height(16.dp)) // 按钮和注册链接间距
            }

            // 注册链接
            val annotatedString = buildAnnotatedString {
                append("Don't have an account yet? ")
                pushStringAnnotation(tag = "register_link", annotation = "register")
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                    append("Register")
                }
                pop()
            }
            ClickableText(
                text = annotatedString,
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth(),
                onClick = { offset ->
                    annotatedString.getStringAnnotations(tag = "register_link", start = offset, end = offset)
                        .firstOrNull()?.let { navController.navigate("register") } // 点击 "Register" 导航
                }
            )

        } // End Column
    } // End Scaffold
}

// Preview
@Preview(showBackground = true, name = "Login Screen Preview")
@Composable
fun LoginScreenPreview() {
    val navController = rememberNavController()
    LoginScreen(navController = navController)
}