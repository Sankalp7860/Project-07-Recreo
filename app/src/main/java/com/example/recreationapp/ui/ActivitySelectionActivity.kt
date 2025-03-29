package com.example.recreationapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.ui.theme.RecreationAppTheme
import com.example.recreationapp.viewmodel.AppViewModel

class ActivitySelectionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecreationAppTheme {
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
        "Games",
        "Journal",
        "Community Sharing",
        "Books"
    )

    // Map activities to emoji icons for visual appeal
    val activityIcons = mapOf(
        "Music" to "🎵",
        "Drawing" to "🎨",
        "Games" to "🎮",
        "Journal" to "📝",
        "Community Sharing" to "👥",
        "Books" to "📚"
    )

    var selectedActivities by remember { mutableStateOf(listOf<String>()) }
    val maxSelections = 3

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Your Activities", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Header text with instructions
                Text(
                    text = "Personalize Your Experience",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select $maxSelections activities you enjoy the most",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Selection counter
                Text(
                    text = "${selectedActivities.size}/$maxSelections selected",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedActivities.size == maxSelections)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Activity selection cards
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableActivities) { activityName ->
                        val isSelected = selectedActivities.contains(activityName)
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (isSelected) {
                                        selectedActivities = selectedActivities - activityName
                                    } else if (selectedActivities.size < maxSelections) {
                                        selectedActivities = selectedActivities + activityName
                                    }
                                },
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = if (isSelected) 6.dp else 2.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Activity icon
                                Text(
                                    text = activityIcons[activityName] ?: "",
                                    style = MaterialTheme.typography.headlineMedium,
                                    modifier = Modifier.padding(end = 16.dp)
                                )

                                // Activity name
                                Text(
                                    text = activityName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.weight(1f)
                                )

                                // Checkbox
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    enabled = isSelected || selectedActivities.size < maxSelections,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.outline
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Continue button
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
                        .height(50.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Continue", style = MaterialTheme.typography.labelLarge)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}