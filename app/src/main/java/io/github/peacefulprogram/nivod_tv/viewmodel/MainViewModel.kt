package io.github.peacefulprogram.nivod_tv.viewmodel

import android.content.Context
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
import io.github.peacefulprogram.nivod_tv.NivodApp
import io.github.peacefulprogram.nivod_tv.common.BasePageResult
import io.github.peacefulprogram.nivod_tv.common.BasicPagingSource
import io.github.peacefulprogram.nivod_tv.common.Resource
import io.github.peacefulprogram.nivod_tv.ext.runCoroutineCompatibleCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    private val sp = NivodApp.context.getSharedPreferences("channel", Context.MODE_PRIVATE)

    private val channelMutex = Mutex()


    init {
        loadChannelsFromSp()
        loadChannels()
    }

    private fun loadChannelsFromSp() {
        viewModelScope.launch(Dispatchers.Default) {
            val cachedChannels = sp.getString("ch", null) ?: "[]"
            kotlin.runCatching { ChannelInfo.fromJsonArray(cachedChannels) }
                .onSuccess { channelList ->
                    channelMutex.withLock {
                        if (channels.value !is Resource.Success) {
                            val actualChannels =
                                if (channelList.isNotEmpty()) channelList else buildDefaultChannels()
                            initChannelDataFlows(actualChannels)
                            _channels.emit(Resource.Success(actualChannels))
                        }
                    }
                }
        }
    }

    private fun buildDefaultChannels(): List<ChannelInfo> {
        return listOf(
            ChannelInfo(channelId = 1, channelName = "电影"),
            ChannelInfo(channelId = 2, channelName = "电视剧"),
            ChannelInfo(channelId = 3, channelName = "综艺"),
            ChannelInfo(channelId = 4, channelName = "动漫"),
            ChannelInfo(channelId = 6, channelName = "纪录片"),
            ChannelInfo(channelId = 15, channelName = "短视频"),
        )

    }

    fun loadChannels() {
        viewModelScope.launch(Dispatchers.IO) {
            _channels.emit(Resource.Loading)
            val result = runCoroutineCompatibleCatching {
                api.queryChannels().list
            }
            result.onSuccess { channelInfoList ->
                channelMutex.withLock {
                    val channelList =
                        channelInfoList.filter { channelInfo -> "午夜" !in channelInfo.channelName }
                    initChannelDataFlows(channelList)
                    sp.edit().putString("ch", ChannelInfo.toJson(channelList)).apply()
                    _channels.emit(Resource.Success(channelList))
                }
            }.onFailure {
                Log.e(TAG, "loadChannels: ${it.message}", it)
//                _channels.emit(Resource.Error("加载频道失败:${it.message}", it))
            }
        }
    }

    private fun initChannelDataFlows(channelList: List<ChannelInfo>): Unit {
        val map: MutableMap<Int, Flow<PagingData<ChannelRecommend>>> = mutableMapOf()
        val bannerMap: MutableMap<Int, MutableStateFlow<List<Pair<String, ChannelRecommendShow>>>> =
            mutableMapOf()
        map.addChannelPagingDataFlow(null)
        bannerMap[-1] = channelBannerMap[-1] ?: MutableStateFlow(emptyList())
        channelList.forEach { channelInfo -> map.addChannelPagingDataFlow(channelInfo.channelId) }
        channelList.forEach { channelInfo ->
            bannerMap[channelInfo.channelId] =
                channelBannerMap[channelInfo.channelId] ?: MutableStateFlow(emptyList())
        }
        channelRecommendMap = map
        channelBannerMap = bannerMap
    }

    private fun MutableMap<Int, Flow<PagingData<ChannelRecommend>>>.addChannelPagingDataFlow(
        channelId: Int?
    ) {
        val pageSize = 6
        val key = channelId ?: -1
        var pager = this[key]
        if (pager == null) {
            pager = channelRecommendMap[key]
        }
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
        }
        this[key] = pager
    }

    fun getChannelRecommendPagingSource(channelId: Int?): Flow<PagingData<ChannelRecommend>> {
        return channelRecommendMap[channelId ?: -1]!!
    }

    fun getChannelBanners(channelId: Int?): StateFlow<List<Pair<String, ChannelRecommendShow>>> {
        return channelBannerMap[channelId ?: -1]!!
    }
}