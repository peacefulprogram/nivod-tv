package io.github.peacefulprogram.nivod_tv.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.foundation.lazy.grid.TvGridCells
import androidx.tv.foundation.lazy.grid.TvGridItemSpan
import androidx.tv.foundation.lazy.grid.TvLazyVerticalGrid
import androidx.tv.foundation.lazy.grid.rememberTvLazyGridState
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ClickableSurfaceScale
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import io.github.peacefulprogram.nivod_tv.R
import io.github.peacefulprogram.nivod_tv.activity.VideoDetailActivity
import io.github.peacefulprogram.nivod_tv.common.compose.ErrorTip
import io.github.peacefulprogram.nivod_tv.common.compose.FocusGroup
import io.github.peacefulprogram.nivod_tv.common.compose.Loading
import io.github.peacefulprogram.nivod_tv.common.compose.VideoCard
import io.github.peacefulprogram.nivod_tv.viewmodel.AppliedSearchConditions
import io.github.peacefulprogram.nivod_tv.viewmodel.CategoriesViewModel
import io.github.peacefulprogram.nivod_tv.viewmodel.ConditionRow
import kotlinx.coroutines.launch

typealias VideoFilter = List<Triple<Int, String, List<Pair<String, String>>>>

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoriesScreen(
    viewModel: CategoriesViewModel
) {
    val pagingItems = viewModel.pager.collectAsLazyPagingItems()
    var showFilterDialog by remember {
        mutableStateOf(false)
    }

    val state = rememberTvLazyGridState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusScale = 1.1f

    val cardWidth = dimensionResource(id = R.dimen.video_preview_card_width)
    val cardHeight = dimensionResource(id = R.dimen.video_preview_card_height)
    val videoCardContainerWidth = cardWidth * focusScale
    val videoCardContainerHeight = cardHeight * focusScale

    val refreshState = pagingItems.loadState.refresh
    val titleFocusRequester = remember {
        FocusRequester()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        TvLazyVerticalGrid(
            columns = TvGridCells.Adaptive(videoCardContainerWidth),
            horizontalArrangement = Arrangement.SpaceEvenly,
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .onPreviewKeyEvent {
                    if (it.key == Key.Back && it.type == KeyEventType.KeyUp && state.firstVisibleItemIndex > 0) {
                        coroutineScope.launch {
                            state.scrollToItem(0)
                            titleFocusRequester.requestFocus()
                        }
                        true
                    } else if (it.key == Key.Menu && it.type == KeyEventType.KeyUp) {
                        pagingItems.refresh()
                        titleFocusRequester.requestFocus()
                        true
                    } else {
                        false
                    }

                },
            content = {
                item(span = {
                    TvGridItemSpan(maxLineSpan)
                }) {
                    Row(
                        verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(
                            start = 15.dp,
                            top = 15.dp
                        )
                    ) {
                        Surface(
                            onClick = { },
                            onLongClick = { showFilterDialog = true },
                            colors = ClickableSurfaceDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surface),
                            scale = ClickableSurfaceScale.None,
                            modifier = Modifier.run {
                                if (pagingItems.itemCount == 0) {
                                    focusRequester(titleFocusRequester)
                                } else this
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.video_category_title),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Text(
                            text = stringResource(R.string.category_change_filter_tip),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                items(count = pagingItems.itemCount) { videoIndex ->
                    val video = pagingItems[videoIndex]!!
                    Box(
                        modifier = Modifier.size(videoCardContainerWidth, videoCardContainerHeight),
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
                                VideoDetailActivity.startActivity(context, it.showIdCode)
                            },
                            onVideoLongClick = {
                                showFilterDialog = true
                            })
                    }
                }

                if (refreshState is LoadState.Error) {
                    item(span = { TvGridItemSpan(maxLineSpan) }) {
                        ErrorTip(message = "加载失败:${refreshState.error.message}") {
                            pagingItems.retry()
                        }
                    }
                } else if (pagingItems.itemCount == 0 && refreshState != LoadState.Loading) {
                    item(span = { TvGridItemSpan(maxLineSpan) }) {
                        Text(
                            text = stringResource(R.string.grid_no_data_tip),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            })
        LaunchedEffect(refreshState) {
            titleFocusRequester.requestFocus()
        }
        if (refreshState == LoadState.Loading) {
            Loading()
        }

    }

    AnimatedVisibility(
        visible = showFilterDialog, enter = fadeIn(), exit = fadeOut()
    ) {
        VideoFilterDialog(
            viewModel = viewModel,
            onApply = {
                pagingItems.refresh()
                coroutineScope.launch {
                    state.scrollToItem(0)
                    titleFocusRequester.requestFocus()
                }
                showFilterDialog = false
            }) {
            showFilterDialog = false
        }
    }
}

@OptIn(
    ExperimentalTvMaterial3Api::class
)
@Composable
fun VideoFilterDialog(
    viewModel: CategoriesViewModel,
    onApply: () -> Unit,
    onCancel: () -> Unit
) {
    val currentCondition = viewModel.currentSettingCondition.collectAsState().value
    val coroutineScope = rememberCoroutineScope()
    val channelConditionRow = viewModel.channelConditionRow.collectAsState().value
    val typeConditionRow = viewModel.typeConditionRow.collectAsState().value
    val regionConditionRow = viewModel.regionConditionRow.collectAsState().value
    val langConditionRow = viewModel.langConditionRow.collectAsState().value
    val yearConditionRow = viewModel.yearConditionRow.collectAsState().value
    val sortConditionRow = viewModel.sortConditionRow.collectAsState().value
    val closeDialog: () -> Unit = {
        coroutineScope.launch {
            if (viewModel.applyUserCondition()) {
                onApply()
            } else {
                onCancel()
            }
        }
    }
    AlertDialog(
        onDismissRequest = closeDialog,
        confirmButton = {},
        modifier = Modifier.fillMaxWidth(0.8f),
        title = {
            Text(
                text = stringResource(id = R.string.video_filter_dialog_title),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
        text = {
            val focusRequester = remember {
                FocusRequester()
            }
            TvLazyColumn(
                content = {
                    item {
                        ConditionRow(
                            modifier = Modifier.focusRequester(focusRequester),
                            conditionRow = channelConditionRow,
                            currentCondition = currentCondition
                        ) {
                            viewModel.applyNewCondition(channelConditionRow, it)
                        }
                    }
                    item {
                        ConditionRow(
                            conditionRow = typeConditionRow,
                            currentCondition = currentCondition
                        ) {
                            viewModel.applyNewCondition(typeConditionRow, it)
                        }
                    }
                    item {
                        ConditionRow(
                            conditionRow = regionConditionRow,
                            currentCondition = currentCondition
                        ) {
                            viewModel.applyNewCondition(regionConditionRow, it)
                        }
                    }
                    item {
                        ConditionRow(
                            conditionRow = langConditionRow,
                            currentCondition = currentCondition
                        ) {
                            viewModel.applyNewCondition(langConditionRow, it)
                        }
                    }
                    item {
                        ConditionRow(
                            conditionRow = yearConditionRow,
                            currentCondition = currentCondition
                        ) {
                            viewModel.applyNewCondition(yearConditionRow, it)
                        }
                    }
                    item {
                        ConditionRow(
                            conditionRow = sortConditionRow,
                            currentCondition = currentCondition
                        ) {
                            viewModel.applyNewCondition(sortConditionRow, it)
                        }
                    }
                },
                verticalArrangement = spacedBy(8.dp),
                modifier = Modifier.onPreviewKeyEvent {
                    if (it.key == Key.Back && it.type == KeyEventType.KeyUp) {
                        closeDialog()
                        true
                    } else {
                        false
                    }
                })

            LaunchedEffect(Unit) {
                kotlin.runCatching { focusRequester.requestFocus() }
            }
        })


}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvFoundationApi::class)
@Composable
fun <T> ConditionRow(
    modifier: Modifier = Modifier,
    conditionRow: ConditionRow<T>,
    currentCondition: AppliedSearchConditions,
    onConditionClick: (value: T) -> Unit = {}
) {
    if (conditionRow.conditionList.isEmpty()) {
        return
    }

    val currentValue = conditionRow.conditionProperty.get(currentCondition)
    val lazyRowState = rememberTvLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = conditionRow.rowName)
        Spacer(modifier = Modifier.width(20.dp))
        FocusGroup(modifier = modifier) {
            TvLazyRow(
                content = {
                    itemsIndexed(
                        conditionRow.conditionList,
                        key = { _, cond -> cond.first }) { condIdx, condition ->
                        val selected = condition.second == currentValue
                        FocusableFilterChip(
                            text = condition.first,
                            selected = selected,
                            modifier = Modifier.run {
                                if (selected) {
                                    initiallyFocused()
                                } else {
                                    restorableFocus()
                                }
                            }
                        ) {
                            if (condition.second != currentValue) onConditionClick(condition.second)
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                horizontalArrangement = spacedBy(8.dp),
                state = lazyRowState
            )
        }
    }
    LaunchedEffect(Unit) {
        val idx = conditionRow.conditionList.indexOfFirst { it.second == currentValue }
        if (idx != -1) {
            coroutineScope.launch {
                lazyRowState.scrollToItem(idx)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTvMaterial3Api::class)
@Composable
fun FocusableFilterChip(
    text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit = {}
) {
    var focused by remember {
        mutableStateOf(false)
    }
    FilterChip(selected = selected, onClick = onClick, label = {
        Text(text = text)
    }, border = if (focused) FilterChipDefaults.filterChipBorder(
        borderColor = MaterialTheme.colorScheme.border,
        selectedBorderColor = MaterialTheme.colorScheme.border,
        borderWidth = 2.dp,
        selectedBorderWidth = 2.dp
    ) else FilterChipDefaults.filterChipBorder(
        borderColor = Color.Transparent, selectedBorderColor = Color.Transparent
    ), modifier = modifier.onFocusChanged {
        focused = it.isFocused || it.hasFocus
    })
}
