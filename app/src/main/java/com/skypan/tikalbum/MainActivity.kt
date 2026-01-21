package com.skypan.tikalbum

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FolderPickerScreen()
                }
            }
        }
    }
}

@Composable
fun FolderPickerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mediaList by remember { mutableStateOf<List<MediaModel>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { folderUri ->
            isScanning = true
            scope.launch(Dispatchers.IO) {
                val result = FileHelper.scanFolder(context, folderUri)
                withContext(Dispatchers.Main) {
                    mediaList = result
                    isScanning = false
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isScanning) {
            Text("⏳ 正在扫描中...")
        } else if (mediaList.isEmpty()) {
            Text("当前没有导入任何媒体文件")
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { launcher.launch(null) }) {
                Text("选择文件夹")
            }
        } else {
            TikTokScrollPlayer(
                mediaList = mediaList,
                onPickFolder = { launcher.launch(null) }
            )
        }
    }
}

// 修复点：改名为列表循环，逻辑更清晰
enum class PlayMode(val label: String) {
    SINGLE_LOOP("单曲循环"),
    LIST_LOOP("列表循环")
}

@kotlin.OptIn(ExperimentalFoundationApi::class)
@Composable
fun TikTokScrollPlayer(
    mediaList: List<MediaModel>,
    onPickFolder: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { mediaList.size })
    val scope = rememberCoroutineScope()

    // 默认开启列表循环
    var playMode by remember { mutableStateOf(PlayMode.LIST_LOOP) }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { pageIndex ->
            val isCurrentPage = (pagerState.currentPage == pageIndex)

            MediaPage(
                media = mediaList[pageIndex],
                isActive = isCurrentPage,
                playMode = playMode,
                onVideoEnded = {
                    // --- 修复点：列表循环逻辑 ---
                    if (playMode == PlayMode.LIST_LOOP && isCurrentPage) {
                        scope.launch {
                            if (pageIndex < mediaList.size - 1) {
                                // 还没到底，滑到下一个
                                pagerState.animateScrollToPage(pageIndex + 1)
                            } else {
                                // 到底了，跳回第一个 (实现无限循环)
                                pagerState.scrollToPage(0)
                            }
                        }
                    }
                }
            )
        }

        TopControlBar(
            onFolderClick = onPickFolder,
            onRotateClick = {
                val activity = context.findActivity()
                activity?.let { act ->
                    if (act.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        act.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
            },
            onModeClick = {
                playMode = if (playMode == PlayMode.SINGLE_LOOP) PlayMode.LIST_LOOP else PlayMode.SINGLE_LOOP
            },
            currentModeName = playMode.label
        )
    }
}

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun TopControlBar(
    onFolderClick: () -> Unit,
    onRotateClick: () -> Unit,
    onModeClick: () -> Unit,
    currentModeName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp)
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onFolderClick,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.Default.Search, contentDescription = "Folder", tint = Color.White)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TextButton(
                onClick = onModeClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Text(text = currentModeName, color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(
                onClick = onRotateClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Rotate", tint = Color.White)
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    uri: Uri,
    isResumed: Boolean,
    playMode: PlayMode,
    onEnded: () -> Unit
) {
    val context = LocalContext.current

    // --- 修复点：使用 rememberUpdatedState 防止闭包失效 ---
    // 这行代码解决了“自动连播失效”的 Bug
    val currentOnEnded by rememberUpdatedState(onEnded)

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
        }
    }

    LaunchedEffect(playMode) {
        if (playMode == PlayMode.SINGLE_LOOP) {
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        } else {
            exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    var isPlaying by remember { mutableStateOf(true) }
    var totalDuration by remember { mutableLongStateOf(0L) }
    var currentTime by remember { mutableLongStateOf(0L) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isDragging by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    totalDuration = exoPlayer.duration.coerceAtLeast(0L)
                }
                // 监听播放结束，并调用最新的回调
                if (state == Player.STATE_ENDED) {
                    currentOnEnded()
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isResumed) {
        if (isResumed) {
            exoPlayer.play()
            isPlaying = true
        } else {
            exoPlayer.pause()
            isPlaying = false
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            if (!isDragging && exoPlayer.isPlaying) {
                currentTime = exoPlayer.currentPosition.coerceAtLeast(0L)
            }
            delay(500)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                if (exoPlayer.isPlaying) {
                    exoPlayer.pause()
                    isPlaying = false
                } else {
                    exoPlayer.play()
                    isPlaying = true
                }
            }
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = {
                    val newSpeed = if (playbackSpeed == 1.0f) 2.0f else 1.0f
                    exoPlayer.setPlaybackSpeed(newSpeed)
                    playbackSpeed = newSpeed
                }) {
                    Text(
                        text = "倍速: ${playbackSpeed}x",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Text(
                    text = "${formatTime(currentTime)} / ${formatTime(totalDuration)}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Slider(
                value = currentTime.toFloat(),
                onValueChange = { newTime ->
                    isDragging = true
                    currentTime = newTime.toLong()
                },
                onValueChangeFinished = {
                    exoPlayer.seekTo(currentTime)
                    isDragging = false
                },
                valueRange = 0f..totalDuration.toFloat().coerceAtLeast(1f),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

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

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}