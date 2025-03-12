package com.example.recreationapp.ui

import android.content.Intent
import android.os.Bundle
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

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                RegisterScreen()
            }
        }
    }
}

@Composable
fun RegisterScreen(viewModel: AppViewModel = viewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
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
                focusedTextColor = Color.White,      // Text color when field is focused
                unfocusedTextColor = Color.White,    // Text color when field is not focused
                focusedLabelColor = Color.White,     // Label color when focused
                unfocusedLabelColor = Color.Gray,    // Label color when not focused
                cursorColor = Color.White            // Cursor color
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,      // Text color when field is focused
                unfocusedTextColor = Color.White,    // Text color when field is not focused
                focusedLabelColor = Color.White,     // Label color when focused
                unfocusedLabelColor = Color.Gray,    // Label color when not focused
                cursorColor = Color.White            // Cursor color
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }
        Button(
            onClick = {
                viewModel.register(email, password) { success, error ->
                    if (success) {
                        context.startActivity(Intent(context, MainActivity::class.java))
                        (context as? ComponentActivity)?.finish()
                    } else {
                        errorMessage = error ?: "Registration failed"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(16.dp))
        TextButton(
            onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
        ) {
            Text("Already have an account? Login")
        }
    }
}