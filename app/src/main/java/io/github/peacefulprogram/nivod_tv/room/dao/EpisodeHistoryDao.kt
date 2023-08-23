package io.github.peacefulprogram.nivod_tv.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.peacefulprogram.nivod_tv.room.entity.EpisodeHistoryEntity

@Dao
interface EpisodeHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(history: EpisodeHistoryEntity)

    @Query("select * from episode_his where playIdCode = :playIdCode")
    suspend fun queryHistoryByEpisodeId(playIdCode: String): EpisodeHistoryEntity?

    @Query(
        """
        select e.*
        from episode_his e, video_his v 
        where e.playIdCode = v.playIdCode
         and v.showIdCode = :showIdCode
    """
    )
    suspend fun queryLatestProgress(showIdCode: String): EpisodeHistoryEntity?
}