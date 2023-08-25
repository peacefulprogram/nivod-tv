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
import io.github.peacefulprogram.nivod_tv.screen.SearchResultScreen
import io.github.peacefulprogram.nivod_tv.theme.NivodTheme
import io.github.peacefulprogram.nivod_tv.viewmodel.SearchResultViewModel
import org.koin.android.ext.android.get
import org.koin.core.parameter.parametersOf

class SearchResultActivity : ComponentActivity() {

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val keyword = intent.getStringExtra("kw")!!
        val viewModel = get<SearchResultViewModel> { parametersOf(keyword) }
        setContent {
            NivodTheme {
                Surface {
                    androidx.tv.material3.Surface(
                        modifier = Modifier.padding(48.dp, 27.dp)
                    ) {
                        SearchResultScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    companion object {
        fun startActivityByKeyword(context: Context, keyword: String) {
            Intent(context, SearchResultActivity::class.java).apply {
                putExtra("kw", keyword)
                context.startActivity(this)
            }
        }
    }
}