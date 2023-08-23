package io.github.peacefulprogram.nivod_tv.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import io.github.peacefulprogram.nivod_api.NivodApi
import io.github.peacefulprogram.nivod_api.dto.ChannelInfo
import io.github.peacefulprogram.nivod_api.dto.ChannelRecommend
import io.github.peacefulprogram.nivod_tv.common.BasePageResult
import io.github.peacefulprogram.nivod_tv.common.BasicPagingSource
import io.github.peacefulprogram.nivod_tv.common.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(
    private val api: NivodApi
) : ViewModel() {

    private val TAG = MainViewModel::class.java.simpleName

    private val _channels = MutableStateFlow<Resource<List<ChannelInfo>>>(Resource.Loading)

    val channels: StateFlow<Resource<List<ChannelInfo>>>
        get() = _channels

    private var channelRecommendMap = emptyMap<Any, Flow<PagingData<ChannelRecommend>>>()

    init {
        loadChannels()
    }

    fun loadChannels() {
        viewModelScope.launch(Dispatchers.IO) {
            _channels.emit(Resource.Loading)
            val result = runCatching {
                api.queryChannels().list
            }
            result.onSuccess {
                val map: MutableMap<Any, Flow<PagingData<ChannelRecommend>>> = mutableMapOf()
                val channelList = it.filter { channelInfo -> "午夜" !in channelInfo.channelName }
                map.addChannelPagingDataFlow(null)
                channelList.forEach { channelInfo -> map.addChannelPagingDataFlow(channelInfo.channelId) }
                channelRecommendMap = map
                _channels.emit(Resource.Success(channelList))
            }.onFailure {
                Log.e(TAG, "loadChannels: ${it.message}", it)
                _channels.emit(Resource.Error("加载频道失败:${it.message}", it))
            }
        }
    }

    private fun MutableMap<Any, Flow<PagingData<ChannelRecommend>>>.addChannelPagingDataFlow(
        channelId: Int?
    ) {
        val pageSize = 6
        val key = channelId ?: -1
        var pager = this[key]
        if (pager == null) {
            pager = Pager(
                config = PagingConfig(pageSize, initialLoadSize = pageSize)
            ) {
                BasicPagingSource { page ->
                    val resp = api.queryRecommendationOfChannel((page - 1) * pageSize, channelId)
                    BasePageResult(
                        data = resp.list,
                        page = page,
                        hasNext = resp.more == 1
                    )
                }
            }
                .flow
                .cachedIn(viewModelScope)
            this[key] = pager
        }
    }

    fun getChannelRecommendPagingSource(channelId: Int?): Flow<PagingData<ChannelRecommend>> {
        return channelRecommendMap[channelId ?: -1]!!
    }
}