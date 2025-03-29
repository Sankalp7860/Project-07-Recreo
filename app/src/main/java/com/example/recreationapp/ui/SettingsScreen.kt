package com.example.recreationapp.ui

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.recreationapp.ui.theme.ThemeState
import com.example.recreationapp.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel, navController: NavHostController) {
    val user by viewModel.user.observeAsState()
    val context = LocalContext.current
    var showActivitySelection by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    user?.let { userData ->
                        Text(
                            text = userData.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = userData.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                SettingsSectionHeader(title = "Preferences")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.ViewModule,
                    title = "Activities",
                    subtitle = "Customize your navigation items",
                    onClick = { showActivitySelection = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.DarkMode,
                    title = "Theme",
                    subtitle = if (ThemeState.isDarkTheme) "Dark mode" else "Light mode",
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                SettingsSectionHeader(title = "Account")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.AccountCircle,
                    title = "Account Information",
                    subtitle = "View and edit your profile",
                    onClick = { showAccountDialog = true }
                )
            }

            item {
                SettingsSectionHeader(title = "App Information")
            }

            item {
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "About",
                    subtitle = "App version and information",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        Button(
            onClick = {
                viewModel.logout()
                // Navigate to LoginActivity and clear the activity stack
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                (context as ComponentActivity).finish()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Logout,
                contentDescription = "Logout"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }

    if (showActivitySelection) {
        ActivitySelectionDialog(
            onDismiss = { showActivitySelection = false },
            onConfirm = { newActivities ->
                viewModel.updateUserPreferences(newActivities) {
                    showActivitySelection = false
                    // Navigate back to home to refresh the UI
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showAccountDialog) {
        AccountDialog(
            user = user,
            onDismiss = { showAccountDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Theme") },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            ThemeState.isDarkTheme = false
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = !ThemeState.isDarkTheme,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Light Mode")
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            ThemeState.isDarkTheme = true
                            onDismiss()
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = ThemeState.isDarkTheme,
                        onClick = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Dark Mode")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AccountDialog(
    user: com.example.recreationapp.model.User?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Account Information") },
        text = {
            user?.let { userData ->
                Column {
                    ProfileInfoRow(label = "Name", value = userData.name)
                    ProfileInfoRow(label = "Email", value = userData.email)
                    Text(
                        text = "Preferred Activities",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                    userData.preferredActivities.forEach { activity ->
                        Text(
                            text = "• $activity",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } ?: Text("User data not available")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About App") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Recreation App",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A college project to create a multi-activity entertainment platform for students.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "© 2025 College Recreation Team",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
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

    val activityIcons = mapOf(
        "Music" to Icons.Filled.MusicNote,
        "Drawing" to Icons.Filled.Brush,
        "Games" to Icons.Filled.SportsEsports,
        "Journal" to Icons.Filled.Book,
        "Community Sharing" to Icons.Filled.Group,
        "Books" to Icons.Filled.MenuBook
    )

    var selectedActivities by remember { mutableStateOf(listOf<String>()) }
    val maxSelections = 3

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Your Favorite Activities") },
        text = {
            Column {
                Text(
                    text = "Select exactly $maxSelections activities to customize your navigation bar",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableActivities) { activityName ->
                        val isSelected = selectedActivities.contains(activityName)
                        val isEnabled = isSelected || selectedActivities.size < maxSelections

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isEnabled) {
                                    selectedActivities = if (isSelected) {
                                        selectedActivities - activityName
                                    } else {
                                        selectedActivities + activityName
                                    }
                                },
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = activityIcons[activityName] ?: Icons.Filled.Star,
                                    contentDescription = activityName,
                                    tint = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Text(
                                    text = activityName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f),
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )

                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    enabled = isEnabled,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = if (isEnabled)
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        else
                                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${selectedActivities.size} of $maxSelections selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedActivities.size == maxSelections) {
                        onConfirm(selectedActivities)
                    }
                },
                enabled = selectedActivities.size == maxSelections
            ) {
                Text("Save Preferences")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}