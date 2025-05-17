package com.minusha.lifemate.view

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.minusha.lifemate.ui.theme.LifeMateTheme
import com.minusha.lifemate.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String) -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Collect authentication state
    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthViewModel.AuthState.Error -> {
                val message = (authState as AuthViewModel.AuthState.Error).message
                Log.e("RegisterScreen", "Auth error: $message")
                showError = true
                errorMessage = message
            }
            else -> {
                showError = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Error message
        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                Log.d("RegisterScreen", "Register button clicked with email: $email, name: $name")
                onRegisterClick(name, email, password)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && email.isNotBlank() && password.isNotBlank() &&
                    authState !is AuthViewModel.AuthState.Loading
        ) {
            if (authState is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onLoginClick) {
            Text("Already have an account? Log in")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    LifeMateTheme {
        RegisterScreen(
            onRegisterClick = { _, _, _ -> },
            onLoginClick = { }
        )
    }
}