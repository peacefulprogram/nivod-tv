package io.github.peacefulprogram.nivod_tv.room

import androidx.room.Database
import androidx.room.RoomDatabase
import io.github.peacefulprogram.nivod_tv.room.dao.EpisodeHistoryDao
import io.github.peacefulprogram.nivod_tv.room.dao.SearchHistoryDao
import io.github.peacefulprogram.nivod_tv.room.dao.VideoHistoryDao
import io.github.peacefulprogram.nivod_tv.room.entity.EpisodeHistoryEntity
import io.github.peacefulprogram.nivod_tv.room.entity.SearchHistoryEntity
import io.github.peacefulprogram.nivod_tv.room.entity.VideoHistoryEntity

@Database(
    entities = [
        SearchHistoryEntity::class,
        VideoHistoryEntity::class,
        EpisodeHistoryEntity::class
    ],
    version = 1
)
abstract class NivodDatabase : RoomDatabase() {

    abstract fun videoHistoryDao(): VideoHistoryDao
    abstract fun episodeHistoryDao(): EpisodeHistoryDao
    abstract fun searchHistoryDao(): SearchHistoryDao

}