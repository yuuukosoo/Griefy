package com.naufal.griefy.ui.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.naufal.griefy.util.scaled
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.naufal.griefy.domain.util.Resource

import com.naufal.griefy.ui.components.ErrorBanner

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showErrorResId by remember { mutableStateOf<Int?>(null) }
    var showErrorRawMsg by remember { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsState()
    val registerState = uiState.registerState
    val context = LocalContext.current

    val successMessage = stringResource(id = R.string.register_success_toast)

    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                Toast.makeText(context, successMessage, Toast.LENGTH_LONG).show()
                navController.navigateUp()
                viewModel.resetState()
            }
            is Resource.Error -> {
                val msg = registerState.message
                if (msg != null) {
                    when (msg) {
                        "ERROR_ALL_FIELDS_REQUIRED" -> showErrorResId = R.string.error_all_fields_required
                        "ERROR_PASSWORD_MISMATCH" -> showErrorResId = R.string.error_password_mismatch
                        "ERROR_PASSWORD_TOO_SHORT" -> showErrorResId = R.string.error_password_too_short
                        "ERROR_REGISTRATION_FAILED" -> showErrorResId = R.string.error_registration_failed
                        "ERROR_REGISTER_FAILED" -> showErrorResId = R.string.error_register_failed
                        else -> showErrorRawMsg = msg
                    }
                } else {
                    showErrorResId = R.string.error_register_failed
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
                text = stringResource(R.string.register_sign_up),
                fontSize = 32.sp.scaled(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp.scaled()))

            // Username input field group
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.register_username),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp.scaled()
                )
                Spacer(modifier = Modifier.height(4.dp.scaled()))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
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

            Spacer(modifier = Modifier.height(12.dp.scaled()))

            // Email input field group
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.register_email),
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

            Spacer(modifier = Modifier.height(12.dp.scaled()))

            // Password input field group
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.register_password),
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

            Spacer(modifier = Modifier.height(12.dp.scaled()))

            // Confirm Password input field group
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.register_confirm_password),
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp.scaled()
                )
                Spacer(modifier = Modifier.height(4.dp.scaled()))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp.scaled()),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
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

            Spacer(modifier = Modifier.height(24.dp.scaled()))

            Button(
                onClick = {
                    viewModel.register(name, email, password, confirmPassword)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp.scaled()),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = registerState !is Resource.Loading
            ) {
                Text(
                    text = stringResource(R.string.register_button),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp.scaled()
                )
            }

            Spacer(modifier = Modifier.height(24.dp.scaled()))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.register_already_have_account),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp.scaled(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp.scaled()))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = stringResource(R.string.register_login_here),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp.scaled(),
                        modifier = Modifier.clickable {
                            if (registerState !is Resource.Loading) {
                                navController.navigateUp()
                            }
                        }
                    )
                }
            }
        }

        if (registerState is Resource.Loading) {
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
    }
}