package com.skypan.tikalbum.ui.screens

import android.content.pm.ActivityInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skypan.tikalbum.model.MediaModel
import com.skypan.tikalbum.model.MediaType
import com.skypan.tikalbum.model.PlayMode
import com.skypan.tikalbum.ui.components.MediaPage
import com.skypan.tikalbum.ui.components.TopControlBar
import com.skypan.tikalbum.ui.viewmodel.PlayerViewModel
import com.skypan.tikalbum.utils.findActivity
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TikTokScrollPlayer(
    mediaList: List<MediaModel>,
    onPickFolder: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val playerViewModel: PlayerViewModel = viewModel()
    val pagerState = rememberPagerState(pageCount = { mediaList.size })
    var playMode by remember { mutableStateOf(PlayMode.LIST_LOOP) }

    val playNext = {
        scope.launch {
            if (pagerState.currentPage < mediaList.size - 1) {
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            } else {
                pagerState.scrollToPage(0)
            }
        }
    }

    // 确保回调始终能拿到最新的模式
    LaunchedEffect(playMode) {
        playerViewModel.onVideoEnded = {
            if (playMode == PlayMode.LIST_LOOP) {
                playNext()
            }
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (mediaList.isNotEmpty()) {
            val currentMedia = mediaList[pagerState.currentPage]
            if (currentMedia.type == MediaType.VIDEO) {
                playerViewModel.prepareAndPlay(currentMedia.uri, playMode)
            } else {
                playerViewModel.exoPlayer.pause()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondBoundsPageCount = 1
        ) { pageIndex ->
            MediaPage(
                media = mediaList[pageIndex],
                isActive = (pagerState.currentPage == pageIndex),
                playMode = playMode,
                playerViewModel = playerViewModel,
                onMediaEnded = { playNext() }
            )
        }

        TopControlBar(
            onFolderClick = onPickFolder,
            onRotateClick = {
                val activity = context.findActivity()
                activity?.let { act ->
                    act.requestedOrientation = if (act.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    } else {
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }
                }
            },
            onModeClick = {
                // --- 核心修复：切换时立即通知播放器更新模式 ---
                val newMode = if (playMode == PlayMode.SINGLE_LOOP) PlayMode.LIST_LOOP else PlayMode.SINGLE_LOOP
                playMode = newMode
                playerViewModel.handlePlayModeChange(newMode)
            },
            currentModeName = playMode.label,
            onSpeedClick = { playerViewModel.updateSpeed() },
            currentSpeed = playerViewModel.playbackSpeed
        )
    }
}