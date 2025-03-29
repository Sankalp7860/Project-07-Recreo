package com.example.recreationapp.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.recreationapp.model.ActivityRecord
import com.example.recreationapp.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ActivityCard(
    val name: String,
    val icon: ImageVector,
    val description: String,
    val backgroundColor: Color
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AppViewModel,
    navController: NavController = rememberNavController()
) {
    val activities by viewModel.activities.observeAsState(initial = emptyList())
    val user by viewModel.user.observeAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    val preferredActivities = user?.preferredActivities ?: emptyList()

    // Activity cards with beautiful colors
    val activityCards = listOf(
        ActivityCard(
            name = "Music",
            icon = Icons.Filled.MusicNote,
            description = "Discover and enjoy music that fits your mood",
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        ),
        ActivityCard(
            name = "Drawing",
            icon = Icons.Filled.Brush,
            description = "Express yourself through art and drawings",
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        ActivityCard(
            name = "Games",
            icon = Icons.Filled.SportsEsports,
            description = "Play engaging games to relax your mind",
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        ActivityCard(
            name = "Journal",
            icon = Icons.Filled.Book,
            description = "Record your thoughts and experiences",
            backgroundColor = MaterialTheme.colorScheme.errorContainer
        ),
        ActivityCard(
            name = "Community Sharing",
            icon = Icons.Filled.Group,
            description = "Connect and share with your community",
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        ActivityCard(
            name = "Books",
            icon = Icons.Filled.MenuBook,
            description = "Explore a world of books and stories",
            backgroundColor = MaterialTheme.colorScheme.inversePrimary
        )
    )

    // Greeting message state
    var greetingMessage by remember { mutableStateOf("") }
    var isGreetingVisible by remember { mutableStateOf(false) }

    // Determine greeting based on time of day
    LaunchedEffect(user) {
        val currentHour = java.time.LocalTime.now().hour
        val username = user?.name ?: "there"

        greetingMessage = when {
            currentHour < 12 -> "Good morning, $username!"
            currentHour < 18 -> "Good afternoon, $username!"
            else -> "Good evening, $username!"
        }

        // Animation delay
        delay(200)
        isGreetingVisible = true
    }

    // Admin control state
    var showAdminControls by remember { mutableStateOf(false) }
    var contentType by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    val isAdmin = user?.isAdmin == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting
        item {
            AnimatedVisibility(
                visible = isGreetingVisible,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut()
            ) {
                Column {
                    Text(
                        text = greetingMessage,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "What would you like to do today?",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Quick Access
        item {
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(preferredActivities.take(4)) { index, activity ->
                    val card = activityCards.find { it.name == activity }
                    if (card != null) {
                        QuickAccessButton(
                            activityCard = card,
                            onClick = {
                                coroutineScope.launch {
                                    navController.navigate(activity)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Recent Activities Section
        item {
            Text(
                text = "Recent Activities",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (activities.isEmpty()) {
                            item {
                                Text(
                                    "No recent activities yet. Start exploring!",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        } else {
                            items(activities.take(5)) { activity ->
                                ActivityItem(
                                    activity = activity,
                                    isAdmin = isAdmin,
                                    onDelete = viewModel::deleteActivity
                                )
                            }
                        }
                    }
                }
            }
        }

        // Discover Section
        item {
            Text(
                text = "Discover",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(activityCards.filterNot { card ->
                    preferredActivities.contains(card.name)
                }) { card ->
                    DiscoverCard(
                        activityCard = card,
                        onClick = {
                            navController.navigate(card.name)
                        }
                    )
                }
            }
        }

        // Admin Controls
        if (isAdmin) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Admin Controls",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )

                            IconButton(onClick = { showAdminControls = !showAdminControls }) {
                                Icon(
                                    if (showAdminControls) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = "Toggle Admin Controls",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        AnimatedVisibility(visible = showAdminControls) {
                            Column {
                                OutlinedTextField(
                                    value = contentType,
                                    onValueChange = { contentType = it },
                                    label = { Text("Content Type") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = contentText,
                                    onValueChange = { contentText = it },
                                    label = { Text("Content") },
                                    modifier = Modifier.fillMaxWidth(),
                                    minLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

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
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Add",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Add Global Content")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAccessButton(activityCard: ActivityCard, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(activityCard.backgroundColor)
                .clickable(onClick = onClick)
        ) {
            Icon(
                imageVector = activityCard.icon,
                contentDescription = activityCard.name,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = activityCard.name,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1
        )
    }
}

@Composable
fun DiscoverCard(activityCard: ActivityCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = activityCard.backgroundColor
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = activityCard.icon,
                contentDescription = activityCard.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = activityCard.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = activityCard.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                maxLines = 2
            )
        }
    }
}

@Composable
fun ActivityItem(activity: ActivityRecord, isAdmin: Boolean, onDelete: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity type indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activity.activityType.first().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.activityType,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = activity.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (isAdmin) {
                IconButton(onClick = { onDelete(activity.id) }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}