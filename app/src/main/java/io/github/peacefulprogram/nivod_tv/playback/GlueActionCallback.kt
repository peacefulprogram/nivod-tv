package io.github.peacefulprogram.nivod_tv.playback

import androidx.leanback.widget.Action

interface GlueActionCallback {
    fun support(action: Action): Boolean

    fun onAction(action: Action)
}