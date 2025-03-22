package com.example.recreationapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment // Add this import
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.recreationapp.model.ActivityRecord
import com.example.recreationapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AppViewModel) {
    val activities by viewModel.activities.observeAsState(initial = emptyList())
    val user by viewModel.user.observeAsState(initial = null)
    val isAdmin = user?.isAdmin == true
    var contentType by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Admin Section
        if (isAdmin) {
            Text("Admin Controls", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = contentType,
                onValueChange = { contentType = it },
                label = { Text("Content Type (e.g., News, Community)") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.Gray,
                    cursorColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (contentType.isNotBlank() && contentText.isNotBlank()) {
                        viewModel.addGlobalContent(contentType, contentText)
                        contentType = ""
                        contentText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Global Content")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        LazyColumn {
            items(activities) { activity ->
                ActivityItem(activity, isAdmin, viewModel::deleteActivity)
            }
        }
    }
}

@Composable
fun ActivityItem(activity: ActivityRecord, isAdmin: Boolean, onDelete: (String) -> Unit) {
    Card(modifier = Modifier.padding(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.activityType, style = MaterialTheme.typography.titleMedium)
                Text(activity.content)
            }
            if (isAdmin) {
                Text("Delete", modifier = Modifier.clickable { onDelete(activity.id) })
            }
        }
    }
}