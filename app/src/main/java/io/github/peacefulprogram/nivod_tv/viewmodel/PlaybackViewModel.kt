package io.github.peacefulprogram.nivod_tv.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.peacefulprogram.nivod_api.NivodApi
import io.github.peacefulprogram.nivod_tv.common.Resource
import io.github.peacefulprogram.nivod_tv.ext.runCoroutineCompatibleCatching
import io.github.peacefulprogram.nivod_tv.playback.VideoEpisode
import io.github.peacefulprogram.nivod_tv.room.dao.EpisodeHistoryDao
import io.github.peacefulprogram.nivod_tv.room.dao.VideoHistoryDao
import io.github.peacefulprogram.nivod_tv.room.entity.EpisodeHistoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class PlaybackViewModel(
    private val showIdCode: String,
    val showTitle: String,
    initEpisodeIndex: Int,
    val episodes: List<VideoEpisode>,
    private val api: NivodApi,
    private val videoHistoryDao: VideoHistoryDao,
    private val episodeHistoryDao: EpisodeHistoryDao
) : ViewModel() {

    private var _saveHistoryJob: Job? = null
    private val TAG = PlaybackViewModel::class.java.simpleName
    private var fetchVideoUrlJob: Job? = null
    var currentEpisodeIndex = initEpisodeIndex
        private set

    private val _playbackEpisode = MutableStateFlow<Resource<PlayingEpisode>>(Resource.Loading)
    val playbackEpisode: StateFlow<Resource<PlayingEpisode>>
        get() = _playbackEpisode

    private val videoUrlCache: MutableMap<String, String> = ConcurrentHashMap()

    var currentPlayPosition: Long = 0L

    var videoDuration: Long = 0L

    init {
        changeEpisode(initEpisodeIndex)
    }

    fun changeEpisode(playIndex: Int) {
        currentEpisodeIndex = playIndex
        fetchVideoUrlJob?.cancel()
        fetchVideoUrlJob = null
        val episode = episodes[playIndex]
        val cacheUrl = videoUrlCache[episode.playIdCode]
        if (cacheUrl != null) {
            viewModelScope.launch(Dispatchers.Default) {
                val history = episodeHistoryDao.queryHistoryByEpisodeId(episode.playIdCode)
                _playbackEpisode.emit(
                    Resource.Success(
                        PlayingEpisode(
                            playIdCode = episode.playIdCode,
                            episodeName = episode.episodeName,
                            videoUrl = cacheUrl,
                            lastPlayPosition = history?.progress ?: 0L,
                            videoDuration = history?.duration ?: 0L
                        )
                    )
                )
                videoHistoryDao.updateLatestPlayedEpisode(showIdCode, episode.playIdCode)

            }
            return
        }
        fetchVideoUrlJob = viewModelScope.launch(Dispatchers.IO) {
            _playbackEpisode.emit(Resource.Loading)
            runCoroutineCompatibleCatching {
                api.queryVideoStreamUrl(showIdCode, episode.playIdCode)
            }.onSuccess { resp ->
                val history = episodeHistoryDao.queryHistoryByEpisodeId(episode.playIdCode)
                _playbackEpisode.emit(
                    Resource.Success(
                        PlayingEpisode(
                            playIdCode = episode.playIdCode,
                            episodeName = episode.episodeName,
                            videoUrl = resp.entity.playUrl,
                            lastPlayPosition = history?.progress ?: 0L,
                            videoDuration = history?.duration ?: 0L
                        )
                    )
                )
                videoHistoryDao.updateLatestPlayedEpisode(showIdCode, episode.playIdCode)
                episodeHistoryDao.save(
                    EpisodeHistoryEntity(
                        showIdCode = showIdCode,
                        playIdCode = episode.playIdCode,
                        episodeName = episode.episodeName,
                        progress = history?.progress ?: 0L,
                        duration = history?.duration ?: 0L,
                        updateTime = System.currentTimeMillis()
                    )
                )
            }.onFailure { ex ->
                Log.e(TAG, "changeEpisode: ${ex.message}", ex)
                _playbackEpisode.emit(Resource.Error("获取视频链接失败:${ex.message}", ex))

            }
        }
    }

    fun playNextEpisodeIfExists() {
        if (currentEpisodeIndex < episodes.size - 1) {
            changeEpisode(currentEpisodeIndex + 1)
        }
    }

    fun startSaveHistory() {
        stopSaveHistory()
        val epRes = playbackEpisode.value
        if (epRes !is Resource.Success) {
            return
        }
        val ep = epRes.data
        _saveHistoryJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                episodeHistoryDao.save(
                    EpisodeHistoryEntity(
                        playIdCode = ep.playIdCode,
                        showIdCode = showIdCode,
                        episodeName = ep.episodeName,
                        progress = currentPlayPosition,
                        duration = videoDuration,
                        updateTime = System.currentTimeMillis()
                    )
                )
                delay(5000L)
            }
        }
    }

    fun saveHistory() {
        val epRes = playbackEpisode.value
        if (epRes !is Resource.Success) {
            return
        }
        val ep = epRes.data
        viewModelScope.launch {
            episodeHistoryDao.save(
                EpisodeHistoryEntity(
                    playIdCode = ep.playIdCode,
                    showIdCode = showIdCode,
                    episodeName = ep.episodeName,
                    progress = currentPlayPosition,
                    duration = videoDuration,
                    updateTime = System.currentTimeMillis()
                )
            )
        }
    }

    fun stopSaveHistory() {
        _saveHistoryJob?.cancel()
        _saveHistoryJob = null
    }
}

data class PlayingEpisode(
    val playIdCode: String,
    val episodeName: String,
    val videoUrl: String,
    val lastPlayPosition: Long,
    val videoDuration: Long
)