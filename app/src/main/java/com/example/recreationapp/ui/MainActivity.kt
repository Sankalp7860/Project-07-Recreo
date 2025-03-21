package com.example.recreationapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.model.ActivityRecord
import com.example.recreationapp.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AppViewModel = viewModel()) {
    val user by viewModel.user.observeAsState(initial = null)
    val activities by viewModel.activities.observeAsState(initial = emptyList())
    val context = LocalContext.current
    var contentType by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var selectedScreen by remember { mutableStateOf("Home") }

    val activityMap = mapOf(
        "Music" to MusicActivity::class.java,
        "Drawing" to DrawingActivity::class.java,
        "Daily Journal" to JournalActivity::class.java,
        "Community Sharing" to CommunityActivity::class.java,
        "Books" to NewsActivity::class.java
    )

    val preferredActivities = user?.preferredActivities ?: emptyList()
    val otherActivities = activityMap.keys.filterNot { it in preferredActivities }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                preferredActivities = preferredActivities,
                otherActivities = otherActivities,
                selectedScreen = selectedScreen,
                onScreenSelected = { screen -> selectedScreen = screen },
                onActivitySelected = { activity ->
                    context.startActivity(Intent(context, activityMap[activity]))
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when (selectedScreen) {
                "Home" -> {
                    // Admin Section
                    if (user?.isAdmin == true) {
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
                            ActivityItem(activity, user?.isAdmin == true, viewModel::deleteActivity)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    preferredActivities: List<String>,
    otherActivities: List<String>,
    selectedScreen: String,
    onScreenSelected: (String) -> Unit,
    onActivitySelected: (String) -> Unit
) {
    NavigationBar {
        // Home button
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedScreen == "Home",
            onClick = { onScreenSelected("Home") }
        )

        // Preferred activities
        preferredActivities.forEach { activity ->
            NavigationBarItem(
                icon = {
                    when (activity) {
                        "Music" -> Icon(Icons.Filled.MusicNote, contentDescription = activity)
                        "Drawing" -> Icon(Icons.Filled.Brush, contentDescription = activity)
                        "Daily Journal" -> Icon(Icons.Filled.Book, contentDescription = activity)
                        "Community Sharing" -> Icon(Icons.Filled.Group, contentDescription = activity)
                        "Books" -> Icon(Icons.Filled.MenuBook, contentDescription = activity)
                        else -> Icon(Icons.Filled.Star, contentDescription = activity)
                    }
                },
                label = { Text(activity) },
                selected = selectedScreen == activity,
                onClick = {
                    onScreenSelected(activity)
                    onActivitySelected(activity)
                }
            )
        }

        // More menu
        NavigationBarItem(
            icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = "More") },
            label = { Text("More") },
            selected = selectedScreen == "More",
            onClick = { onScreenSelected("More") }
        )

        // Settings
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = selectedScreen == "Settings",
            onClick = { onScreenSelected("Settings") }
        )
    }

    // Show dropdown for "More" menu
    if (selectedScreen == "More") {
        DropdownMenu(
            expanded = true,
            onDismissRequest = { onScreenSelected("Home") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            otherActivities.forEach { activity ->
                DropdownMenuItem(
                    text = { Text(activity) },
                    onClick = {
                        onActivitySelected(activity)
                        onScreenSelected(activity)
                    }
                )
            }
        }
    }

    // Navigate to Settings
    if (selectedScreen == "Settings") {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            context.startActivity(Intent(context, SettingsActivity::class.java))
            onScreenSelected("Home")
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