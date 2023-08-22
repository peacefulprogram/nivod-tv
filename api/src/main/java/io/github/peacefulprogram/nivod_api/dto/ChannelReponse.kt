package io.github.peacefulprogram.nivod_api.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChannelResponse(
    val msg: String,
    val status: Int,
    val list: List<ChannelInfo>
)

@Serializable
data class ChannelInfo(
    val channelId: Int,
    val channelName: String,
    val catId: Int,
    val notice: String = ""
)
