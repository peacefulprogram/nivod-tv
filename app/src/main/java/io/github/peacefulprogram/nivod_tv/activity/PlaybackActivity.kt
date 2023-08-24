package io.github.peacefulprogram.nivod_tv.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.github.peacefulprogram.nivod_tv.R
import io.github.peacefulprogram.nivod_tv.playback.VideoEpisode
import io.github.peacefulprogram.nivod_tv.playback.VideoPlaybackFragment
import io.github.peacefulprogram.nivod_tv.viewmodel.PlaybackViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlaybackActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel by viewModel<PlaybackViewModel> {
            parametersOf(
                intent.getStringExtra("id"),
                intent.getStringExtra("title"),
                intent.getIntExtra("idx", 0),
                (intent.getSerializableExtra("eps") as Array<VideoEpisode>).toList()
            )
        }
        setContentView(R.layout.activity_playback)
        supportFragmentManager.beginTransaction()
            .replace(R.id.playback_fragment, VideoPlaybackFragment(viewModel)).commit()

    }

    companion object {
        fun startActivity(
            context: Context,
            showIdCode: String,
            showTitle: String,
            playEpIndex: Int,
            episodes: List<VideoEpisode>
        ) {
            Intent(context, PlaybackActivity::class.java).apply {
                putExtra("id", showIdCode)
                putExtra("idx", playEpIndex)
                putExtra("title", showTitle)
                putExtra("eps", episodes.toTypedArray())
                context.startActivity(this)
            }
        }
    }

}