package com.example.recreationapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.viewmodel.AppViewModel

class JournalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                JournalScreen()
            }
        }
    }
}

@Composable
fun JournalScreen(viewModel: AppViewModel = viewModel()) {
    var journalText by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Daily Journal", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = journalText,
            onValueChange = { journalText = it },
            label = { Text("Write your thoughts here...") },
            modifier = Modifier.fillMaxWidth().weight(1f)
        )
        Button(onClick = {
            if (journalText.isNotEmpty()) {
                viewModel.user.value?.uid?.let { uid ->
                    viewModel.addActivity(uid, "Daily Journal", journalText)
                    (context as? ComponentActivity)?.finish()
                }
            }
        }) {
            Text("Save")
        }
    }
}