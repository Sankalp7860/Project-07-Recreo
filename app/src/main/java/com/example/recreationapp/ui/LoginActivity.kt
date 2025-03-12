package com.example.recreationapp.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.viewmodel.AppViewModel

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LoginScreen()
            }
        }
    }
}
@Composable
fun LoginScreen(viewModel: AppViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.Gray,
                cursorColor = Color.White
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                when {
                    email.isBlank() -> {
                        errorMessage = "Email cannot be empty"
                        showErrorDialog = true
                    }
                    password.isBlank() -> {
                        errorMessage = "Password cannot be empty"
                        showErrorDialog = true
                    }
                    else -> {
                        Log.d("LoginScreen", "Calling login with: $email")
                        viewModel.login(email, password) { success, error ->
                            Log.d("LoginScreen", "Login callback: success=$success, error=$error")
                            if (success) {
                                showSuccessDialog = true
                            } else {
                                errorMessage = error
                                showErrorDialog = true
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = {
                context.startActivity(Intent(context, RegisterActivity::class.java))
            }
        ) {
            Text("Don't have an account? Register")
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Success") },
            text = { Text("Login successful!") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    context.startActivity(Intent(context, MainActivity::class.java))
                    (context as? ComponentActivity)?.finish()
                }) {
                    Text("OK")
                }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage ?: "Unknown error") },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}