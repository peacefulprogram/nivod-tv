package io.github.peacefulprogram.nivod_api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoDetailResponse(
    @SerialName("entity")
    val entity: VideoDetailEntity,
    @SerialName("msg")
    val msg: String,
    @SerialName("status")
    val status: Int
)

@Serializable
data class VideoDetailEntity(
    @SerialName("actors")
    val actors: String,
    @SerialName("addDate")
    val addDate: Long,
    @SerialName("catId")
    val catId: Int,
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("channelName")
    val channelName: String,
    @SerialName("commentCount")
    val commentCount: Int,
    @SerialName("director")
    val director: String,
    @SerialName("episodesTxt")
    val episodesTxt: String,
    @SerialName("episodesUpdateDesc")
    val episodesUpdateDesc: String,
    @SerialName("episodesUpdateRemark")
    val episodesUpdateRemark: String,
    @SerialName("favoriteCount")
    val favoriteCount: Int,
    @SerialName("forceApp")
    val forceApp: String,
    @SerialName("hot")
    val hot: Int,
    @SerialName("inSeries")
    val inSeries: Int,
    @SerialName("isEpisodes")
    val isEpisodes: Int,
    @SerialName("isEpisodesEnd")
    val isEpisodesEnd: Int,
    @SerialName("pageBgImg")
    val pageBgImg: String,
    @SerialName("playLangs")
    val playLangs: List<VideoPlayLang>,
    @SerialName("playResolutions")
    val playResolutions: List<String>,
    @SerialName("playSources")
    val playSources: List<VideoPlaySource>,
    @SerialName("plays")
    val plays: List<VideoDetailPlay>,
    @SerialName("postYear")
    val postYear: Int,
    @SerialName("rating")
    val rating: Int,
    @SerialName("regionId")
    val regionId: Int,
    @SerialName("regionName")
    val regionName: String,
    @SerialName("shareCount")
    val shareCount: Int,
    @SerialName("shareForced")
    val shareForced: Int,
    @SerialName("shareTxt")
    val shareTxt: String,
    @SerialName("shareUrl")
    val shareUrl: String,
    @SerialName("showDesc")
    val showDesc: String,
    @SerialName("showId")
    val showId: Int,
    @SerialName("showIdCode")
    val showIdCode: String,
    @SerialName("showImg")
    val showImg: String,
    @SerialName("showTcTitle")
    val showTcTitle: String,
    @SerialName("showTitle")
    val showTitle: String,
    @SerialName("showTypeId")
    val showTypeId: Int,
    @SerialName("showTypeName")
    val showTypeName: String = "",
    @SerialName("status")
    val status: Int,
    @SerialName("titleImg")
    val titleImg: String,
    @SerialName("voteDown")
    val voteDown: Int,
    @SerialName("voteUp")
    val voteUp: Int
)

@Serializable
data class VideoDetailPlay(
    @SerialName("displayName")
    val displayName: String,
    @SerialName("episodeId")
    val episodeId: Int,
    @SerialName("episodeName")
    val episodeName: String,
    @SerialName("external")
    val `external`: Int,
    @SerialName("langId")
    val langId: Int,
    @SerialName("playIdCode")
    val playIdCode: String,
    @SerialName("resolution")
    val resolution: String,
    @SerialName("resolutionInt")
    val resolutionInt: Int,
    @SerialName("seq")
    val seq: Int,
    @SerialName("size")
    val size: Long,
    @SerialName("sourceId")
    val sourceId: Int
)