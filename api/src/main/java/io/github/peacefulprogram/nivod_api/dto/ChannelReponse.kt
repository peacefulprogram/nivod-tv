package io.github.peacefulprogram.nivod_api.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ChannelResponse(
    val msg: String,
    val status: Int,
    val list: List<ChannelInfo>
)

@Serializable
data class ChannelInfo(
    val channelId: Int,
    val channelName: String
) {
    companion object {
        fun fromJsonArray(json: String): List<ChannelInfo> = Json.decodeFromString(json)

        fun toJson(channels: List<ChannelInfo>) = Json.encodeToString(channels)
    }
}
