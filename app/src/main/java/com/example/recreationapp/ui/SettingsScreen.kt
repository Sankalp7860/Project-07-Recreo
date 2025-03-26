package com.example.recreationapp.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.recreationapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel, navController: NavHostController) { // Removed default value
    val user by viewModel.user.observeAsState()
    val context = LocalContext.current
    var showActivitySelection by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "User Details",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        user?.let { userData ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Name: ${userData.name}")
                    Text("Email: ${userData.email}")
                    Text("Preferred Activities: ${userData.preferredActivities.joinToString()}")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Customize Navigation",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = { showActivitySelection = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Preferred Activities")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.logout()
                // Navigate to LoginActivity and clear the activity stack
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout")
        }

        if (showActivitySelection) {
            ActivitySelectionDialog(
                onDismiss = { showActivitySelection = false },
                onConfirm = { newActivities ->
                    viewModel.updateUserPreferences(newActivities) {
                        showActivitySelection = false
                    }
                }
            )
        }
    }
}

@Composable
fun ActivitySelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<String>) -> Unit
) {
    val availableActivities = listOf(
        "Music",
        "Drawing",
        "Games",
        "Journal",
        "Community Sharing",
        "Books"
    )

    var selectedActivities by remember { mutableStateOf(listOf<String>()) }
    val maxSelections = 3

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Activities") },
        text = {
            Column {
                Text(
                    text = "Select up to $maxSelections activities",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(availableActivities) { activityName ->
                        val isSelected = selectedActivities.contains(activityName)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isSelected) {
                                        selectedActivities = selectedActivities - activityName
                                    } else if (selectedActivities.size < maxSelections) {
                                        selectedActivities = selectedActivities + activityName
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activityName,
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (selectedActivities.size == maxSelections) {
                        onConfirm(selectedActivities)
                    }
                },
                enabled = selectedActivities.size == maxSelections
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}