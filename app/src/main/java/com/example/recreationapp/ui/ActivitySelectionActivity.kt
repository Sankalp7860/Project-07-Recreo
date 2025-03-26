package com.example.recreationapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.viewmodel.AppViewModel

class ActivitySelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val viewModel: AppViewModel = viewModel() // Initialize viewModel here
                ActivitySelectionScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivitySelectionScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val availableActivities = listOf(
        "Music",
        "Drawing",
        "Games", // Renamed from "Daily Journal" to "Games"
        "Journal", // Added new Journal section
        "Community Sharing",
        "Books"
    )

    var selectedActivities by remember { mutableStateOf(listOf<String>()) }
    val maxSelections = 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Activities") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select up to $maxSelections activities",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableActivities) { activityName ->
                    val isSelected = selectedActivities.contains(activityName)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isSelected) {
                                    selectedActivities = selectedActivities - activityName
                                } else if (selectedActivities.size < maxSelections) {
                                    selectedActivities = selectedActivities + activityName
                                }
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activityName,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                                enabled = !isSelected || selectedActivities.size < maxSelections
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedActivities.size == maxSelections) {
                        viewModel.updateUserPreferences(selectedActivities) {
                            context.startActivity(Intent(context, MainActivity::class.java))
                            (context as ComponentActivity).finish()
                        }
                    }
                },
                enabled = selectedActivities.size == maxSelections,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Continue")
            }
        }
    }
}