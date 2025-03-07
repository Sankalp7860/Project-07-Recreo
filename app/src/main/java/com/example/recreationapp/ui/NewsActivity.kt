package com.example.recreationapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class NewsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                NewsScreen()
            }
        }
    }
}

@Composable
fun NewsScreen(viewModel: AppViewModel = viewModel()) {
    val coroutineScope = rememberCoroutineScope()
    var news by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            news = listOf(
                "Funny Cat Meme: Cat falls off couch!",
                "Hilarious News: Man tries to pay with Monopoly money",
                "Meme: When you realize it's Monday tomorrow"
            )
            viewModel.user.value?.uid?.let { uid ->
                viewModel.addActivity(uid, "Funny News", "Viewed news")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Funny News & Memes", style = MaterialTheme.typography.headlineMedium)
        LazyColumn {
            items(news) { item ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Text(item, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}