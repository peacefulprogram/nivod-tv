package io.github.peacefulprogram.nivod_tv.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class NivodResponse<T>(
    val msg: String,
    val status: Int,
    val list: List<T>
)
