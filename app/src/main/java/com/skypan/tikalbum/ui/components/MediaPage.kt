package com.skypan.tikalbum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.skypan.tikalbum.model.MediaModel
import com.skypan.tikalbum.model.MediaType
import com.skypan.tikalbum.model.PlayMode
import com.skypan.tikalbum.ui.viewmodel.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun MediaPage(
    media: MediaModel,
    isActive: Boolean,
    playMode: PlayMode,
    playerViewModel: PlayerViewModel,
    onMediaEnded: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // --- 核心修复：监听 isActive 和 playMode 的双重变化 ---
    LaunchedEffect(isActive, playMode) {
        if (!isActive) {
            scale = 1f
            offset = Offset.Zero
        } else if (media.type == MediaType.IMAGE) {
            // 只有在列表循环模式下才执行 5 秒倒计时
            if (playMode == PlayMode.LIST_LOOP) {
                delay(5000)
                // 延迟结束时再次确认是否依然在列表循环模式且页面处于激活态
                if (isActive && playMode == PlayMode.LIST_LOOP) {
                    onMediaEnded()
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (media.type == MediaType.IMAGE) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                scale = 2.5f
                            }
                        })
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown()
                            do {
                                val event = awaitPointerEvent()
                                val zoom = event.calculateZoom()
                                val pan = event.calculatePan()

                                if (scale > 1f || zoom != 1f) {
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    if (scale > 1f) {
                                        offset += pan
                                    } else {
                                        offset = Offset.Zero
                                    }
                                    event.changes.forEach { it.consume() }
                                }
                            } while (event.changes.any { it.pressed })
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        } else {
            if (isActive) {
                VideoPlayer(viewModel = playerViewModel)
            }
        }

        // UI 遮罩层（文件名、发布日期）
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 90.dp, end = 16.dp)
        ) {
            Text(
                text = media.name,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "发布于：${media.date}",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}