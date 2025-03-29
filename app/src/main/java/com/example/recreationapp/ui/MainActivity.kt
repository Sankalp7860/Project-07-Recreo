package com.example.recreationapp.ui

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.recreationapp.ui.theme.RecreationAppTheme
import com.example.recreationapp.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecreationAppTheme {
                val viewModel: AppViewModel = viewModel()
                MainScreen(viewModel = viewModel)
            }
        }
    }
}

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: AppViewModel) {
    val user by viewModel.user.observeAsState(initial = null)
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"

    val preferredActivities = user?.preferredActivities ?: emptyList()
    val allActivities = listOf(
        "Music",
        "Drawing",
        "Games",
        "Journal",
        "Community Sharing",
        "Books"
    )
    val otherActivities = allActivities.filterNot { it in preferredActivities }

    // Define navigation items based on preferred activities
    val navItems = mutableListOf(
        BottomNavItem(
            route = "home",
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        )
    )

    // Add preferred activities (limited to 3 for space)
    preferredActivities.take(3).forEach { activity ->
        val icon = when (activity) {
            "Music" -> Pair(Icons.Filled.MusicNote, Icons.Outlined.MusicNote)
            "Drawing" -> Pair(Icons.Filled.Brush, Icons.Outlined.Brush)
            "Games" -> Pair(Icons.Filled.SportsEsports, Icons.Outlined.SportsEsports)
            "Journal" -> Pair(Icons.Filled.Book, Icons.Outlined.Book)
            "Community Sharing" -> Pair(Icons.Filled.Group, Icons.Outlined.Group)
            "Books" -> Pair(Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
            else -> Pair(Icons.Filled.Star, Icons.Outlined.Star)
        }

        navItems.add(
            BottomNavItem(
                route = activity,
                title = activity,
                selectedIcon = icon.first,
                unselectedIcon = icon.second
            )
        )
    }

    // Add Explore and Settings
    navItems.add(
        BottomNavItem(
            route = "explore",
            title = "Explore",
            selectedIcon = Icons.Filled.Explore,
            unselectedIcon = Icons.Outlined.Explore
        )
    )

    navItems.add(
        BottomNavItem(
            route = "Settings",
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings
        )
    )

    var showExploreDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = when (currentRoute) {
                                "home" -> "Recreation Hub"
                                "explore" -> "Explore Activities"
                                else -> currentRoute
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 6.dp
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            navItems.forEach { item ->
                                val isSelected = currentRoute == item.route
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            if (item.route == "explore") {
                                                showExploreDialog = true
                                            } else {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.startDestinationId) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(4.dp),
                                        contentAlignment = androidx.compose.ui.Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            // Background indicator for selected item
                                            Surface(
                                                modifier = Modifier.size(40.dp),
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                tonalElevation = 2.dp
                                            ) {}
                                        }

                                        if (item.route == "explore" && showExploreDialog) {
                                            BadgedBox(badge = {
                                                Badge {
                                                    Text(otherActivities.size.toString())
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                    contentDescription = item.title,
                                                    tint = if (isSelected)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.title,
                                                tint = if (isSelected)
                                                    MaterialTheme.colorScheme.primary
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
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
                DrawingAppScreen(navController = navController, viewModel = viewModel) {
                    DrawingsOverviewScreen(
                        navController = navController,
                        snackbarHostState = SnackbarHostState(),
                        viewModel = viewModel
                    )
                }
            }
            composable("new_drawing") {
                DrawingAppScreen(navController = navController, viewModel = viewModel) {
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
                DrawingAppScreen(navController = navController, viewModel = viewModel) {
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
                DrawingAppScreen(navController = navController, viewModel = viewModel) {
                    ViewDrawingScreen(
                        drawingPath = drawingPath ?: "",
                        navController = navController,
                        snackbarHostState = SnackbarHostState(),
                        viewModel = viewModel
                    )
                }
            }
            composable("Games") {
                GamesScreen(viewModel = viewModel)
            }
            composable("Journal") {
                NewJournalScreen(viewModel = viewModel)
            }
            composable("Community Sharing") {
                CommunityScreen(viewModel = viewModel)
            }
            composable("Books") {
                BooksApp()
            }
            composable("Settings") {
                SettingsScreen(viewModel, navController)
            }
        }
            // Explore Dialog
            if (showExploreDialog) {
                ExploreDialog(
                    activities = otherActivities,
                    onDismiss = { showExploreDialog = false },
                    onActivitySelected = { activity ->
                        showExploreDialog = false
                        navController.navigate(activity) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    }
}
@Composable
fun ExploreDialog(
    activities: List<String>,
    onDismiss: () -> Unit,
    onActivitySelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Explore More Activities") },
        text = {
            Column {
                activities.forEach { activity ->
                    ListItem(
                        headlineContent = { Text(activity) },
                        leadingContent = {
                            Icon(
                                imageVector = when (activity) {
                                    "Music" -> Icons.Filled.MusicNote
                                    "Drawing" -> Icons.Filled.Brush
                                    "Games" -> Icons.Filled.SportsEsports
                                    "Journal" -> Icons.Filled.Book
                                    "Community Sharing" -> Icons.Filled.Group
                                    "Books" -> Icons.Filled.MenuBook
                                    else -> Icons.Filled.Star
                                },
                                contentDescription = activity
                            )
                        },
                        modifier = Modifier.clickable { onActivitySelected(activity) }
                    )
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