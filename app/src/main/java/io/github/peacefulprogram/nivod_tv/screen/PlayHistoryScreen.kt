package io.github.peacefulprogram.nivod_tv.screen

import android.util.Log
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvGridItemSpan
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.rememberTvLazyGridState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import io.github.peacefulprogram.nivod_tv.R
import io.github.peacefulprogram.nivod_tv.activity.VideoDetailActivity
import io.github.peacefulprogram.nivod_tv.common.compose.ConfirmDeleteDialog
import io.github.peacefulprogram.nivod_tv.common.compose.ErrorTip
import io.github.peacefulprogram.nivod_tv.common.compose.Loading
import io.github.peacefulprogram.nivod_tv.common.compose.VideoCard
import io.github.peacefulprogram.nivod_tv.room.dto.VideoPlayHistory
import io.github.peacefulprogram.nivod_tv.viewmodel.PlayHistoryViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayHistoryScreen(viewModel: PlayHistoryViewModel) {
    val pagingItems = viewModel.pager.collectAsLazyPagingItems()
    val refreshState = pagingItems.loadState.refresh
    if (refreshState is LoadState.Loading) {
        Loading()
        return
    }
    if (refreshState is LoadState.Error) {
        ErrorTip(message = "加载错误:${refreshState.error.message}") {
            pagingItems.refresh()
        }
    }
    val cardWidth = dimensionResource(id = R.dimen.video_preview_card_width)
    val cardHeight = dimensionResource(id = R.dimen.video_preview_card_height)
    val containerWidth = cardWidth * 1.1f
    val containerHeight = cardHeight * 1.1f
    val context = LocalContext.current
    val gridState = rememberTvLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var confirmRemoveVideo by remember {
        mutableStateOf<VideoPlayHistory?>(null)
    }
    val titleFocusRequester = remember {
        FocusRequester()
    }
    Box(modifier = Modifier.fillMaxSize()) {
        TvLazyVerticalGrid(
            columns = TvGridCells.Adaptive(containerWidth),
            modifier = Modifier
                .fillMaxSize(),
            state = gridState,
            content = {
                item(span = { TvGridItemSpan(maxLineSpan) }) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = stringResource(R.string.playback_history),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier
                                .run {
                                    if (pagingItems.itemCount == 0) {
                                        focusRequester(titleFocusRequester)
                                    } else {
                                        this
                                    }
                                }
                                .focusable()
                        )

                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = stringResource(R.string.click_ok_del_tip),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                items(count = pagingItems.itemCount) { videoIndex ->
                    val video = pagingItems[videoIndex]!!
                    Box(
                        modifier = Modifier.size(containerWidth, containerHeight),
                        contentAlignment = Alignment.Center
                    ) {
                        VideoCard(
                            modifier = if (videoIndex == 0) Modifier.focusRequester(
                                titleFocusRequester
                            ) else Modifier,
                            width = cardWidth,
                            height = cardHeight,
                            video = video,
                            onVideoClick = {
                                VideoDetailActivity.startActivity(context, video.showIdCode)
                            },
                            onVideoLongClick = {
                                confirmRemoveVideo = video
                            },
                            onVideoKeyEvent = { _, keyEvent ->
                                if (keyEvent.key == Key.Menu && keyEvent.type == KeyEventType.KeyUp) {
                                    pagingItems.refresh()
                                    coroutineScope.launch {
                                        gridState.scrollToItem(0)
                                    }
                                    true
                                } else if (keyEvent.key == Key.Back && keyEvent.type == KeyEventType.KeyUp && gridState.firstVisibleItemIndex != 0) {
                                    coroutineScope.launch {
                                        gridState.scrollToItem(0)
                                        titleFocusRequester.requestFocus()
                                    }
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                    }
                }
            }
        )

        if (pagingItems.itemCount == 0) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(id = R.string.grid_no_data_tip),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    LaunchedEffect(refreshState) {
        try {
            titleFocusRequester.requestFocus()
        } catch (e: Exception) {
            Log.w("PlayHistoryScreen", "request focus error: ${e.message}", e)
        }
    }

    val removeVideo = confirmRemoveVideo ?: return
    ConfirmDeleteDialog(
        text = String.format(
            stringResource(id = R.string.confirm_delete_template),
            removeVideo.title
        ),
        onDeleteClick = {
            confirmRemoveVideo = null
            coroutineScope.launch {
                gridState.scrollToItem(0)
                titleFocusRequester.requestFocus()
            }
            viewModel.deleteHistoryByVideoId(removeVideo.showIdCode)
        },
        onDeleteAllClick = {
            confirmRemoveVideo = null
            coroutineScope.launch {
                gridState.scrollToItem(0)
                titleFocusRequester.requestFocus()
            }
            viewModel.deleteAllHistory()
        },
        onCancel = {
            confirmRemoveVideo = null
        }
    )
}
