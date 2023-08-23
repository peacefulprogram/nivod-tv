package io.github.peacefulprogram.nivod_tv.room.dto

data class VideoPlayHistory(
    val showIdCode: String,
    val showTitle: String,
    val showImage: String,
    val playIdCode: String,
    val episodeName: String,
    val progress: Long,
    val duration: Long
)
