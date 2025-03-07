package com.example.recreationapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.recreationapp.api.MusicApiService
import com.example.recreationapp.api.MusicRepository
import com.example.recreationapp.viewmodel.AppViewModel
import kotlinx.coroutines.launch

class MusicActivity : ComponentActivity() {
    private lateinit var player: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()
        setContent {
            MaterialTheme {
                MusicScreen(player)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}

@Composable
fun MusicScreen(player: ExoPlayer, viewModel: AppViewModel = viewModel()) {
    var tracks by remember { mutableStateOf(listOf<MusicApiService.Track>()) }
    var isPlaying by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            tracks = try {
                MusicRepository.getTracks()
            } catch (e: Exception) {
                listOf(MusicApiService.Track("Sample Song", "Unknown", "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"))
            }
            viewModel.user.value?.uid?.let { uid ->
                viewModel.addActivity(uid, "Music", "Loaded music tracks")
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Music Player", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                if (tracks.isNotEmpty()) {
                    val mediaItem = MediaItem.fromUri(tracks[0].streamUrl ?: return@Button)
                    player.setMediaItem(mediaItem)
                    player.prepare()
                    player.play()
                    isPlaying = true
                    viewModel.user.value?.uid?.let { uid ->
                        viewModel.addActivity(uid, "Music", "Played ${tracks[0].title}")
                    }
                }
            }) {
                Text("Play")
            }
            Button(onClick = {
                player.pause()
                isPlaying = false
            }) {
                Text("Pause")
            }
            Button(onClick = {
                player.stop()
                isPlaying = false
            }) {
                Text("Stop")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(tracks) { track ->
                Card(modifier = Modifier.padding(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Text("${track.title} by ${track.artist}", modifier = Modifier.weight(1f))
                        Button(onClick = {
                            val mediaItem = MediaItem.fromUri(track.streamUrl ?: return@Button)
                            player.setMediaItem(mediaItem)
                            player.prepare()
                            player.play()
                            isPlaying = true
                            viewModel.user.value?.uid?.let { uid ->
                                viewModel.addActivity(uid, "Music", "Played ${track.title}")
                            }
                        }) {
                            Text("Play")
                        }
                    }
                }
            }
        }
    }
}