package io.github.peacefulprogram.nivod_tv.playback

import java.io.Serializable

data class VideoEpisode(
    val playIdCode: String,
    val episodeName: String
) : Serializable