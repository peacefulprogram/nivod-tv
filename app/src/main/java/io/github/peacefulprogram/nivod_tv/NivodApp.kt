package io.github.peacefulprogram.nivod_tv

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import coil.ImageLoader
import coil.ImageLoaderFactory
import io.github.peacefulprogram.nivod_api.DefaultSSLSocketFactory
import io.github.peacefulprogram.nivod_api.DefaultTrustManager
import io.github.peacefulprogram.nivod_api.NivodApi
import io.github.peacefulprogram.nivod_tv.room.NivodDatabase
import io.github.peacefulprogram.nivod_tv.viewmodel.MainViewModel
import io.github.peacefulprogram.nivod_tv.viewmodel.PlayHistoryViewModel
import io.github.peacefulprogram.nivod_tv.viewmodel.PlaybackViewModel
import io.github.peacefulprogram.nivod_tv.viewmodel.SearchResultViewModel
import io.github.peacefulprogram.nivod_tv.viewmodel.SearchViewModel
import io.github.peacefulprogram.nivod_tv.viewmodel.VideoDetailViewModel
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.context.startKoin
import org.koin.dsl.module
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class NivodApp : Application(), ImageLoaderFactory {

    private val TAG = NivodApp::class.java.simpleName

    override fun onCreate() {
        super.onCreate()
        context = this
        HttpsURLConnection.setDefaultSSLSocketFactory(DefaultSSLSocketFactory)
        startKoin {
            androidContext(this@NivodApp)
            androidLogger()
            modules(httpModule(), viewModelModule(), roomModule())
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient(
            OkHttpClient.Builder()
                .sslSocketFactory(DefaultSSLSocketFactory, DefaultTrustManager)
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(getRefererInterceptor()).build()
        )
        .build()

    private fun getRefererInterceptor() = Interceptor { chain ->
        val newReq = chain.request().newBuilder()
            .header("referer", NivodApi.REFERER)
            .header("user-agent", NivodApi.USER_AGENT)
            .build()
        chain.proceed(newReq)
    }

    private fun httpModule() = module {
        single { NivodApi(BuildConfig.DEBUG) }
    }

    private fun viewModelModule() = module {
        viewModelOf(::MainViewModel)
        viewModel { parameters ->
            VideoDetailViewModel(parameters.get(), get(), get(), get())
        }
        viewModel { parameters ->
            PlaybackViewModel(
                parameters.get(),
                parameters.get(),
                parameters.get(),
                parameters.get(),
                get(),
                get(),
                get()
            )
        }
        viewModelOf(::SearchViewModel)
        viewModelOf(::PlayHistoryViewModel)
        viewModel { parameters -> SearchResultViewModel(parameters.get(), get()) }
    }

    private fun roomModule() = module {
        single {
            Room.databaseBuilder(this@NivodApp, NivodDatabase::class.java, "nivod").apply {
                if (BuildConfig.DEBUG) {
                    val queryCallback = object : RoomDatabase.QueryCallback {
                        override fun onQuery(sqlQuery: String, bindArgs: List<Any?>) {
                            Log.i(TAG, "room sql: $sqlQuery  args: $bindArgs")
                        }
                    }
                    setQueryCallback(queryCallback, Executors.newSingleThreadExecutor())
                }
            }
                .build()
        }

        single {
            get<NivodDatabase>().videoHistoryDao()
        }

        single {
            get<NivodDatabase>().episodeHistoryDao()
        }
        single {
            get<NivodDatabase>().searchHistoryDao()
        }
    }
}