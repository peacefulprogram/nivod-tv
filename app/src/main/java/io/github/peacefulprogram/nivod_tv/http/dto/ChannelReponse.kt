package io.github.peacefulprogram.nivod_tv.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChannelResponse(
    val channelId: Int,
    val channelName: String,
    val catId: Int,
    val notice: String? = null
)
