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
import io.github.peacefulprogram.nivod_tv.screen.CategoriesScreen
import io.github.peacefulprogram.nivod_tv.theme.NivodTheme
import io.github.peacefulprogram.nivod_tv.viewmodel.CategoriesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CategoriesActivity : ComponentActivity() {


    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultParam = if (intent.hasExtra("cid")) intent.getIntExtra("cid", 0) else null
        val viewModel by viewModel<CategoriesViewModel> { parametersOf(defaultParam) }
        setContent {
            NivodTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    androidx.tv.material3.Surface(modifier = Modifier.fillMaxSize()) {
                        CategoriesScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    companion object {
        fun startActivity(context: Context, channelId: Int? = null) {
            Intent(context, CategoriesActivity::class.java).apply {
                if (channelId != null) {
                    putExtra("cid", channelId)
                }
                context.startActivity(this)
            }
        }
    }
}