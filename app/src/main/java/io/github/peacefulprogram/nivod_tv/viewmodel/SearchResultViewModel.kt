package io.github.peacefulprogram.nivod_tv.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import io.github.peacefulprogram.nivod_api.NivodApi
import io.github.peacefulprogram.nivod_api.SearchKeywordType
import io.github.peacefulprogram.nivod_tv.common.BasePageResult
import io.github.peacefulprogram.nivod_tv.common.BasicPagingSource

class SearchResultViewModel(
    private val keyword: String,
    private val api: NivodApi
) : ViewModel() {


    private val pageSize = 20
    val pager = Pager(
        config = PagingConfig(pageSize)
    ) {
        BasicPagingSource { page ->
            val resp = api.searchVideo(keyword, (page - 1) * pageSize, SearchKeywordType.ALL)
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