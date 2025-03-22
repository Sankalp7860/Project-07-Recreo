package com.example.recreationapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val navController = rememberNavController()

    val preferredActivities = user?.preferredActivities ?: emptyList()
    val allActivities = listOf(
        "Music",
        "Drawing",
        "Games", // Renamed from "Daily Journal" to "Games"
        "Journal", // New Journal section
        "Community Sharing",
        "Books"
    )
    val otherActivities = allActivities.filterNot { it in preferredActivities }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                preferredActivities = preferredActivities,
                otherActivities = otherActivities,
                navController = navController
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") {
                HomeScreen(viewModel)
            }
            composable("Music") {
                MusicScreen()
            }
            composable("Drawing") {
                DrawingAppScreen(navController = navController, viewModel = viewModel()) {
                    DrawingsOverviewScreen(
                        navController = navController,
                        snackbarHostState = SnackbarHostState(),
                        viewModel = viewModel
                    )
                }
            }
            composable("new_drawing") {
                DrawingAppScreen(navController = navController, viewModel = viewModel()) {
                    DrawingScreen(
                        viewModel = viewModel,
                        snackbarHostState = SnackbarHostState(),
                        navController = navController,
                        editMode = false,
                        drawingPath = null
                    )
                }
            }
            composable(
                "edit_drawing/{drawingPath}",
                arguments = listOf(navArgument("drawingPath") { type = NavType.StringType })
            ) { backStackEntry ->
                val drawingPath = backStackEntry.arguments?.getString("drawingPath")
                DrawingAppScreen(navController = navController, viewModel = viewModel()) {
                    DrawingScreen(
                        viewModel = viewModel,
                        snackbarHostState = SnackbarHostState(),
                        navController = navController,
                        editMode = true,
                        drawingPath = drawingPath
                    )
                }
            }
            composable(
                "view_drawing/{drawingPath}",
                arguments = listOf(navArgument("drawingPath") { type = NavType.StringType })
            ) { backStackEntry ->
                val drawingPath = backStackEntry.arguments?.getString("drawingPath")
                DrawingAppScreen(navController = navController, viewModel = viewModel()) {
                    ViewDrawingScreen(
                        drawingPath = drawingPath ?: "",
                        navController = navController,
                        snackbarHostState = SnackbarHostState(),
                        viewModel = viewModel
                    )
                }
            }
            composable("Games") { // Renamed from "Daily Journal" to "Games"
                GamesScreen() // This is the old JournalScreen, now for games
            }
            composable("Journal") { // New Journal section
                NewJournalScreen(viewModel = viewModel) // We'll create this new screen
            }
            composable("Community Sharing") {
                CommunityScreen()
            }
            composable("Books") {
                BooksApp()
            }
            composable("Settings") {
                SettingsScreen(viewModel, navController)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    preferredActivities: List<String>,
    otherActivities: List<String>,
    navController: NavHostController
) {
    var selectedScreen by remember { mutableStateOf("home") }

    NavigationBar {
        // Home button
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = selectedScreen == "home",
            onClick = {
                selectedScreen = "home"
                navController.navigate("home") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )

        // Preferred activities
        preferredActivities.forEach { activity ->
            NavigationBarItem(
                icon = {
                    when (activity) {
                        "Music" -> Icon(Icons.Filled.MusicNote, contentDescription = activity)
                        "Drawing" -> Icon(Icons.Filled.Brush, contentDescription = activity)
                        "Games" -> Icon(Icons.Filled.SportsEsports, contentDescription = activity) // Updated icon for Games
                        "Journal" -> Icon(Icons.Filled.Book, contentDescription = activity) // Icon for new Journal
                        "Community Sharing" -> Icon(Icons.Filled.Group, contentDescription = activity)
                        "Books" -> Icon(Icons.Filled.MenuBook, contentDescription = activity)
                        else -> Icon(Icons.Filled.Star, contentDescription = activity)
                    }
                },
                label = { Text(activity) },
                selected = selectedScreen == activity,
                onClick = {
                    selectedScreen = activity
                    navController.navigate(activity) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }

        // More menu
        NavigationBarItem(
            icon = { Icon(Icons.Filled.MoreHoriz, contentDescription = "More") },
            label = { Text("More") },
            selected = selectedScreen == "More",
            onClick = { selectedScreen = "More" }
        )

        // Settings
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            selected = selectedScreen == "Settings",
            onClick = {
                selectedScreen = "Settings"
                navController.navigate("Settings") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
    }

    // Show dropdown for "More" menu
    if (selectedScreen == "More") {
        DropdownMenu(
            expanded = true,
            onDismissRequest = {
                selectedScreen = "home"
                navController.navigate("home")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            otherActivities.forEach { activity ->
                DropdownMenuItem(
                    text = { Text(activity) },
                    onClick = {
                        selectedScreen = activity
                        navController.navigate(activity) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}