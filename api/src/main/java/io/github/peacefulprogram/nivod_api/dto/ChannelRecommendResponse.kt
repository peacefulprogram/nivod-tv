package io.github.peacefulprogram.nivod_api.dto

import io.github.peacefulprogram.nivod_api.BasicVideoInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelRecommendResponse(
    @SerialName("banners")
    val banners: List<ChannelRecommendBanner>,
    @SerialName("list")
    val list: List<ChannelRecommend>,
    @SerialName("more")
    val more: Int,
    @SerialName("msg")
    val msg: String,
    @SerialName("start")
    val start: String,
    @SerialName("status")
    val status: Int
)

@Serializable
data class ChannelRecommendBanner(
    @SerialName("id")
    val id: Int,
    @SerialName("imageUrl")
    val imageUrl: String,
    @SerialName("seq")
    val seq: Int,
    @SerialName("show")
    val show: ChannelRecommendShow? = null,
    @SerialName("title")
    val title: String
)

@Serializable
data class ChannelRecommend(
    @SerialName("blockId")
    val blockId: Int,
    @SerialName("blockType")
    val blockType: Int,
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("layout")
    val layout: Int,
    @SerialName("rows")
    val rows: List<ChannelRecommendRow>,
    @SerialName("srcChannelId")
    val srcChannelId: Int,
    @SerialName("title")
    val title: String
)

@Serializable
data class ChannelRecommendShow(
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
    @SerialName("pageBgImg")
    val pageBgImg: String,
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
    val showTypeName: String = "",
    @SerialName("status")
    val status: Int,
    @SerialName("titleImg")
    val titleImg: String,
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


@Serializable
data class ChannelRecommendRow(
    @SerialName("blockId")
    val blockId: Int,
    @SerialName("cells")
    val cells: List<ChannelRecommendCell>,
    @SerialName("overflow")
    val overflow: String,
    @SerialName("rowId")
    val rowId: Int,
    @SerialName("type")
    val type: Int
)

@Serializable
data class ChannelRecommendCell(
    @SerialName("bottomRightText")
    val bottomRightText: String,
    @SerialName("cellId")
    val cellId: Int,
    @SerialName("img")
    val img: String,
    @SerialName("intro")
    val intro: String = "",
    @SerialName("rowId")
    val rowId: Int,
    @SerialName("show")
    val show: ChannelRecommendShow,
    @SerialName("title")
    val title: String
)
