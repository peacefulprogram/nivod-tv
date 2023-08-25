package io.github.peacefulprogram.nivod_tv.room.dto

import io.github.peacefulprogram.nivod_api.BasicVideoInfo

data class VideoPlayHistory(
    val showIdCode: String,
    val showTitle: String,
    val showImage: String,
    val playIdCode: String,
    val episodeName: String,
    val progress: Long,
    val duration: Long
) : BasicVideoInfo {
    override val title: String
        get() = showTitle
    override val subTitle: String
        get() = episodeName
    override val imageUrl: String
        get() = showImage
}
