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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.navigation.NavController
import com.naufal.griefy.R
import com.naufal.griefy.ui.components.ErrorBanner
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import com.naufal.griefy.util.scaled

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    val forgotPasswordState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val successMsg = stringResource(id = R.string.forgot_password_success)
    val horizontalPadding = getAdaptiveHorizontalPadding()

    // Observe success state
    LaunchedEffect(forgotPasswordState.isSuccess) {
        if (forgotPasswordState.isSuccess) {
            Toast.makeText(context, successMsg, Toast.LENGTH_LONG).show()
            viewModel.resetState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding, vertical = 32.dp.scaled()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Griefy
                Box(
                    modifier = Modifier
                        .size(120.dp.scaled())
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logogriefy),
                        contentDescription = stringResource(id = R.string.app_name),
                        modifier = Modifier.size(72.dp.scaled())
                    )
                }

                Spacer(modifier = Modifier.height(32.dp.scaled()))

                // Title
                Text(
                    text = stringResource(id = R.string.forgot_password_title),
                    fontSize = 26.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(16.dp.scaled()))

                // Description text
                Text(
                    text = stringResource(id = R.string.forgot_password_subtitle),
                    fontSize = 15.sp.scaled(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp.scaled())
                )

                Spacer(modifier = Modifier.height(32.dp.scaled()))

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

                Spacer(modifier = Modifier.weight(1f))
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
}
