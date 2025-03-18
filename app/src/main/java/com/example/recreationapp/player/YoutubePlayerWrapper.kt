package com.example.recreationapp.player

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.example.recreationapp.api.MusicItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class YoutubePlayerWrapper(context: Context) {

    private val player: ExoPlayer
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _bufferingPercentage = MutableStateFlow(0)
    val bufferingPercentage: StateFlow<Int> = _bufferingPercentage.asStateFlow()

    private val _currentTrack = MutableStateFlow<MusicItem?>(null)
    val currentTrack: StateFlow<MusicItem?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow(Player.STATE_IDLE)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    init {
        val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)

        player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .build()

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(state: Int) {
                _playbackState.value = state
                if (state == Player.STATE_READY) {
                    _totalDuration.value = player.duration
                }
            }
        })

        // Start position tracking
        createPositionTracker()
    }

    private fun createPositionTracker() {
        player.createPositionTracker(
            period = 500,
            onPositionChanged = { position ->
                _currentPosition.value = position
                _bufferingPercentage.value = player.bufferedPercentage
            }
        )
    }

    fun play(musicItem: MusicItem) {
        // Format for YouTube videos
        val videoUrl = "https://www.youtube.com/watch?v=${musicItem.id}"
        val mediaItem = MediaItem.fromUri(videoUrl)

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        _currentTrack.value = musicItem
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun stop() {
        player.stop()
        _currentTrack.value = null
    }

    fun release() {
        player.release()
        _currentTrack.value = null
    }
}

// Extension function to create a position tracker
fun Player.createPositionTracker(
    period: Long = 1000,
    onPositionChanged: (Long) -> Unit
) {
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run() {
            onPositionChanged(currentPosition)
            if (isPlaying) {
                handler.postDelayed(this, period)
            }
        }
    }

    addListener(object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            handler.removeCallbacks(runnable)
            if (isPlaying) {
                handler.postDelayed(runnable, 0)
            }
        }
    })
}