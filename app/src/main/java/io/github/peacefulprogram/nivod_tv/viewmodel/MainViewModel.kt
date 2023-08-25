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
import io.github.peacefulprogram.nivod_api.dto.ChannelRecommendShow
import io.github.peacefulprogram.nivod_tv.common.BasePageResult
import io.github.peacefulprogram.nivod_tv.common.BasicPagingSource
import io.github.peacefulprogram.nivod_tv.common.Resource
import io.github.peacefulprogram.nivod_tv.ext.runCoroutineCompatibleCatching
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
    private var channelRecommendMap = emptyMap<Int, Flow<PagingData<ChannelRecommend>>>()

    private var channelBannerMap =
        emptyMap<Int, MutableStateFlow<List<Pair<String, ChannelRecommendShow>>>>()

    init {
        loadChannels()
    }

    fun loadChannels() {
        viewModelScope.launch(Dispatchers.IO) {
            _channels.emit(Resource.Loading)
            val result = runCoroutineCompatibleCatching {
                api.queryChannels().list
            }
            result.onSuccess {
                val map: MutableMap<Int, Flow<PagingData<ChannelRecommend>>> = mutableMapOf()
                val bannerMap: MutableMap<Int, MutableStateFlow<List<Pair<String, ChannelRecommendShow>>>> =
                    mutableMapOf()
                val channelList = it.filter { channelInfo -> "午夜" !in channelInfo.channelName }
                map.addChannelPagingDataFlow(null)
                bannerMap[-1] = MutableStateFlow(emptyList())
                channelList.forEach { channelInfo -> map.addChannelPagingDataFlow(channelInfo.channelId) }
                channelList.forEach { channelInfo ->
                    bannerMap[channelInfo.channelId] = MutableStateFlow(emptyList())
                }
                channelRecommendMap = map
                channelBannerMap = bannerMap
                _channels.emit(Resource.Success(channelList))
            }.onFailure {
                Log.e(TAG, "loadChannels: ${it.message}", it)
                _channels.emit(Resource.Error("加载频道失败:${it.message}", it))
            }
        }
    }

    private fun MutableMap<Int, Flow<PagingData<ChannelRecommend>>>.addChannelPagingDataFlow(
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
                    if (resp.banners.isNotEmpty()) {
                        val bannerShows = resp.banners.filter { it.show != null }
                            .map { Pair(it.imageUrl, it.show!!) }
                        channelBannerMap[key]!!.emit(bannerShows)
                    }
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

    fun getChannelBanners(channelId: Int?): StateFlow<List<Pair<String, ChannelRecommendShow>>> {
        return channelBannerMap[channelId ?: -1]!!
    }
}