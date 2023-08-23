package io.github.peacefulprogram.nivod_tv.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("search_his")
data class SearchHistoryEntity(
    @PrimaryKey
    val keyword: String,
    val updateTime: Long
)