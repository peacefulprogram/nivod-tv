package io.github.peacefulprogram.nivod_tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import io.github.peacefulprogram.nivod_tv.room.dao.VideoHistoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlayHistoryViewModel(
    private val videoHistoryDao: VideoHistoryDao
) : ViewModel() {
    fun deleteHistoryByVideoId(videoId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            videoHistoryDao.deleteHistoryById(videoId)
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch(Dispatchers.Default) {
            videoHistoryDao.deleteAllVideoHistory()
        }
    }


    val pager = Pager(
        config = PagingConfig(20)
    ) {
        videoHistoryDao.queryAllPlayHistory()
    }
        .flow

}