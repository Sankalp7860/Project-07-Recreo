package com.example.recreationapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment // Add this import
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.recreationapp.api.MusicCategory
import com.example.recreationapp.api.MusicItem
import com.example.recreationapp.player.YoutubePlayerWrapper
import com.example.recreationapp.viewmodel.AppViewModel
import com.example.recreationapp.viewmodel.MusicViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    appViewModel: AppViewModel = viewModel(),
    musicViewModel: MusicViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val playerView = remember { YouTubePlayerView(context) }
    val playerWrapper = remember {
        YoutubePlayerWrapper(context, lifecycleOwner, playerView)
    }

    val currentTrack by playerWrapper.currentTrack.collectAsState()
    val isPlaying by playerWrapper.isPlaying.collectAsState()
    val currentPosition by playerWrapper.currentPosition.collectAsState()
    val totalDuration by playerWrapper.totalDuration.collectAsState()
    val playbackState by playerWrapper.playbackState.collectAsState()

    val categories by musicViewModel.categories.collectAsState()
    val selectedCategory by musicViewModel.selectedCategory.collectAsState()
    val musicItems by musicViewModel.musicItems.collectAsState()
    val isLoading by musicViewModel.isLoading.collectAsState()
    val searchQuery by musicViewModel.searchQuery.collectAsState()
    val recentlyPlayed by musicViewModel.recentlyPlayed.collectAsState()

    var isSearchVisible by remember { mutableStateOf(false) }
    var isPlayerExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(currentTrack) {
        currentTrack?.let { track ->
            musicViewModel.addToRecentlyPlayed(track)
            appViewModel.user.value?.uid?.let { uid ->
                appViewModel.addActivity(uid, "Music", "Played ${track.title}")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            playerWrapper.release()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Music Player") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            actions = {
                IconButton(onClick = { isSearchVisible = !isSearchVisible }) {
                    Icon(Icons.Filled.Search, "Search")
                }
            }
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = isSearchVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { musicViewModel.setSearchQuery(it) },
                        onSearch = { musicViewModel.searchMusic() },
                        onClose = { isSearchVisible = false }
                    )
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(categories) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory?.id == category.id,
                            onClick = { musicViewModel.selectCategory(category) }
                        )
                    }
                }

                if (recentlyPlayed.isNotEmpty()) {
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                        Text(
                            "Recently Played",
                            style = MaterialTheme.typography.titleMedium
                        )
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(recentlyPlayed) { track ->
                                RecentlyPlayedItem(
                                    musicItem = track,
                                    onClick = { playerWrapper.play(track) }
                                )
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (musicItems.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No music found. Try searching or select a category.")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = if (currentTrack != null) 80.dp else 16.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(musicItems) { track ->
                                MusicListItem(
                                    musicItem = track,
                                    isPlaying = currentTrack?.id == track.id && isPlaying,
                                    onPlayClick = { playerWrapper.play(track) },
                                    onFavoriteToggle = { musicViewModel.toggleFavorite(track) }
                                )
                            }
                        }
                    }
                }
            }

            // Use the general AnimatedVisibility composable
            this@Column.AnimatedVisibility(
                visible = currentTrack != null && !isPlayerExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                currentTrack?.let { track ->
                    MiniPlayer(
                        musicItem = track,
                        isPlaying = isPlaying,
                        onPlayPauseClick = { playerWrapper.togglePlayPause() },
                        onExpand = { isPlayerExpanded = true }
                    )
                }
            }

            // Use the general AnimatedVisibility composable
            this@Column.AnimatedVisibility(
                visible = isPlayerExpanded,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                currentTrack?.let { track ->
                    FullScreenPlayer(
                        musicItem = track,
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        totalDuration = totalDuration,
                        playerView = playerView,
                        onPlayPauseClick = { playerWrapper.togglePlayPause() },
                        onSeek = { playerWrapper.seekTo(it) },
                        onClose = { isPlayerExpanded = false },
                        onStop = {
                            playerWrapper.stop()
                            isPlayerExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: MusicCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = category.title,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClose: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search for music...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
    }
}

@Composable
fun RecentlyPlayedItem(
    musicItem: MusicItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = musicItem.thumbnailUrl,
            contentDescription = "Album art",
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Text(
            text = musicItem.title,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = musicItem.artist,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MusicListItem(
    musicItem: MusicItem,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onPlayClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = musicItem.thumbnailUrl,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = musicItem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isPlaying)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = musicItem.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isPlaying)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                musicItem.duration?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isPlaying)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }

            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (musicItem.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (musicItem.isFavorite) Color.Red else if (isPlaying)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

@Composable
fun MiniPlayer(
    musicItem: MusicItem,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onExpand: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable(onClick = onExpand),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = musicItem.thumbnailUrl,
                contentDescription = "Album art",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = musicItem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = musicItem.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            IconButton(onClick = onExpand) {
                Icon(
                    imageVector = Icons.Filled.ExpandLess,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun FullScreenPlayer(
    musicItem: MusicItem,
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    playerView: YouTubePlayerView,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onClose: () -> Unit,
    onStop: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.ExpandMore, "Collapse")
                }
                Text("Now Playing", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onStop) {
                    Icon(Icons.Filled.Close, "Stop")
                }
            }

            AndroidView(
                factory = { playerView },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = musicItem.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = musicItem.artist,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                LinearProgressIndicator(
                    progress = {
                        if (totalDuration > 0) currentPosition.toFloat() / totalDuration.toFloat()
                        else 0f
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatDuration(currentPosition), style = MaterialTheme.typography.bodySmall)
                    Text(formatDuration(totalDuration), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Previous */ }) {
                    Icon(Icons.Filled.SkipPrevious, "Previous", modifier = Modifier.size(36.dp))
                }
                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                IconButton(onClick = { /* Next */ }) {
                    Icon(Icons.Filled.SkipNext, "Next", modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

@Composable
fun formatDuration(durationMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - TimeUnit.MINUTES.toSeconds(minutes)
    return String.format("%02d:%02d", minutes, seconds)
}