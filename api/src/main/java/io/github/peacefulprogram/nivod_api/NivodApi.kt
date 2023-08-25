package io.github.peacefulprogram.nivod_api

import cn.hutool.crypto.digest.MD5
import io.github.peacefulprogram.nivod_api.dto.ChannelRecommendResponse
import io.github.peacefulprogram.nivod_api.dto.ChannelResponse
import io.github.peacefulprogram.nivod_api.dto.HotKeywordResponse
import io.github.peacefulprogram.nivod_api.dto.SearchVideoResponse
import io.github.peacefulprogram.nivod_api.dto.VideoDetailRecommendResponse
import io.github.peacefulprogram.nivod_api.dto.VideoDetailResponse
import io.github.peacefulprogram.nivod_api.dto.VideoStreamUrlResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class NivodApi(logRequest: Boolean = false, private val disableSSLCheck: Boolean = true) {

    companion object {

        const val API_SERVER = "https://api.nivodz.com"

        const val REFERER = "https://www.nivod4.tv/"

        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"

    }

    private val httpClient by lazy { createKtorClient(logRequest) }

    private fun createKtorClient(logRequest: Boolean): HttpClient {
        return HttpClient(OkHttp) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json {
                    isLenient = true
                    allowSpecialFloatingPointValues = true
                    allowStructuredMapKeys = true
                    ignoreUnknownKeys = true
                })
            }
            defaultRequest {
                url(API_SERVER)
                header(HttpHeaders.UserAgent, USER_AGENT)
                header(HttpHeaders.Referrer, REFERER)
            }
            engine {
                if (logRequest) {
                    addNetworkInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.HEADERS
                    })
                }
                config {
                    if (disableSSLCheck) {
                        sslSocketFactory(DefaultSSLSocketFactory, DefaultTrustManager)
                        hostnameVerifier { _, _ -> true }
                    }
                }
                addInterceptor { chain ->
                    val resp = chain.proceed(chain.request())
                    val plainBytes = resp.body
                        ?.string()
                        ?.let { decrypt(it) }
                    if (plainBytes == null) {
                        resp
                    } else {
                        resp.newBuilder()
                            .body(plainBytes.toResponseBody(resp.body!!.contentType()))
                            .build()
                    }
                }
            }
        }
    }

    private fun decrypt(encryptedText: String): ByteArray {
        val cipherText = encryptedText.decodeHexString()
        val key = "diao.com".toByteArray()
        return Cipher.getInstance("DES/ECB/PKCS5Padding").run {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "DES"))
            doFinal(cipherText)
        }
    }

    private fun String.decodeHexString(): ByteArray =
        this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()


    private fun MutableMap<String, String>.appendIfNotEmpty(key: String, value: Any?) {
        if (value == null || (value is String && value.isEmpty())) {
            return
        }
        put(key, value.toString())
    }

    fun NoEmptyValueMap(vararg pairs: Pair<String, Any?>): Map<String, String> {
        val result =
            pairs.asSequence()
                .filter { (k, v) -> k.isNotEmpty() && v != null && (v !is String || v.isNotEmpty()) }
                .map { (k, v) -> k to v.toString() }
                .toList()
                .toTypedArray()
        return mapOf(*result)
    }

    private fun createSign(queryParams: Map<String, String>, body: Map<String, String>): String {
        val prefixes = arrayOf("__QUERY::", "__BODY::")
        val params = arrayOf(queryParams, body)
        val str = StringBuilder()
        for (i in prefixes.indices) {
            val param = params[i]
            str.append(prefixes[i])
            param.keys.toList().sorted().filter {
                it.isNotEmpty() && param[it]?.isNotEmpty() == true
            }.forEach {
                str.append(it)
                str.append('=')
                str.append(param[it])
                str.append('&')
            }
        }
        str.append("__KEY::2x_Give_it_a_shot")
        return MD5.create().digestHex(str.toString())
    }


    private fun HttpRequestBuilder.withSign(
        body: Map<String, String> = emptyMap(),
        queryParams: Map<String, String> = emptyMap()
    ) {
        val allQueryParams = mutableMapOf(
            "_ts" to System.currentTimeMillis().toString(),
            "app_version" to "1.0",
            "platform" to "3",
            "market_id" to "web_nivod",
            "device_code" to "web",
            "versioncode" to "1",
            "oid" to "8ca275aa5e12ba504b266d4c70d95d77a0c2eac5726198ea"
        ).apply {
            putAll(queryParams)
        }
        url {
            allQueryParams.forEach { (k, v) ->
                parameters.append(k, v)
            }
            parameters.append("sign", createSign(queryParams = allQueryParams, body = body))
        }
        if (body.isNotEmpty()) {
            val formParams = parameters {
                body.forEach { (k, v) -> append(k, v) }
            }
            setBody(FormDataContent(formParams))
        }
    }

    suspend fun queryChannels(): ChannelResponse {
        return httpClient.post("/show/channel/list/WEB/3.2") {
            withSign()
        }
            .body()

    }

    suspend fun queryRecommendationOfChannel(
        start: Int,
        channelId: Int?
    ): ChannelRecommendResponse {
        return httpClient.post("/index/desktop/WEB/3.4") {
            withSign(
                body = NoEmptyValueMap(
                    "channel_id" to channelId,
                    "start" to start,
                    "more" to "1"
                )
            )
        }.body<ChannelRecommendResponse>()
            .run {
                copy(banners = banners.filter { it.show != null }) // 排除掉广告
            }
    }

    suspend fun queryVideoDetail(showIdCode: String): VideoDetailResponse {
        return httpClient.post("/show/detail/WEB/3.2") {
            withSign(body = mapOf("show_id_code" to showIdCode))
        }.body()
    }

    suspend fun queryVideoDetailRecommend(
        channelId: Int,
        showTypeId: Int
    ): VideoDetailRecommendResponse {
        return httpClient.post("/show/detail/recommend/WEB/3.2") {
            withSign(
                body = mapOf(
                    "channel_id" to channelId.toString(),
                    "show_type_id" to showTypeId.toString()
                )
            )
        }.body()
    }

    suspend fun queryVideoStreamUrl(
        showIdCode: String,
        playIdCode: String
    ): VideoStreamUrlResponse {
        return httpClient.post("/show/play/info/WEB/3.2") {
            withSign(
                body = mapOf(
                    "show_id_code" to showIdCode,
                    "play_id_code" to playIdCode
                )
            )
        }.body()
    }

    suspend fun searchVideo(
        keyword: String,
        start: Int,
        keywordType: SearchKeywordType = SearchKeywordType.ALL
    ): SearchVideoResponse {
        return httpClient.post("/show/search/WEB/3.2") {
            withSign(
                body = mapOf(
                    "keyword" to keyword,
                    "start" to start.toString(),
                    "cat_id" to "1",
                    "keyword_type" to keywordType.code
                )
            )
        }.body()
    }

    suspend fun queryHotKeyword(): HotKeywordResponse {
        return httpClient.post("https://api.nivodz.com/show/search/hotwords/WEB/3.2") {
            withSign()
        }.body()
    }

}
