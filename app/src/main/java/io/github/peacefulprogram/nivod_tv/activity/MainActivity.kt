package io.github.peacefulprogram.nivod_tv.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import io.github.peacefulprogram.nivod_tv.R
import io.github.peacefulprogram.nivod_tv.ext.showShortToast
import io.github.peacefulprogram.nivod_tv.screen.MainScreen
import io.github.peacefulprogram.nivod_tv.theme.NivodTheme
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {

    private var lastClickBackTime: Long = 0L

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NivodTheme {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(
                            dimensionResource(id = R.dimen.screen_h_padding), dimensionResource(
                                id = R.dimen.screen_v_padding
                            )
                        )
                        .fillMaxWidth()
                ) {
                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
                        MainScreen(viewModel = get())
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val now = System.currentTimeMillis()
        if (now - lastClickBackTime < 2000) {
            super.onBackPressed()
        } else {
            lastClickBackTime = now
            this.showShortToast("再次点击退出应用")
        }
    }


}