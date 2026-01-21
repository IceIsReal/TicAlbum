package com.skypan.tikalbum.ui.viewmodel

import android.app.Application
import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.skypan.tikalbum.model.PlayMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build().apply {
        playWhenReady = true
    }

    var isPlaying by mutableStateOf(true)
    var currentTime by mutableLongStateOf(0L)
    var totalDuration by mutableLongStateOf(0L)
    var playbackSpeed by mutableFloatStateOf(1.0f)
    var isDragging by mutableStateOf(false)

    var onVideoEnded: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            while (true) {
                if (exoPlayer.playbackState == Player.STATE_READY && !isDragging) {
                    currentTime = exoPlayer.currentPosition.coerceAtLeast(0L)
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                }
                delay(500)
            }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                isPlaying = isPlayingNow
            }
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    totalDuration = exoPlayer.duration
                }
                if (state == Player.STATE_ENDED) {
                    onVideoEnded?.invoke()
                }
            }
        })
    }

    // --- 核心修复：增加实时切换循环模式的方法 ---
    fun handlePlayModeChange(playMode: PlayMode) {
        exoPlayer.repeatMode = if (playMode == PlayMode.SINGLE_LOOP)
            Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun prepareAndPlay(uri: android.net.Uri, playMode: PlayMode) {
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        // 准备时也应用模式
        handlePlayModeChange(playMode)

        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun seekTo(position: Long) {
        currentTime = position
        exoPlayer.seekTo(position)
    }

    fun updateSpeed() {
        playbackSpeed = when (playbackSpeed) {
            1.0f -> 1.5f
            1.5f -> 2.0f
            else -> 1.0f
        }
        exoPlayer.setPlaybackSpeed(playbackSpeed)
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }
}