package io.github.peacefulprogram.nivod_api.dto

import io.github.peacefulprogram.nivod_api.BasicFilterCondition
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class FilterConditionResponse(
    @SerialName("channels")
    val channels: List<ChannelInfo>,
    @SerialName("langs")
    val langs: List<FilterConditionLang>,
    @SerialName("msg")
    val msg: String,
    @SerialName("regions")
    val regions: List<FilterConditionRegion>,
    @SerialName("sorts")
    val sorts: List<FilterConditionSort>,
    @SerialName("status")
    val status: Int,
    @SerialName("yearRanges")
    val yearRanges: List<FilterConditionYearRange>,
    @SerialName("sortsMap")
    val sortsMap: Map<Int, List<FilterConditionSort>>,
    @SerialName("typesMap")
    val typesMap: Map<Int, List<FilterConditionShowType>>
)


@Serializable
data class FilterConditionLang(
    @SerialName("langId")
    val langId: Int,
    @SerialName("langName")
    val langName: String
) : BasicFilterCondition<Int> {
    override val conditionName: String
        get() = langName
    override val conditionValue: Int
        get() = langId
}

@Serializable
data class FilterConditionRegion(
    @SerialName("langId")
    val langId: Int,
    @SerialName("regionId")
    val regionId: Int,
    @SerialName("regionName")
    val regionName: String,
    @SerialName("seq")
    val seq: Int,
    @SerialName("status")
    val status: Int
) : BasicFilterCondition<Int> {
    override val conditionName: String
        get() = regionName
    override val conditionValue: Int
        get() = regionId
}

@Serializable
data class FilterConditionSort(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("title")
    val title: String
) : BasicFilterCondition<Int> {
    override val conditionName: String
        get() = title
    override val conditionValue: Int
        get() = id
}

@Serializable
data class FilterConditionYearRange(
    @SerialName("code")
    val code: String,
    @SerialName("name")
    val name: String
) : BasicFilterCondition<String> {
    override val conditionName: String
        get() = name
    override val conditionValue: String
        get() = code
}

@Serializable
data class FilterConditionShowType(
    @SerialName("channelId")
    val channelId: Int,
    @SerialName("showTypeId")
    val showTypeId: Int,
    @SerialName("showTypeName")
    val showTypeName: String
) : BasicFilterCondition<Int> {
    override val conditionName: String
        get() = showTypeName
    override val conditionValue: Int
        get() = showTypeId
}