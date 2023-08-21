package io.github.peacefulprogram.nivod_tv.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import io.github.peacefulprogram.nivod_tv.http.api.NivodApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LaunchedEffect(Unit) {
                val resp = withContext(Dispatchers.IO) {
                    NivodApi.queryRecommendationOfChannel(start = 0, channelId = null)
                }
                println(resp.list)
            }
            Text(
                text = "hello",
                fontSize = 120.sp,
                color = Color.White,
                fontWeight = FontWeight.Thin
            )
        }
    }


}