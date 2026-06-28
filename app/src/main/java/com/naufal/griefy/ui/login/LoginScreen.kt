package com.naufal.griefy.ui.login
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.naufal.griefy.R
import com.naufal.griefy.ui.navigation.Screen
import com.naufal.griefy.util.scaled
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.naufal.griefy.domain.util.Resource
import com.naufal.griefy.ui.components.ErrorBanner
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showErrorResId by remember { mutableStateOf<Int?>(null) }
    var showErrorRawMsg by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordSheet by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    val loginState = uiState.loginState
    val context = LocalContext.current
    val successMessage = stringResource(id = R.string.login_success_toast)
    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
                viewModel.resetState()
            }
            is Resource.Error -> {
                val msg = loginState.message
                if (msg != null) {
                    when (msg) {
                        "ERROR_EMAIL_PASSWORD_EMPTY" -> showErrorResId = R.string.error_email_password_empty
                        "ERROR_USER_NOT_FOUND" -> showErrorResId = R.string.error_user_not_found
                        "ERROR_LOGIN_FAILED" -> showErrorResId = R.string.error_login_failed
                        else -> showErrorRawMsg = msg
                    }
                } else {
                    showErrorResId = R.string.error_login_failed
                }
                viewModel.resetState()
            }
            else -> {}
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 64.dp.scaled(), vertical = 24.dp.scaled())
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.login_sign_in),
                fontSize = 32.sp.scaled(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp.scaled()))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.login_email),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp.scaled()
                )
                Spacer(modifier = Modifier.height(4.dp.scaled()))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp.scaled()),
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp.scaled()),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(16.dp.scaled()))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.login_password),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp.scaled()
                )
                Spacer(modifier = Modifier.height(4.dp.scaled()))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp.scaled()),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp.scaled()),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp.scaled()))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = stringResource(R.string.login_forgot_password),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp.scaled(),
                    modifier = Modifier.clickable {
                        showForgotPasswordSheet = true
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp.scaled()))
            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp.scaled()),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = loginState !is Resource.Loading
            ) {
                Text(
                    text = stringResource(R.string.login_button),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp.scaled()
                )
            }
            Spacer(modifier = Modifier.height(24.dp.scaled()))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.login_no_account),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp.scaled()
                )
                Text(
                    text = stringResource(R.string.login_sign_up),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp.scaled(),
                    modifier = Modifier.clickable {
                        if (loginState !is Resource.Loading) {
                            navController.navigate(Screen.Register.route)
                        }
                    }
                )
            }
        }
        if (loginState is Resource.Loading) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        val displayErrorMsg = if (showErrorResId != null || showErrorRawMsg != null) {
            showErrorResId?.let { stringResource(id = it) } ?: showErrorRawMsg ?: ""
        } else ""
        ErrorBanner(
            message = displayErrorMsg,
            visible = displayErrorMsg.isNotEmpty(),
            onDismiss = {
                showErrorResId = null
                showErrorRawMsg = null
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
        if (showForgotPasswordSheet) {
            ModalBottomSheet(
                onDismissRequest = { showForgotPasswordSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
                containerColor = MaterialTheme.colorScheme.background,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                com.naufal.griefy.ui.forgotpassword.ForgotPasswordScreen(
                    onDismiss = { showForgotPasswordSheet = false }
                )
            }
        }
    }
}