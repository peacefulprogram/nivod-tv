package io.github.peacefulprogram.nivod_api.dto

import io.github.peacefulprogram.nivod_api.BasicFilterCondition
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
) : BasicFilterCondition<Int> {
    companion object {
        fun fromJsonArray(json: String): List<ChannelInfo> = Json.decodeFromString(json)

        fun toJson(channels: List<ChannelInfo>) = Json.encodeToString(channels)
    }

    override val conditionName: String
        get() = channelName
    override val conditionValue: Int
        get() = channelId
}
