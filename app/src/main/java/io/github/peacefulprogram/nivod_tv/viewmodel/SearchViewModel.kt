package io.github.peacefulprogram.nivod_tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import io.github.peacefulprogram.nivod_api.NivodApi
import io.github.peacefulprogram.nivod_tv.common.Resource
import io.github.peacefulprogram.nivod_tv.room.dao.SearchHistoryDao
import io.github.peacefulprogram.nivod_tv.room.entity.SearchHistoryEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchViewModel(
    private val api: NivodApi,
    private val searchHistoryDao: SearchHistoryDao
) : ViewModel() {

    private val _searchRecommend: MutableStateFlow<Resource<List<String>>> =
        MutableStateFlow(Resource.Loading)
    val searchRecommend: StateFlow<Resource<List<String>>>
        get() = _searchRecommend

    val searchHistoryPager = Pager(
        config = PagingConfig(pageSize = 10),
        initialKey = 1
    ) {
        searchHistoryDao.queryPaging()
    }
        .flow
        .cachedIn(viewModelScope)

    init {
        loadSearchRecommend()
    }

    private fun loadSearchRecommend() {
        viewModelScope.launch(Dispatchers.IO) {
            _searchRecommend.emit(Resource.Loading)
            try {
                _searchRecommend.emit(Resource.Success(api.queryHotKeyword().list.map { it.keyword }))
            } catch (ex: Exception) {
                if (ex is CancellationException) {
                    throw ex
                }
                _searchRecommend.emit(Resource.Error("查询失败:${ex.message}", ex))
            }
        }
    }

    suspend fun deleteSearchHistory(history: SearchHistoryEntity) {
        withContext(Dispatchers.IO) {
            searchHistoryDao.deleteHistory(history)
        }
    }

    suspend fun deleteAllHistory() {
        withContext(Dispatchers.IO) {
            searchHistoryDao.deleteAllHistory()
        }
    }

    fun saveHistory(keyword: String) {
        if (keyword.isBlank()) {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryDao.saveHistory(
                history = SearchHistoryEntity(
                    keyword = keyword.trim(),
                    updateTime = System.currentTimeMillis()
                )
            )
        }
    }
}