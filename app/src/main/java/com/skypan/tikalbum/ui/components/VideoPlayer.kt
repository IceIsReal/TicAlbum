package com.skypan.tikalbum.ui.components

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.skypan.tikalbum.ui.viewmodel.PlayerViewModel
import com.skypan.tikalbum.utils.formatTime

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(viewModel: PlayerViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable { viewModel.togglePlayPause() }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = viewModel.exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!viewModel.isPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            Text(
                text = "${formatTime(viewModel.currentTime)} / ${formatTime(viewModel.totalDuration)}",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.End)
            )

            // --- 核心修复：Slider 回调逻辑 ---
            Slider(
                value = viewModel.currentTime.toFloat(),
                onValueChange = { newValue ->
                    // 1. 告诉 ViewModel：用户开始拖了，别再自动更新时间
                    viewModel.isDragging = true
                    // 2. 实时更新当前显示的时间，让进度条跟着手指动
                    viewModel.currentTime = newValue.toLong()
                },
                onValueChangeFinished = {
                    // 3. 用户松手了，执行真正的跳转
                    viewModel.seekTo(viewModel.currentTime)
                    // 4. 恢复后台自动更新
                    viewModel.isDragging = false
                },
                valueRange = 0f..viewModel.totalDuration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }
    }
}