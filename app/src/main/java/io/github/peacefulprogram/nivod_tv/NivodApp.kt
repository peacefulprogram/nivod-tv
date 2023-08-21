package io.github.peacefulprogram.nivod_tv

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class NivodApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        context = this
        startKoin {
            androidContext(this@NivodApp)
            androidLogger()
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
            private set
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .okHttpClient(OkHttpClient.Builder().addInterceptor(getRefererInterceptor()).build())
        .build()

    private fun getRefererInterceptor() = Interceptor { chain ->
        val newReq = chain.request().newBuilder()
            .header("referer", "")
            .build()
        chain.proceed(newReq)
    }
}