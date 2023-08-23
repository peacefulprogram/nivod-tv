package io.github.peacefulprogram.nivod_tv.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.peacefulprogram.nivod_tv.room.entity.SearchHistoryEntity

@Dao
interface SearchHistoryDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHistory(history: SearchHistoryEntity)

    @Query("select * from search_his order by updateTime desc")
    fun queryPaging(): PagingSource<Int, SearchHistoryEntity>

    @Delete
    suspend fun deleteHistory(history: SearchHistoryEntity)

    @Query("delete from search_his")
    suspend fun deleteAllHistory()
}