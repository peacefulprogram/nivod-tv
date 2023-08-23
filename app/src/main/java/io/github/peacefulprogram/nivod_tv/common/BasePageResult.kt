package io.github.peacefulprogram.nivod_tv.common

data class BasePageResult<T>(
    val data: List<T>,
    val page: Int,
    val hasNext: Boolean
)
