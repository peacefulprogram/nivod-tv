package io.github.peacefulprogram.nivod_tv.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import io.github.peacefulprogram.nivod_api.dto.ChannelInfo
import io.github.peacefulprogram.nivod_tv.R
import io.github.peacefulprogram.nivod_tv.activity.VideoDetailActivity
import io.github.peacefulprogram.nivod_tv.common.Resource
import io.github.peacefulprogram.nivod_tv.common.compose.CustomTabRow
import io.github.peacefulprogram.nivod_tv.common.compose.ErrorTip
import io.github.peacefulprogram.nivod_tv.common.compose.FocusGroup
import io.github.peacefulprogram.nivod_tv.common.compose.Loading
import io.github.peacefulprogram.nivod_tv.common.compose.VideoCard
import io.github.peacefulprogram.nivod_tv.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvFoundationApi::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val channelResource = viewModel.channels.collectAsState().value
    if (channelResource is Resource.Loading) {
        Loading()
        return
    }
    if (channelResource is Resource.Error) {
        ErrorTip(message = channelResource.msg) {
            viewModel.loadChannels()
        }
        return
    }
    val channelFocusRequester = remember {
        FocusRequester()
    }
    val channels = (channelResource as Resource.Success<List<ChannelInfo>>).data
    val allChannelIdAndNames = remember(channels) {
        val channelIds = mutableListOf<Int?>(null)
        val channelNames = mutableListOf("综合")
        channels.forEach { channelInfo ->
            channelIds.add(channelInfo.channelId)
            channelNames.add(channelInfo.channelName)
        }
        Pair(
            channelIds,
            channelNames
        )
    }
    val context = LocalContext.current
    Column {
        var selectedTabIndex by remember {
            mutableIntStateOf(0)
        }
        CustomTabRow(
            selectedTabIndex = selectedTabIndex,
            tabs = allChannelIdAndNames.second,
            modifier = Modifier.focusRequester(channelFocusRequester)
        ) { selectedIndex ->
            selectedTabIndex = selectedIndex
        }

        LaunchedEffect(Unit) {
            runCatching { channelFocusRequester.requestFocus() }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val pagingData =
            viewModel.getChannelRecommendPagingSource(allChannelIdAndNames.first[selectedTabIndex])
                .collectAsLazyPagingItems()
        val refreshState = pagingData.loadState.refresh
        if (refreshState == LoadState.Loading) {
            Loading()
            return
        }
        if (refreshState is LoadState.Error) {
            ErrorTip(message = "加载数据失败:${refreshState.error.message}") {
                pagingData.refresh()
            }
            return
        }
        val videoCardWidth = dimensionResource(id = R.dimen.video_preview_card_width)
        val videoCardHeight = dimensionResource(id = R.dimen.video_preview_card_height)
        val focusScale = 1.1f
        val verticalGap = videoCardHeight * (focusScale - 1f)
        val horizontalGap = videoCardHeight * (focusScale - 1f)

        val lazyColumnState = rememberTvLazyListState()
        val coroutineScope = rememberCoroutineScope()
        TvLazyColumn(
            state = lazyColumnState,
            content = {
                items(
                    count = pagingData.itemCount,
                    key = { pagingData[it]?.blockId ?: "" }) { blockIndex ->
                    val block = pagingData[blockIndex] ?: return@items
                    Column {
                        val videosInBlock = remember(block) {
                            block.rows.flatMap { it.cells }.map { it.show }
                        }
                        Text(text = block.title)
                        Spacer(modifier = Modifier.height(verticalGap))
                        FocusGroup {
                            TvLazyRow(
                                content = {
                                    item {
                                        Spacer(modifier = Modifier.width(horizontalGap))
                                    }
                                    itemsIndexed(
                                        videosInBlock,
                                        key = { _, show -> show.showId }) { videoIndex, video ->
                                        val modifier = Modifier.run {
                                            if (videoIndex == 0) {
                                                this
                                                    .initiallyFocused()
                                                    .onKeyEvent { keyEvent ->
                                                        keyEvent.key == Key.DirectionLeft
                                                    }
                                            } else {
                                                this.restorableFocus()
                                            }
                                        }
                                        VideoCard(
                                            modifier = modifier,
                                            width = videoCardWidth,
                                            height = videoCardHeight,
                                            video = video,
                                            onVideoClick = {
                                                VideoDetailActivity.startActivity(
                                                    context,
                                                    it.showIdCode
                                                )
                                            },
                                            onVideoKeyEvent = { _, keyEvent ->
                                                if (keyEvent.key == Key.Back && keyEvent.type == KeyEventType.KeyUp) {
                                                    coroutineScope.launch {
                                                        lazyColumnState.scrollToItem(0)
                                                        channelFocusRequester.requestFocus()
                                                    }
                                                    true
                                                } else if (keyEvent.key == Key.Menu && keyEvent.type == KeyEventType.KeyUp) {
                                                    pagingData.refresh()
                                                    channelFocusRequester.requestFocus()
                                                    true
                                                } else {
                                                    false
                                                }
                                            }
                                        )
                                    }
                                    item {
                                        Spacer(modifier = Modifier.width(horizontalGap))
                                    }
                                })
                        }
                        Spacer(modifier = Modifier.height(verticalGap))
                    }
                }
            })


    }
}