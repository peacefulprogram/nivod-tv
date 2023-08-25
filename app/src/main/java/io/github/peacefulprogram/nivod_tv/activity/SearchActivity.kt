package io.github.peacefulprogram.nivod_tv.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.tv.material3.ExperimentalTvMaterial3Api
import io.github.peacefulprogram.nivod_tv.screen.SearchScreen
import io.github.peacefulprogram.nivod_tv.theme.NivodTheme
import io.github.peacefulprogram.nivod_tv.viewmodel.SearchViewModel
import org.koin.android.ext.android.get

class SearchActivity : ComponentActivity() {

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = get<SearchViewModel>()
        setContent {
            NivodTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    androidx.tv.material3.Surface(modifier = Modifier.fillMaxSize()) {
                        SearchScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    companion object {
        fun startActivity(context: Context) {
            Intent(context, SearchActivity::class.java).apply {
                context.startActivity(this)
            }
        }
    }
}