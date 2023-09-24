package io.github.peacefulprogram.nivod_tv.screen

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
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
import androidx.tv.material3.CardScale
import androidx.tv.material3.Carousel
import androidx.tv.material3.CompactCard
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import io.github.peacefulprogram.nivod_api.dto.ChannelInfo
import io.github.peacefulprogram.nivod_tv.R
import io.github.peacefulprogram.nivod_tv.activity.CategoriesActivity
import io.github.peacefulprogram.nivod_tv.activity.PlayHistoryActivity
import io.github.peacefulprogram.nivod_tv.activity.SearchActivity
import io.github.peacefulprogram.nivod_tv.activity.SettingsActivity
import io.github.peacefulprogram.nivod_tv.activity.VideoDetailActivity
import io.github.peacefulprogram.nivod_tv.common.Resource
import io.github.peacefulprogram.nivod_tv.common.compose.CustomTabRow
import io.github.peacefulprogram.nivod_tv.common.compose.ErrorTip
import io.github.peacefulprogram.nivod_tv.common.compose.FocusGroup
import io.github.peacefulprogram.nivod_tv.common.compose.Loading
import io.github.peacefulprogram.nivod_tv.common.compose.VideoCard
import io.github.peacefulprogram.nivod_tv.viewmodel.MainViewModel
import kotlinx.coroutines.launch

private fun isRefreshEvent(event: KeyEvent) =
    event.key == Key.Menu && event.type == KeyEventType.KeyUp

private fun isBackToTopEvent(event: KeyEvent) =
    event.key == Key.Back && event.type == KeyEventType.KeyUp

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
        FocusGroup {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(
                    onClick = {
                        SearchActivity.startActivity(context)
                    }, modifier = Modifier.initiallyFocused()
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "search")
                }
                IconButton(
                    onClick = {
                        PlayHistoryActivity.startActivity(context)
                    }, modifier = Modifier.restorableFocus()
                ) {
                    Icon(
                        imageVector = Icons.Default.History, contentDescription = "history"
                    )
                }

                IconButton(
                    onClick = {
                        CategoriesActivity.startActivity(context)
                    }, modifier = Modifier.restorableFocus()
                ) {
                    Icon(
                        imageVector = Icons.Default.Category, contentDescription = "category"
                    )
                }
                IconButton(
                    onClick = {
                        SettingsActivity.startActivity(context)
                    }, modifier = Modifier.restorableFocus()
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings, contentDescription = "settings"
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(5.dp))

        val currentChannelId = allChannelIdAndNames.first[selectedTabIndex]
        val pagingData =
            viewModel.getChannelRecommendPagingSource(currentChannelId)
                .collectAsLazyPagingItems()

        CustomTabRow(
            modifier = Modifier
                .onPreviewKeyEvent {
                    if (isRefreshEvent(it)) {
                        pagingData.refresh()
                        true
                    } else {
                        false
                    }
                }
                .focusRequester(channelFocusRequester),
            selectedTabIndex = selectedTabIndex,
            tabs = allChannelIdAndNames.second,
            onTabClick = {
                CategoriesActivity.startActivity(
                    context,
                    allChannelIdAndNames.first[it]
                )
            }
        ) { selectedIndex ->
            selectedTabIndex = selectedIndex
        }

        LaunchedEffect(Unit) {
            runCatching { channelFocusRequester.requestFocus() }
        }

        Spacer(modifier = Modifier.height(10.dp))
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
        val bannerList = viewModel.getChannelBanners(currentChannelId)
            .collectAsState().value
        val lazyColumnState = rememberTvLazyListState()
        val coroutineScope = rememberCoroutineScope()
        TvLazyColumn(
            state = lazyColumnState,
            content = {
                if (bannerList.isNotEmpty()) {
                    item {
                        val switchAnimation = (slideInHorizontally() + fadeIn()).togetherWith(
                            (slideOutHorizontally() + fadeOut())
                        )
                        Carousel(
                            itemCount = bannerList.size,
                            contentTransformStartToEnd = switchAnimation,
                            contentTransformEndToStart = switchAnimation,
                            modifier = Modifier
                                .height(dimensionResource(id = R.dimen.video_preview_card_height) * 1.5f)
                                .onPreviewKeyEvent { keyEvent ->
                                    if (isBackToTopEvent(keyEvent)) {
                                        coroutineScope.launch {
                                            lazyColumnState.scrollToItem(0)
                                            channelFocusRequester.requestFocus()
                                        }
                                        true
                                    } else if (isRefreshEvent(event = keyEvent)) {
                                        pagingData.refresh()
                                        channelFocusRequester.requestFocus()
                                        true
                                    } else {
                                        false
                                    }
                                }
                        ) { bannerIndex ->
                            val banner = bannerList[bannerIndex]
                            val imageUrl = banner.first
                            val bannerShow = banner.second
                            CompactCard(
                                onClick = {
                                    VideoDetailActivity.startActivity(
                                        context,
                                        bannerShow.showIdCode
                                    )
                                },
                                scale = CardScale.None,
                                image = {
                                    AsyncImage(
                                        model = imageUrl,
                                        contentDescription = bannerShow.showTitle,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                },
                                title = {
                                    Text(
                                        text = bannerShow.showTitle,
                                        modifier = Modifier.padding(start = 20.dp)
                                    )
                                },
                                subtitle = {
                                    Text(
                                        text = bannerShow.episodesTxt,
                                        Modifier.padding(start = 20.dp, bottom = 10.dp)
                                    )
                                },
                                modifier = Modifier.padding(2.dp)
                            )
                        }
                    }
                }
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
                                                if (isBackToTopEvent(keyEvent)) {
                                                    coroutineScope.launch {
                                                        lazyColumnState.scrollToItem(0)
                                                        channelFocusRequester.requestFocus()
                                                    }
                                                    true
                                                } else if (isRefreshEvent(keyEvent)) {
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