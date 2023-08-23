package io.github.peacefulprogram.nivod_api.dto

import io.github.peacefulprogram.nivod_api.BasicVideoInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class VideoDetailRecommendResponse(
    @SerialName("list")
    val list: List<VideoDetailRecommend>,
    @SerialName("msg")
    val msg: String,
    @SerialName("status")
    val status: Int
)

@Serializable
data class VideoDetailRecommend(
    @SerialName("actors")
    val actors: String = "",
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
    val director: String = "",
    @SerialName("episodesTxt")
    val episodesTxt: String = "",
    @SerialName("favoriteCount")
    val favoriteCount: Int,
    @SerialName("hot")
    val hot: Int,
    @SerialName("inSeries")
    val inSeries: Int,
    @SerialName("isEpisodes")
    val isEpisodes: Int,
    @SerialName("isEpisodesEnd")
    val isEpisodesEnd: Int,
    @SerialName("playLangs")
    val playLangs: List<VideoPlayLang>,
    @SerialName("playResolutions")
    val playResolutions: List<String>,
    @SerialName("playSources")
    val playSources: List<VideoPlaySource>,
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
    val showTypeName: String,
    @SerialName("status")
    val status: Int,
    @SerialName("voteDown")
    val voteDown: Int,
    @SerialName("voteUp")
    val voteUp: Int
) : BasicVideoInfo {
    override val title: String
        get() = showTitle
    override val subTitle: String
        get() = episodesTxt
    override val imageUrl: String
        get() = showImg
}
