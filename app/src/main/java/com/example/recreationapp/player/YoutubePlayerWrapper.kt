package com.example.recreationapp.player

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.example.recreationapp.api.MusicItem
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class YoutubePlayerWrapper(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val playerView: YouTubePlayerView
) {
    private var youTubePlayer: YouTubePlayer? = null

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _totalDuration = MutableStateFlow(0L)
    val totalDuration: StateFlow<Long> = _totalDuration.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentTrack = MutableStateFlow<MusicItem?>(null)
    val currentTrack: StateFlow<MusicItem?> = _currentTrack.asStateFlow()

    private val _playbackState = MutableStateFlow(0)
    val playbackState: StateFlow<Int> = _playbackState.asStateFlow()

    init {
        playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(player: YouTubePlayer) {
                youTubePlayer = player
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState) {
                when (state) {
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PLAYING -> _isPlaying.value = true
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.PAUSED -> _isPlaying.value = false
                    com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerState.ENDED -> {
                        _isPlaying.value = false
                        _currentPosition.value = 0L
                    }
                    else -> {}
                }
                _playbackState.value = state.ordinal
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                _currentPosition.value = (second * 1000).toLong()
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                _totalDuration.value = (duration * 1000).toLong()
            }
        })

        lifecycleOwner.lifecycle.addObserver(playerView)
    }

    fun play(musicItem: MusicItem) {
        youTubePlayer?.loadVideo(musicItem.id, 0f)
        _currentTrack.value = musicItem
    }

    fun togglePlayPause() {
        if (_isPlaying.value) {
            youTubePlayer?.pause()
        } else {
            youTubePlayer?.play()
        }
    }

    fun seekTo(position: Long) {
        youTubePlayer?.seekTo(position / 1000f)
    }

    fun stop() {
        youTubePlayer?.pause()
        _currentTrack.value = null
        _currentPosition.value = 0L
    }

    fun release() {
        playerView.release()
        _currentTrack.value = null
    }
}