package com.example.artsyfrontend.ui.screen

import android.util.Log // 用于调试
// 导入布局组件
import androidx.compose.foundation.clickable // 用于注册/登录链接和背景点击
import androidx.compose.foundation.interaction.MutableInteractionSource // 用于移除点击效果
import androidx.compose.foundation.layout.* // Column, Row, Spacer, padding, fillMaxSize, etc.
// 导入输入组件和相关工具
import androidx.compose.foundation.text.ClickableText // 用于登录链接
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 返回箭头
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
// 导入 Material 3 组件
import androidx.compose.material3.* // Scaffold, TopAppBar, OutlinedTextField, Button, Text, Icon, IconButton, CircularProgressIndicator, SnackbarHost, SnackbarHostState, MaterialTheme, etc.
// 导入 Compose 运行时和状态管理
import androidx.compose.runtime.* // Composable, remember, mutableStateOf, collectAsState, LaunchedEffect, getValue, setValue, rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// 导入焦点管理
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
// 导入文本样式相关
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// 导入 ViewModel 和导航
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.artsyfrontend.viewmodel.RegisterViewModel // <<< 导入 RegisterViewModel
import kotlinx.coroutines.flow.collectLatest // 用于监听一次性事件
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay


@OptIn(ExperimentalMaterial3Api::class) // For Scaffold, TopAppBar, OutlinedTextField, Button etc.
@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: RegisterViewModel = viewModel() // 获取 ViewModel 实例 (Task R-A)
) {
    // --- 状态收集 ---
    val uiState by viewModel.uiState.collectAsState() // 收集整体 UI 状态 (R-A)
    var passwordVisible by remember { mutableStateOf(false) } // 本地状态：密码可见性 (R-B)
    val snackbarHostState = remember { SnackbarHostState() } // Snackbar 状态 (R-E)
    val scope = rememberCoroutineScope() // 用于启动 Snackbar 协程 (R-E)
    // 记住焦点状态用于失去焦点验证 (R-C Rev 2)
    var previousFullNameFocusState by remember { mutableStateOf<FocusState?>(null) }
    var previousEmailFocusState by remember { mutableStateOf<FocusState?>(null) }
    var previousPasswordFocusState by remember { mutableStateOf<FocusState?>(null) }
    val focusManager = LocalFocusManager.current // 用于点击背景清除焦点 (L-F Refinement logic applied here too)

    // --- 副作用：处理注册成功事件 --- (Task R-E)
    LaunchedEffect(Unit) {
        //并行执行
        viewModel.registrationSuccessEvent.collectLatest { // <<< 监听 registrationSuccessEvent
            Log.d("RegisterScreen", "Registration success event received.")

            // 1. 立刻启动 Snackbar 显示 (不阻塞)
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Registered successfully", // <<< 修改成功消息
                    duration = SnackbarDuration.Short
                )
            }

            // 2. 等待一小段时间 (例如 800 毫秒)
            delay(800L) // <<< 在导航前延迟

            // 3. 执行导航
            Log.d("RegisterScreen", "Navigating to home after delay.")
            navController.navigate("home") {
                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
        //顺序执行
//        viewModel.registrationSuccessEvent.collectLatest {
//            Log.d("RegisterScreen", "Registration success event received.")
//            // 1. 直接显示 Snackbar 并等待它完成 (或开始消失)
//            snackbarHostState.showSnackbar(
//                message = "Registered successfully",
//                duration = SnackbarDuration.Short // 或 Long
//            )
//            // 2. 添加延迟，让用户有时间看清 Snackbar
//            //delay(1000L) // <<< 等待 1000 毫秒 (1 秒) - 可调整
//            // 3. 执行导航，并清空返回栈
//            Log.d("RegisterScreen", "Navigating to home after delay.")
//            navController.navigate("home") {
//                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
//                launchSingleTop = true
//            }
//        }
        // (可选) 监听通用的 Snackbar 消息 (例如 ViewModel 中 API 错误未设置到具体字段时)
        viewModel.snackbarMessage.collectLatest { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message = message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // (R-E)
        topBar = { // (R-B)
            TopAppBar(
                title = { Text("Register") },
                navigationIcon = { // 返回按钮 (R-E)
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                // 可以设置颜色
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                // 点击背景清除焦点
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { focusManager.clearFocus() }
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Full Name 输入框 --- (Task R-B / R-C)
            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = { viewModel.onFullNameChange(it) },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth().onFocusChanged { currentFocusState -> // 失去焦点验证
                    if (previousFullNameFocusState?.isFocused == true && !currentFocusState.isFocused) { viewModel.onFullNameFocusLost() }
                    previousFullNameFocusState = currentFocusState
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                singleLine = true,
                isError = uiState.fieldInteracted.fullName && uiState.fullNameError != null, // 显示错误状态
                supportingText = {
                    var fullNameError = uiState.fullNameError
                    if (fullNameError != null)
                        Text(fullNameError, color = MaterialTheme.colorScheme.error)
                } // 显示错误文本
            )

            // --- Email 输入框 --- (Task R-B / R-C)
            OutlinedTextField(
                value = uiState.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth().onFocusChanged { currentFocusState -> // 失去焦点验证
                    if (previousEmailFocusState?.isFocused == true && !currentFocusState.isFocused) { viewModel.onEmailFocusLost() }
                    previousEmailFocusState = currentFocusState
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                isError = uiState.fieldInteracted.email && uiState.emailError != null, // 显示错误状态 (会显示格式错误或邮箱已存在)
                supportingText = {
                    var emailError = uiState.emailError
                    if (emailError != null)
                        Text(emailError, color = MaterialTheme.colorScheme.error)
                } // 显示错误文本
            )

            // --- Password 输入框 --- (Task R-B / R-C)
            OutlinedTextField(
                value = uiState.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().onFocusChanged { currentFocusState -> // 失去焦点验证
                    if (previousPasswordFocusState?.isFocused == true && !currentFocusState.isFocused) { viewModel.onPasswordFocusLost() }
                    previousPasswordFocusState = currentFocusState
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), // 密码可见性
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = { // 可见性切换图标
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                    }
                },
                isError = uiState.fieldInteracted.password && uiState.passwordError != null, // 显示错误状态
                supportingText = {
                    var passwordError = uiState.passwordError
                    if (passwordError != null)
                        Text(passwordError, color = MaterialTheme.colorScheme.error)
                } // 显示错误文本
            )

            // --- Register Button --- (Task R-C / R-D)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { viewModel.register() }, // 点击调用 ViewModel 注册
                enabled = !uiState.isLoading, // 加载中禁用
                modifier = Modifier.fillMaxWidth() // 宽度占满
            ) {
                if (uiState.isLoading) { // 显示加载状态
                    CircularProgressIndicator(
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        strokeWidth = 2.dp,
                        color = LocalContentColor.current // 使用按钮内容颜色
                    )
                } else {
                    Text("Register") // 显示按钮文字
                }
            }

            // --- API 错误信息 (Task R-D) ---
            // 使用 Box 预留空间，避免下方链接跳动
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                // 条件显示 API 错误信息 (非字段特定的错误)
                var apiError = uiState.apiError
                if (apiError != null) {
                    Text(
                        text = apiError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // --- Login Link (Task R-E) ---
            // 始终显示登录链接
            LoginLinkTextRegister(navController = navController) // 调用下方定义的函数

        } // End Column
    } // End Scaffold
}


/**
 * 用于显示 "Already have an account? Login" 的可点击文本
 */
@Composable
private fun LoginLinkTextRegister(navController: NavHostController) {
    val annotatedString = buildAnnotatedString {
        append("Already have an account? ") // 提示文字
        pushStringAnnotation(tag = "login_link", annotation = "login") // 标记
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.primary, // 链接样式
            textDecoration = TextDecoration.Underline
        )) {
            append("Login") // 链接文字
        }
        pop()
    }
    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center), // 整体样式
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp), // 增加与上方元素的间距
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "login_link", start = offset, end = offset)
                .firstOrNull()?.let {
                    Log.d("RegisterScreen", "Login link clicked!")
                    // 点击时导航到登录页
                    navController.navigate("login") {
                        // 从注册页返回时，通常希望回到登录页，所以不清栈或只 popUpTo Login
                        // 或者直接 navigateUp 也可以，如果总是从 Login 进入 Register
                        // navController.navigateUp()
                    }
                }
        }
    )
}


// Preview
@Preview(showBackground = true, name = "Register Screen Preview")
@Composable
fun RegisterScreenPreview() {
    val navController = rememberNavController()
    // 在 Preview 中可能需要提供一个假的 ViewModel 或 Theme
    // ArtsyFrontendTheme {
    RegisterScreen(navController = navController)
    // }
}