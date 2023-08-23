package io.github.peacefulprogram.nivod_tv.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity("episode_his", indices = [Index("showIdCode")])
data class EpisodeHistoryEntity(
    @PrimaryKey
    val playIdCode: String,
    val showIdCode: String,
    val episodeName: String,
    val progress: Long,
    val duration: Long,
    val updateTime: Long
)
