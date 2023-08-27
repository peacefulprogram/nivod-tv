package io.github.peacefulprogram.nivod_tv.screen

import android.Manifest
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.tv.foundation.ExperimentalTvFoundationApi
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.itemsIndexed
import androidx.tv.foundation.lazy.list.rememberTvLazyListState
import androidx.tv.material3.Border
import androidx.tv.material3.ButtonScale
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ClickableSurfaceScale
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.IconButton
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import io.github.peacefulprogram.nivod_tv.NivodApp
import io.github.peacefulprogram.nivod_tv.R
import io.github.peacefulprogram.nivod_tv.activity.SearchResultActivity
import io.github.peacefulprogram.nivod_tv.common.Resource
import io.github.peacefulprogram.nivod_tv.common.SpeechToTextParser
import io.github.peacefulprogram.nivod_tv.common.compose.ConfirmDeleteDialog
import io.github.peacefulprogram.nivod_tv.common.compose.FocusGroup
import io.github.peacefulprogram.nivod_tv.room.entity.SearchHistoryEntity
import io.github.peacefulprogram.nivod_tv.viewmodel.SearchViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel) {
    val context = LocalContext.current
    val handleSearchRequest = { keyword: String ->
        viewModel.saveHistory(keyword)
        SearchResultActivity.startActivityByKeyword(context, keyword)
    }
    Column(modifier = Modifier.padding(horizontal = 48.dp, vertical = 27.dp)) {
        InputKeywordRow(handleSearchRequest)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.recommend_search_kw),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                RecommendKeywordList(viewModel = viewModel, onKeywordClick = handleSearchRequest)
            }

            Spacer(modifier = Modifier.width(40.dp))


            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(id = R.string.search_history),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                SearchHistory(viewModel = viewModel, onKeywordClick = handleSearchRequest)
            }
        }
    }
}

@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalTvMaterial3Api::class
)
@Composable
fun InputKeywordRow(onSearch: (String) -> Unit) {
    val defaultFocusRequester = remember {
        FocusRequester()
    }
    val context = LocalContext.current
    val speechToTextParser = remember {
        SpeechToTextParser(context)
    }
    val permissionState = rememberPermissionState(permission = Manifest.permission.RECORD_AUDIO) {
        if (it) {
            speechToTextParser.startListening()
        }
    }
    var inputKeyword by remember {
        mutableStateOf("")
    }

    val sttState by speechToTextParser.state.collectAsState()
    LaunchedEffect(sttState) {
        if (!sttState.isSpeaking && sttState.text.isNotEmpty()) {
            inputKeyword = sttState.text.trim()
        }
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (NivodApp.isMicAvailable) {
            AnimatedContent(targetState = sttState.isSpeaking, label = "") { isSpeaking ->
                IconButton(
                    onClick = {
                        if (isSpeaking) {
                            speechToTextParser.stopListening()
                        } else {
                            if (permissionState.status.isGranted) {
                                speechToTextParser.startListening()
                            } else {
                                permissionState.launchPermissionRequest()
                            }
                        }
                    },
                    scale = ButtonScale.None,
                    modifier = Modifier.focusRequester(defaultFocusRequester)
                ) {
                    if (isSpeaking) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            tint = colorResource(id = R.color.red400),
                            contentDescription = "stop"
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Mic, contentDescription = "speak"
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
        TextField(value = inputKeyword,
            onValueChange = { inputKeyword = it },
            modifier = Modifier.weight(1f).run {
                if (!NivodApp.isMicAvailable) {
                    focusRequester(defaultFocusRequester)
                } else {
                    this
                }
            },
            placeholder = {
                if (sttState.isSpeaking) {
                    Text(text = stringResource(R.string.speak_search_keyword))
                } else {
                    Text(text = stringResource(R.string.input_search_keyword))
                }
            })
        Spacer(modifier = Modifier.width(20.dp))

        IconButton(
            onClick = {
                onSearch(inputKeyword.trim())
            }, enabled = inputKeyword.isNotBlank()
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "search")
        }
    }

    LaunchedEffect(sttState.isSpeaking) {
        defaultFocusRequester.requestFocus()
    }
}

@OptIn(ExperimentalTvFoundationApi::class)
@Composable
fun RecommendKeywordList(viewModel: SearchViewModel, onKeywordClick: (String) -> Unit = {}) {
    val recommendResource = viewModel.searchRecommend.collectAsState().value
    if (recommendResource !is Resource.Success) {
        return
    }
    val keywords = recommendResource.data
    FocusGroup {
        TvLazyColumn(
            content = {
                itemsIndexed(keywords, key = { _, kw -> kw }) { idx, keyword ->
                    Keyword(
                        text = keyword,
                        modifier = Modifier.run {
                            if (idx == 0) {
                                initiallyFocused()
                            } else {
                                restorableFocus()
                            }
                        }) {
                        onKeywordClick(keyword)
                    }
                }
            }, verticalArrangement = spacedBy(10.dp)
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Keyword(
    text: String,
    modifier: Modifier = Modifier,
    onLongClick: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    var focused by remember {
        mutableStateOf(false)
    }
    Surface(onClick = onClick,
        onLongClick = onLongClick,
        scale = ClickableSurfaceScale.None,
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                BorderStroke(
                    2.dp, MaterialTheme.colorScheme.border
                )
            )
        ),
        colors = ClickableSurfaceDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier.onFocusChanged {
            focused = it.isFocused || it.hasFocus
        }) {
        var textModifier = Modifier.padding(8.dp, 4.dp)
        if (focused) {
            textModifier = textModifier.basicMarquee()
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            modifier = textModifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalTvFoundationApi::class)
@Composable
fun SearchHistory(viewModel: SearchViewModel, onKeywordClick: (String) -> Unit) {
    val pagingItems = viewModel.searchHistoryPager.collectAsLazyPagingItems()
    if (pagingItems.loadState.refresh !is LoadState.NotLoading || pagingItems.itemCount == 0) {
        return
    }
    var confirmDeleteHistory by remember {
        mutableStateOf<SearchHistoryEntity?>(null)
    }
    val coroutineScope = rememberCoroutineScope()

    val listState = rememberTvLazyListState()
    FocusGroup {
        TvLazyColumn(
            state = listState, content = {
                items(pagingItems.itemCount, key = { pagingItems[it]?.keyword ?: it }) {
                    val history = pagingItems[it]
                    if (history != null) {
                        Keyword(text = history.keyword,
                            modifier = Modifier.restorableFocus(),
                            onLongClick = {
                                confirmDeleteHistory = history
                            }) {
                            onKeywordClick(history.keyword)
                        }
                    }
                }
            }, verticalArrangement = spacedBy(10.dp)
        )
    }

    val history = confirmDeleteHistory ?: return

    val confirmText = String.format(
        stringResource(
            id = R.string.confirm_delete_template
        ), confirmDeleteHistory?.keyword
    )
    ConfirmDeleteDialog(
        text = confirmText,
        onDeleteClick = {
            confirmDeleteHistory = null
            coroutineScope.launch {
                viewModel.deleteSearchHistory(history)
                pagingItems.refresh()
            }
        },
        onDeleteAllClick = {
            confirmDeleteHistory = null
            coroutineScope.launch {
                viewModel.deleteAllHistory()
                pagingItems.refresh()
            }
        },
        onCancel = {
            confirmDeleteHistory = null
        }
    )

}
