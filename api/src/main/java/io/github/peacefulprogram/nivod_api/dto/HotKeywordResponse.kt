package io.github.peacefulprogram.nivod_api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class HotKeywordResponse(
    @SerialName("list")
    val list: List<HotKeyword>,
    @SerialName("msg")
    val msg: String,
    @SerialName("status")
    val status: Int
)

@Serializable
data class HotKeyword(
    @SerialName("color")
    val color: String,
    @SerialName("id")
    val id: Int,
    @SerialName("keyword")
    val keyword: String,
    @SerialName("newlyListed")
    val newlyListed: Int,
    @SerialName("rising")
    val rising: Int,
    @SerialName("seq")
    val seq: Int,
    @SerialName("team")
    val team: Int
)