package io.github.peacefulprogram.nivod_tv.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyListState
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.Border
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ClickableSurfaceScale
import androidx.tv.material3.CompactCard
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.ProvideTextStyle
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import io.github.peacefulprogram.nivod_api.dto.VideoDetailEntity
import io.github.peacefulprogram.nivod_api.dto.VideoDetailPlay
import io.github.peacefulprogram.nivod_api.dto.VideoDetailRecommend
import io.github.peacefulprogram.nivod_tv.R
import io.github.peacefulprogram.nivod_tv.activity.PlaybackActivity
import io.github.peacefulprogram.nivod_tv.activity.VideoDetailActivity
import io.github.peacefulprogram.nivod_tv.common.Resource
import io.github.peacefulprogram.nivod_tv.common.compose.ErrorTip
import io.github.peacefulprogram.nivod_tv.common.compose.FocusGroup
import io.github.peacefulprogram.nivod_tv.common.compose.Loading
import io.github.peacefulprogram.nivod_tv.common.compose.VideoCard
import io.github.peacefulprogram.nivod_tv.ext.secondsToDuration
import io.github.peacefulprogram.nivod_tv.playback.VideoEpisode
import io.github.peacefulprogram.nivod_tv.viewmodel.VideoDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoDetailScreen(viewModel: VideoDetailViewModel) {
    val videoDetailResource = viewModel.videoInfo.collectAsState().value
    if (videoDetailResource == Resource.Loading) {
        Loading()
        return
    }
    if (videoDetailResource is Resource.Error) {
        ErrorTip(message = videoDetailResource.msg) {
            viewModel.loadVideoDetail()
        }
        return
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                viewModel.fetchHistory()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val videoDetail = (videoDetailResource as Resource.Success<VideoDetailEntity>).data
    val context = LocalContext.current
    var reverseEpisode by remember {
        mutableStateOf(false)
    }

    val playlistEpisodes = remember(reverseEpisode, videoDetail.plays) {
        if (reverseEpisode) {
            videoDetail.plays.reversed()
        } else {
            videoDetail.plays
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var shouldFocusFirstRecommend = rememberSaveable {
        false
    }
    TvLazyColumn(
        modifier = Modifier.fillMaxSize(), content = {
            item {
                VideoInfoRow(videoDetail = videoDetail, viewModel = viewModel)
            }
            item {
                PlayListRow(episodes = playlistEpisodes, title = {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(id = R.string.playlist_name),
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(text = " | ")
                        Surface(
                            onClick = {
                                reverseEpisode = !reverseEpisode
                            },
                            scale = ClickableSurfaceScale.None,
                            colors = ClickableSurfaceDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small),
                            border = ClickableSurfaceDefaults.border(
                                focusedBorder = Border(
                                    BorderStroke(
                                        2.dp, MaterialTheme.colorScheme.border
                                    )
                                )
                            ),
                        ) {
                            Text(
                                text = if (reverseEpisode) "倒序" else "正序",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(8.dp, 4.dp)
                            )
                        }
                    }

                }, onEpisodeClick = { epIndex, _ ->
                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.saveVideoHistory(videoDetail)
                        val eps = videoDetail.plays.map {
                            VideoEpisode(
                                playIdCode = it.playIdCode,
                                episodeName = it.episodeName
                            )
                        }
                        val playIndex = if (reverseEpisode) eps.size - epIndex - 1 else epIndex
                        PlaybackActivity.startActivity(
                            context = context,
                            showIdCode = videoDetail.showIdCode,
                            showTitle = videoDetail.showTitle,
                            playEpIndex = playIndex,
                            episodes = eps
                        )
                    }
                })
            }
            item {
                when (val recommendRes = viewModel.recommends.collectAsState().value) {
                    is Resource.Loading -> {
                        Loading()
                    }

                    is Resource.Success -> {
                        RelativeVideoRow(
                            recommendRes.data,
                            shouldFocusFirstOne = shouldFocusFirstRecommend
                        ) {
                            shouldFocusFirstRecommend = true
                            viewModel.reloadRecommend()
                            true
                        }
                        if (shouldFocusFirstRecommend) shouldFocusFirstRecommend = false
                    }

                    is Resource.Error -> {
                        ErrorTip(message = recommendRes.msg)
                    }
                }
            }
        }, verticalArrangement = Arrangement.spacedBy(10.dp)
    )
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvFoundationApi::class)
@Composable
fun RelativeVideoRow(
    videos: List<VideoDetailRecommend>,
    shouldFocusFirstOne: Boolean,
    onRequestRefresh: () -> Boolean = { false }
) {
    if (videos.isEmpty()) {
        return
    }
    val firstItemFocusRequester = remember {
        FocusRequester()
    }
    val context = LocalContext.current
    val cardWidth = dimensionResource(id = R.dimen.video_preview_card_width) * 0.8f
    val cardHeight = dimensionResource(id = R.dimen.video_preview_card_height) * 0.8f
    val horizontalGap = cardWidth * 0.1f
    val verticalGap = 5.dp + cardHeight * 0.1f
    Column {
        Text(text = stringResource(id = R.string.related_videos))
        Spacer(modifier = Modifier.height(verticalGap))
        FocusGroup {
            TvLazyRow(
                content = {
                    item { Spacer(modifier = Modifier.width(horizontalGap)) }
                    itemsIndexed(videos, key = { _, vod -> vod.showIdCode }) { videoIndex, video ->
                        val modifier = Modifier.run {
                            if (videoIndex == 0) {
                                this
                                    .initiallyFocused()
                                    .focusRequester(firstItemFocusRequester)
                            } else {
                                this.restorableFocus()
                            }
                        }
                        VideoCard(
                            modifier,
                            width = cardWidth,
                            height = cardHeight,
                            video = video,
                            onVideoClick = {
                                VideoDetailActivity.startActivity(context, it.showIdCode)
                            },
                            onVideoKeyEvent = { _, keyEvent ->
                                if (keyEvent.key == Key.Menu && keyEvent.type == KeyEventType.KeyUp) {
                                    onRequestRefresh()
                                } else videoIndex == 0 && keyEvent.key == Key.DirectionLeft
                            })
                    }
                    item { Spacer(modifier = Modifier.width(horizontalGap)) }
                },
            )
        }
        Spacer(modifier = Modifier.height(verticalGap))
    }
    LaunchedEffect(shouldFocusFirstOne) {
        if (shouldFocusFirstOne && videos.isNotEmpty()) {
            runCatching { firstItemFocusRequester.requestFocus() }
        }
    }
}

@OptIn(ExperimentalTvFoundationApi::class)
@Composable
fun PlayListRow(
    episodes: List<VideoDetailPlay>,
    title: @Composable () -> Unit,
    listState: TvLazyListState = rememberTvLazyListState(),
    onEpisodeClick: (episodeIndex: Int, episode: VideoDetailPlay) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        title()
        Spacer(modifier = Modifier.height(5.dp))
        FocusGroup {
            TvLazyRow(
                state = listState, content = {
                    item { Spacer(modifier = Modifier.width(5.dp)) }
                    itemsIndexed(items = episodes, key = { _, ep -> ep.episodeId }) { epIndex, ep ->
                        VideoTag(
                            modifier = Modifier.run {
                                if (epIndex == 0) {
                                    initiallyFocused()
                                } else {
                                    restorableFocus()
                                }
                            }, tagName = ep.episodeName
                        ) {
                            onEpisodeClick(epIndex, ep)
                        }
                    }
                    item { Spacer(modifier = Modifier.width(5.dp)) }
                }, horizontalArrangement = Arrangement.spacedBy(5.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VideoInfoRow(videoDetail: VideoDetailEntity, viewModel: VideoDetailViewModel) {
    val focusRequester = remember {
        FocusRequester()
    }
    var showDescDialog by remember {
        mutableStateOf(false)
    }

    Row(
        Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.video_preview_card_height) * 1.3f + 10.dp)
    ) {
        CompactCard(
            onClick = {},
            image = {
                AsyncImage(
                    model = videoDetail.showImg,
                    contentDescription = videoDetail.showTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            },
            title = {},
            scale = CardDefaults.scale(focusedScale = 1f),
            modifier = Modifier
                .padding(2.dp)
                .focusRequester(focusRequester)
                .size(
                    dimensionResource(id = R.dimen.video_preview_card_width) * 1.3f,
                    dimensionResource(
                        id = R.dimen.video_preview_card_height
                    ) * 1.3f
                )
        )
        Spacer(modifier = Modifier.width(15.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = videoDetail.showTitle,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
            val playHistory = viewModel.latestProgress.collectAsState().value
            if (playHistory is Resource.Success) {
                val his = playHistory.data
                Text(
                    text = "上次播放到${his.episodeName} ${(his.progress / 1000).secondsToDuration()}/${(his.duration / 1000).secondsToDuration()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            ProvideTextStyle(value = MaterialTheme.typography.bodySmall) {
                TvLazyColumn(
                    content = {
                        item {
                            Text(text = stringResource(R.string.video_year) + ": " + videoDetail.postYear)
                        }
                        item {
                            Text(text = stringResource(R.string.video_region) + ": " + videoDetail.regionName)
                        }
                        if (videoDetail.actors.isNotEmpty()) {
                            item {
                                Text(text = stringResource(id = R.string.video_actor) + ": " + videoDetail.actors)
                            }
                        }
                        if (videoDetail.director.isNotEmpty()) {
                            item {
                                Text(text = stringResource(id = R.string.video_director) + ": " + videoDetail.director)
                            }
                        }
                        item {
                            Surface(
                                onClick = { showDescDialog = true },
                                scale = ClickableSurfaceScale.None,
                                colors = ClickableSurfaceDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                border = ClickableSurfaceDefaults.border(
                                    focusedBorder = Border(
                                        BorderStroke(
                                            2.dp, MaterialTheme.colorScheme.border
                                        )
                                    )
                                ),
                                shape = ClickableSurfaceDefaults.shape(MaterialTheme.shapes.extraSmall)
                            ) {
                                Text(
                                    text = videoDetail.showDesc,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(
                                        horizontal = 6.dp, vertical = 3.dp
                                    )
                                )
                            }
                        }
                    }, verticalArrangement = Arrangement.spacedBy(10.dp)
                )

            }

        }
    }

    // 在Dialog中显示视频简介
    AnimatedVisibility(visible = showDescDialog) {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val longDescFocusRequester = remember {
            FocusRequester()
        }
        AlertDialog(onDismissRequest = { showDescDialog = false },
            confirmButton = {},
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth(0.6f),
            title = {
                Text(
                    text = stringResource(R.string.video_description),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            },
            text = {
                Text(text = videoDetail.showDesc,
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .focusRequester(longDescFocusRequester)
                        .focusable()
                        .onPreviewKeyEvent {
                            val step = 70f
                            when (it.key) {
                                Key.DirectionUp -> {
                                    if (it.type == KeyEventType.KeyDown) {
                                        coroutineScope.launch {
                                            scrollState.animateScrollBy(-step)
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                }

                                Key.DirectionDown -> {
                                    if (it.type == KeyEventType.KeyDown) {
                                        coroutineScope.launch {
                                            scrollState.animateScrollBy(step)
                                        }
                                        true
                                    } else {
                                        false
                                    }

                                }

                                else -> false
                            }

                        })
                LaunchedEffect(Unit) {
                    longDescFocusRequester.requestFocus()
                }
            }

        )

    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun VideoTag(tagName: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier,
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.2f),
            focusedContainerColor = Color.White.copy(alpha = 0.2f),
            pressedContainerColor = Color.White.copy(alpha = 0.2f)
        ),
        shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small),
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(
                    width = 2.dp, color = MaterialTheme.colorScheme.border
                ), shape = MaterialTheme.shapes.small
            )
        ),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
        onClick = onClick
    ) {
        Text(
            modifier = Modifier.padding(6.dp, 3.dp), text = tagName, color = Color.White
        )
    }

}