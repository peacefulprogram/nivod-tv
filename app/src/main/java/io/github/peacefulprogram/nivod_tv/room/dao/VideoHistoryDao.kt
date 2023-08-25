package io.github.peacefulprogram.nivod_tv.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.github.peacefulprogram.nivod_api.dto.VideoDetailEntity
import io.github.peacefulprogram.nivod_tv.room.dto.VideoPlayHistory
import io.github.peacefulprogram.nivod_tv.room.entity.VideoHistoryEntity

@Dao
interface VideoHistoryDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoHistoryEntity)

    @Query("select * from video_his where showIdCode = :showIdCode")
    suspend fun queryVideoHistory(showIdCode: String): VideoHistoryEntity?

    @Query("update video_his set playIdCode = :playIdCode where showIdCode = :showIdCode")
    suspend fun updateLatestPlayedEpisode(showIdCode: String, playIdCode: String)

    @Query(
        """
        select v.showIdCode,
            v.showTitle,
            v.showImage,
            e.playIdCode,
            e.episodeName,
            e.progress,
            e.duration
        from video_his v, episode_his e 
        where v.playIdCode = e.playIdCode
        order by e.updateTime desc
    """
    )
    fun queryAllPlayHistory(): PagingSource<Int, VideoPlayHistory>

    @Transaction
    suspend fun saveVideo(videoDetail: VideoDetailEntity) {
        val playIdCode = queryVideoHistory(videoDetail.showIdCode)?.playIdCode
        insertVideo(
            VideoHistoryEntity(
                showIdCode = videoDetail.showIdCode,
                showTitle = videoDetail.showTitle,
                showImage = videoDetail.showImg,
                playIdCode = playIdCode
            )
        )
    }


    @Query("delete from video_his where playIdCode = :playIdCode")
    suspend fun deleteVideo(playIdCode: String)

    @Query("delete from episode_his where showIdCode = :showIdCode")
    suspend fun deleteEpisodeHistoryOfVideo(showIdCode: String)

    @Query("delete from video_his")
    suspend fun deleteAllVideoHistory()


    @Query("delete from episode_his")
    suspend fun deleteAllEpisodeHistory()

    @Transaction
    suspend fun deleteHistoryById(id: String) {
        deleteVideo(id)
        deleteEpisodeHistoryOfVideo(id)
    }

    @Transaction
    suspend fun deleteAllHistory() {
        deleteAllVideoHistory()
        deleteAllEpisodeHistory()
    }
}