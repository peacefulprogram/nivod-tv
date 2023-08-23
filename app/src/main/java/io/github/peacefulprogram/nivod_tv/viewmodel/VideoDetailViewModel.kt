package io.github.peacefulprogram.nivod_tv.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.peacefulprogram.nivod_api.NivodApi
import io.github.peacefulprogram.nivod_api.dto.VideoDetailEntity
import io.github.peacefulprogram.nivod_api.dto.VideoDetailRecommend
import io.github.peacefulprogram.nivod_tv.common.Resource
import io.github.peacefulprogram.nivod_tv.room.dao.EpisodeHistoryDao
import io.github.peacefulprogram.nivod_tv.room.dao.VideoHistoryDao
import io.github.peacefulprogram.nivod_tv.room.entity.EpisodeHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VideoDetailViewModel(
    private val showIdCode: String,
    private val api: NivodApi,
    private val episodeHistoryDao: EpisodeHistoryDao,
    private val videoHistoryDao: VideoHistoryDao
) : ViewModel() {

    private val TAG = VideoDetailViewModel::class.java.simpleName

    private val _videoInfo = MutableStateFlow<Resource<VideoDetailEntity>>(Resource.Loading)

    val videoInfo: StateFlow<Resource<VideoDetailEntity>>
        get() = _videoInfo

    private val _recommends =
        MutableStateFlow<Resource<List<VideoDetailRecommend>>>(Resource.Loading)
    val recommends: StateFlow<Resource<List<VideoDetailRecommend>>>
        get() = _recommends


    private val _latestProgress: MutableStateFlow<Resource<EpisodeHistoryEntity>> =
        MutableStateFlow(Resource.Loading)

    val latestProgress: StateFlow<Resource<EpisodeHistoryEntity>>
        get() = _latestProgress

    init {
        loadVideoDetail()
        viewModelScope.launch {
            _videoInfo.collectLatest { res ->
                if (res is Resource.Success) {
                    with(res.data) {
                        loadRecommend(showTypeId, channelId)
                    }
                }
            }
        }
    }

    fun loadVideoDetail() {
        viewModelScope.launch(Dispatchers.IO) {
            _videoInfo.emit(Resource.Loading)
            runCatching {
                api.queryVideoDetail(showIdCode).entity
            }.onSuccess {
                _videoInfo.emit(Resource.Success(it))
            }.onFailure { ex ->
                Log.e(TAG, "loadVideoDetail: ${ex.message}", ex)
                _videoInfo.emit(Resource.Error("查询详情失败:${ex.message}", ex))
            }
        }
    }

    fun reloadRecommend() {
        val res = _videoInfo.value
        if (res !is Resource.Success) {
            return
        }
        loadRecommend(res.data.showTypeId, res.data.channelId)
    }

    private fun loadRecommend(showTypeId: Int, channelId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _recommends.emit(Resource.Loading)
            runCatching {
                api.queryVideoDetailRecommend(channelId, showTypeId).list
            }.onSuccess {
                _recommends.emit(Resource.Success(it))
            }.onFailure { ex ->
                Log.e(TAG, "loadRecommend: ${ex.message}", ex)
                _recommends.emit(Resource.Error("加载推荐视频失败:${ex.message}", ex))
            }
        }
    }

    fun fetchHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            episodeHistoryDao.queryLatestProgress(showIdCode)?.let {
                _latestProgress.emit(Resource.Success(it))
            }
        }
    }

    suspend fun saveVideoHistory(videoDetail: VideoDetailEntity) {
        videoHistoryDao.saveVideo(videoDetail)
    }
}