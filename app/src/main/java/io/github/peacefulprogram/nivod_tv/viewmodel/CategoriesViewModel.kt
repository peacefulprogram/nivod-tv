package io.github.peacefulprogram.nivod_tv.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import io.github.peacefulprogram.nivod_api.BasicFilterCondition
import io.github.peacefulprogram.nivod_api.NivodApi
import io.github.peacefulprogram.nivod_api.dto.FilterConditionShowType
import io.github.peacefulprogram.nivod_api.dto.FilterConditionSort
import io.github.peacefulprogram.nivod_tv.common.BasePageResult
import io.github.peacefulprogram.nivod_tv.common.BasicPagingSource
import io.github.peacefulprogram.nivod_tv.ext.runCoroutineCompatibleCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty1

typealias FilterCondition<T> = Pair<String, T>

class CategoriesViewModel(
    private val api: NivodApi,
    defaultChannelId: Int?
) : ViewModel() {


    private val TAG = CategoriesViewModel::class.java.simpleName

    // 默认查询条件
    private val _defaultSearchCondition = AppliedSearchConditions(channelId = defaultChannelId ?: 1)

    // 搜索时使用的查询条件
    private val _currentSearchCondition = MutableStateFlow(_defaultSearchCondition.copy())

    // 暂存用户正在设置的查询条件
    private val _currentSettingCondition = MutableStateFlow(_defaultSearchCondition.copy())
    val currentSettingCondition: StateFlow<AppliedSearchConditions>
        get() = _currentSettingCondition

    private var commonSortConditionRow = emptyList<FilterConditionSort>()

    private val _channelConditionRow = MutableStateFlow(
        ConditionRow(
            rowName = "频道:",
            conditionProperty = AppliedSearchConditions::channelId,
        )
    )

    private val _sortConditionRow = MutableStateFlow(
        ConditionRow(
            rowName = "排序:",
            conditionProperty = AppliedSearchConditions::sortBy,
        )
    )

    private val _typeConditionRow = MutableStateFlow(
        ConditionRow(
            rowName = "类型:",
            conditionProperty = AppliedSearchConditions::showTypeId
        )
    )

    private val _regionConditionRow = MutableStateFlow(
        ConditionRow(
            rowName = "地区:",
            conditionProperty = AppliedSearchConditions::regionId
        )
    )

    private val _langConditionRow = MutableStateFlow(
        ConditionRow(
            rowName = "语言:",
            conditionProperty = AppliedSearchConditions::langId
        )
    )


    private val _yearConditionRow = MutableStateFlow(
        ConditionRow(
            rowName = "年份:",
            conditionProperty = AppliedSearchConditions::yearRange
        )
    )

    val channelConditionRow: StateFlow<ConditionRow<Int>>
        get() = _channelConditionRow
    val sortConditionRow: StateFlow<ConditionRow<Int>>
        get() = _sortConditionRow
    val typeConditionRow: StateFlow<ConditionRow<Int>>
        get() = _typeConditionRow
    val regionConditionRow: StateFlow<ConditionRow<Int>>
        get() = _regionConditionRow
    val langConditionRow: StateFlow<ConditionRow<Int>>
        get() = _langConditionRow
    val yearConditionRow: StateFlow<ConditionRow<String>>
        get() = _yearConditionRow

    private var sortConditionMap = emptyMap<Int, List<FilterConditionSort>>()

    private var typeConditionMap = emptyMap<Int, List<FilterConditionShowType>>()

    private val pageSize = 20

    val pager = Pager(
        config = PagingConfig(
            pageSize = pageSize
        )
    ) {
        BasicPagingSource { page ->
            val currentCond = _currentSearchCondition.value
            val resp = api.queryVideoOfCategories(
                sortBy = currentCond.sortBy,
                channelId = currentCond.channelId,
                showTypeId = currentCond.showTypeId,
                regionId = currentCond.regionId,
                langId = currentCond.langId,
                yearRange = currentCond.yearRange,
                start = (page - 1) * pageSize
            )
            BasePageResult(
                data = resp.list,
                page = page,
                hasNext = resp.more == 1
            )
        }
    }
        .flow
        .cachedIn(viewModelScope)

    init {
        queryFilterConditions()
    }

    suspend fun applyUserCondition(): Boolean {
        val searchCond = _currentSearchCondition.value
        val settingCond = _currentSettingCondition.value
        if (searchCond == settingCond) {
            return false
        }
        _currentSearchCondition.emit(settingCond.copy())
        return true
    }

    fun <T> applyNewCondition(conditionRow: ConditionRow<T>, conditionValue: T) {
        viewModelScope.launch {
            // 修改频道后，更新类型和排序
            if (conditionRow.conditionProperty.name == AppliedSearchConditions::channelId.name) {
                changeConditionRowByChannelId(
                    row = _sortConditionRow,
                    channelId = conditionValue as Int,
                    conditionMap = sortConditionMap,
                    addEmptyCondition = false,
                    defaultConditions = commonSortConditionRow
                )
                changeConditionRowByChannelId(
                    row = _typeConditionRow,
                    channelId = conditionValue,
                    conditionMap = typeConditionMap,
                    addEmptyCondition = true
                )
            }
            val newCond = _currentSettingCondition.value.copy().apply {
                conditionRow.conditionProperty.set(this, conditionValue)
            }
            _currentSettingCondition.emit(newCond)
        }
    }

    private suspend fun <T> changeConditionRowByChannelId(
        row: MutableStateFlow<ConditionRow<T>>,
        channelId: Int,
        conditionMap: Map<Int, List<BasicFilterCondition<T>>>,
        addEmptyCondition: Boolean,
        defaultConditions: List<BasicFilterCondition<T>>? = null
    ) {
        val newConditions = conditionMap[channelId] ?: defaultConditions ?: return
        val rowValue = row.value
        val condProp = rowValue.conditionProperty
        val currentRowValue = condProp.get(_currentSettingCondition.value)
        var resetRowValue = true
        val pairList = newConditions.map {
            if (resetRowValue && it.conditionValue == currentRowValue) {
                resetRowValue = false
            }
            Pair(it.conditionName, it.conditionValue)
        }
        val displayList = if (addEmptyCondition) {
            mutableListOf(
                Pair(
                    "全部",
                    rowValue.conditionProperty.get(_defaultSearchCondition)
                )
            ).apply {
                addAll(pairList)
            }
        } else {
            pairList
        }
        // 更新条件
        row.emit(row.value.copy(conditionList = displayList))
        if (resetRowValue) {
            // 重置当前行筛选条件
            _currentSettingCondition.emit(
                _currentSettingCondition.value.copy()
                    .apply { condProp.set(this, condProp.get(_defaultSearchCondition)) })
        }
    }

    private fun queryFilterConditions() {
        viewModelScope.launch(Dispatchers.IO) {
            runCoroutineCompatibleCatching {
                val resp = api.queryFilterCondition()
                sortConditionMap = resp.sortsMap
                typeConditionMap = resp.typesMap
                commonSortConditionRow = resp.sorts
                val currentChannelId = _defaultSearchCondition.channelId
                changeConditionRowByChannelId(
                    row = _sortConditionRow,
                    channelId = currentChannelId,
                    conditionMap = sortConditionMap,
                    addEmptyCondition = false,
                    defaultConditions = commonSortConditionRow
                )
                changeConditionRowByChannelId(
                    row = _typeConditionRow,
                    channelId = currentChannelId,
                    conditionMap = typeConditionMap,
                    addEmptyCondition = true,
                )
                setConditionRowConditions(
                    _channelConditionRow,
                    resp.channels.filter { "午夜" !in it.channelName },
                    false
                )
                setConditionRowConditions(_regionConditionRow, resp.regions, true)
                setConditionRowConditions(_yearConditionRow, resp.yearRanges, true)
                setConditionRowConditions(_langConditionRow, resp.langs, true)

            }
                .onFailure { ex ->
                    Log.e(TAG, "queryFilters: ${ex.message}", ex)
                }
        }
    }

    private suspend fun <T> setConditionRowConditions(
        row: MutableStateFlow<ConditionRow<T>>,
        conditions: List<BasicFilterCondition<T>>,
        addDefaultCondition: Boolean
    ) {
        val rowValue = row.value
        val pairList = conditions.map { Pair(it.conditionName, it.conditionValue) }
        val condList = if (addDefaultCondition) {
            mutableListOf(
                Pair(
                    "全部",
                    rowValue.conditionProperty.get(_defaultSearchCondition)
                )
            ).apply {
                addAll(pairList)
            }
        } else {
            pairList
        }
        row.emit(rowValue.copy(conditionList = condList))
    }


}


data class ConditionRow<T>(
    val rowName: String,
    val conditionProperty: KMutableProperty1<AppliedSearchConditions, T>,
    val conditionList: List<FilterCondition<T>> = emptyList()
)

data class AppliedSearchConditions(
    var sortBy: Int = 3,
    var channelId: Int = 1,
    var showTypeId: Int = 0,
    var regionId: Int = 0,
    var langId: Int = 0,
    var yearRange: String = ""
)