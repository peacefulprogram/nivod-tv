package io.github.peacefulprogram.nivod_api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoPlayLang(
    @SerialName("langId")
    val langId: Int,
    @SerialName("langName")
    val langName: String
)


@Serializable
data class VideoPlaySource(
    @SerialName("sourceId")
    val sourceId: Int,
    @SerialName("sourceName")
    val sourceName: String
)