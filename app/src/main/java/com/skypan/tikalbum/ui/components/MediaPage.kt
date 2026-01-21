package com.skypan.tikalbum.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.skypan.tikalbum.model.MediaModel
import com.skypan.tikalbum.model.MediaType
import com.skypan.tikalbum.model.PlayMode
import kotlinx.coroutines.delay

@Composable
fun MediaPage(
    media: MediaModel,
    isActive: Boolean,
    playMode: PlayMode,
    onVideoEnded: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (media.type == MediaType.IMAGE) {
            LaunchedEffect(isActive) {
                if (isActive) {
                    delay(5000)
                    onVideoEnded()
                }
            }
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(media.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().align(Alignment.Center)
            )
        } else {
            VideoPlayer(
                uri = media.uri,
                isResumed = isActive,
                playMode = playMode,
                onEnded = onVideoEnded
            )
        }

        Text(
            text = media.name,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}