package com.naufal.griefy.ui.forgotpassword

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naufal.griefy.R
import com.naufal.griefy.ui.components.ErrorBanner
import com.naufal.griefy.util.scaled

@Composable
fun ForgotPasswordScreen(
    onDismiss: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val forgotPasswordState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val successMsg = stringResource(id = R.string.forgot_password_success)

    // Observe success state
    LaunchedEffect(forgotPasswordState.isSuccess) {
        if (forgotPasswordState.isSuccess) {
            Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
            viewModel.resetState()
            onDismiss()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp.scaled())
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo Griefy
            Box(
                modifier = Modifier
                    .size(100.dp.scaled())
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logogriefy),
                    contentDescription = stringResource(id = R.string.app_name),
                    modifier = Modifier.size(60.dp.scaled())
                )
            }

            Spacer(modifier = Modifier.height(24.dp.scaled()))

            // Title
            Text(
                text = stringResource(id = R.string.forgot_password_title),
                fontSize = 22.sp.scaled(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp.scaled()))

            // Description text
            Text(
                text = stringResource(id = R.string.forgot_password_subtitle),
                fontSize = 14.sp.scaled(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp.scaled()))

            // Email Input
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp.scaled()),
                singleLine = true,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.forgot_password_email_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 14.sp.scaled()
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                textStyle = TextStyle(fontSize = 14.sp.scaled()),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp.scaled()))

            // Continue Button
            Button(
                onClick = { viewModel.sendPasswordReset(email) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp.scaled()),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !forgotPasswordState.isLoading
            ) {
                if (forgotPasswordState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp.scaled())
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.forgot_password_btn_continue),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp.scaled()
                    )
                }
            }
        }

        // Error display
        val displayErrorMsg = forgotPasswordState.errorMessageRes?.let { stringResource(id = it) }
            ?: forgotPasswordState.errorMessage
            ?: ""

        ErrorBanner(
            message = displayErrorMsg,
            visible = displayErrorMsg.isNotEmpty(),
            onDismiss = {
                viewModel.resetState()
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}
