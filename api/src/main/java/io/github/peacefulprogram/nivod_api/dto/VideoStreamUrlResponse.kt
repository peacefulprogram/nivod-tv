package io.github.peacefulprogram.nivod_api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoStreamUrlResponse(
    @SerialName("entity")
    val entity: VideoStreamUrlEntity,
    @SerialName("msg")
    val msg: String,
    @SerialName("status")
    val status: Int
)

@Serializable
data class VideoStreamUrlEntity(
    @SerialName("playType")
    val playType: Int,
    @SerialName("playUrl")
    val playUrl: String
)