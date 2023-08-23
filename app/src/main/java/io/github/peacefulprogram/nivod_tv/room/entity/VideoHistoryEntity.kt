package io.github.peacefulprogram.nivod_tv.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("video_his")
data class VideoHistoryEntity(
    @PrimaryKey
    val showIdCode: String,

    val showTitle: String,

    val showImage: String,

    val playIdCode: String? = null
)
