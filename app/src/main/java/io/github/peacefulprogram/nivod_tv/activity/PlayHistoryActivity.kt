package io.github.peacefulprogram.nivod_tv.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.peacefulprogram.nivod_tv.screen.PlayHistoryScreen
import io.github.peacefulprogram.nivod_tv.theme.NivodTheme
import io.github.peacefulprogram.nivod_tv.viewmodel.PlayHistoryViewModel
import org.koin.android.ext.android.get

class PlayHistoryActivity : ComponentActivity() {

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = get<PlayHistoryViewModel>()
        setContent {
            NivodTheme {
                Surface {
                    androidx.tv.material3.Surface(
                        modifier = Modifier.padding(48.dp, 27.dp)
                    ) {
                        PlayHistoryScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    companion object {
        fun startActivity(context: Context) {
            Intent(context, PlayHistoryActivity::class.java).apply {
                context.startActivity(this)
            }
        }
    }
}