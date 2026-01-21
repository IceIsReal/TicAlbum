package com.skypan.tikalbum.ui.screens

import android.content.pm.ActivityInfo
import androidx.compose.foundation.ExperimentalFoundationApi // 1. 必须导入这个
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.* // 包含 getValue, setValue, remember 等
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.skypan.tikalbum.model.MediaModel
import com.skypan.tikalbum.model.PlayMode
import com.skypan.tikalbum.ui.components.MediaPage
import com.skypan.tikalbum.ui.components.TopControlBar
import com.skypan.tikalbum.utils.findActivity
import kotlinx.coroutines.launch

// 2. 在这里加上注解，给整个函数授权
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TikTokScrollPlayer(
    mediaList: List<MediaModel>,
    onPickFolder: () -> Unit
) {
    val context = LocalContext.current

    // 3. rememberPagerState 也是实验性的，会被上面的注解覆盖
    val pagerState = rememberPagerState(pageCount = { mediaList.size })
    val scope = rememberCoroutineScope()

    var playMode by remember { mutableStateOf(PlayMode.LIST_LOOP) }

    Box(modifier = Modifier.fillMaxSize()) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondBoundsPageCount = 1 // 4. 这个参数也是实验性的
        ) { pageIndex ->
            val isCurrentPage = (pagerState.currentPage == pageIndex)

            MediaPage(
                media = mediaList[pageIndex],
                isActive = isCurrentPage,
                playMode = playMode,
                onVideoEnded = {
                    if (playMode == PlayMode.LIST_LOOP && isCurrentPage) {
                        scope.launch {
                            if (pageIndex < mediaList.size - 1) {
                                pagerState.animateScrollToPage(pageIndex + 1)
                            } else {
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