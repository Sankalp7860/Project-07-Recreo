package com.example.recreationapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.viewmodel.AppViewModel

@Composable
fun CommunityScreen(viewModel: AppViewModel) {
    val activities by viewModel.activities.observeAsState(initial = emptyList())
    var postText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Community Sharing", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = postText,
            onValueChange = { postText = it },
            label = { Text("Share something...") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            if (postText.isNotEmpty()) {
                viewModel.user.value?.uid?.let { uid ->
                    viewModel.addActivity(uid, "Community Sharing", postText)
                    postText = ""
                }
            }
        }) {
            Text("Post")
        }
        LazyColumn {
            items(activities.filter { it.activityType == "Community Sharing" }) { post ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Text(post.content, modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}