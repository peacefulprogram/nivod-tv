package io.github.peacefulprogram.nivod_api

import cn.hutool.crypto.digest.MD5
import io.github.peacefulprogram.nivod_api.dto.ChannelRecommendResponse
import io.github.peacefulprogram.nivod_api.dto.ChannelResponse
import io.github.peacefulprogram.nivod_api.dto.FilterConditionResponse
import io.github.peacefulprogram.nivod_api.dto.HotKeywordResponse
import io.github.peacefulprogram.nivod_api.dto.SearchVideoResponse
import io.github.peacefulprogram.nivod_api.dto.VideoCategoriesSearchResponse
import io.github.peacefulprogram.nivod_api.dto.VideoDetailRecommendResponse
import io.github.peacefulprogram.nivod_api.dto.VideoDetailResponse
import io.github.peacefulprogram.nivod_api.dto.VideoStreamUrlResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.ProxyConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.MalformedInputException
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import java.net.Proxy
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class NivodApi(
    val logRequest: Boolean = false,
    private val disableSSLCheck: Boolean = true,
    defaultProxyConfig: Proxy?
) {

    companion object {

        const val API_SERVER = "https://api.nivodz.com"

        const val REFERER = "https://www.nivod4.tv/"

        const val USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36"

    }

    private var httpClient = createKtorClient(logRequest, defaultProxyConfig)


    fun recreateKtorClientWithProxy(proxyConfig: ProxyConfig?) {
        httpClient = createKtorClient(logRequest = logRequest, proxyConfig = proxyConfig)
    }

    private fun createKtorClient(logRequest: Boolean, proxyConfig: ProxyConfig?): HttpClient {
        return HttpClient(OkHttp) {
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.status.value < 300) {
                        return@validateResponse
                    }
                    val exceptionResponseText = try {
                        response.bodyAsText()
                    } catch (_: MalformedInputException) {
                        "<body failed decoding>"
                    }
                    if (response.status.value == 403) {
                        throw RuntimeException(
                            "请求失败,请确认科学上网后试:" + exceptionResponseText
                                .run {
                                    substring(0..(length.coerceAtMost(200)))
                                }
                        )
                    } else if (response.status.value >= 300) {
                        val exception = when (response.status.value) {
                            in 300..399 -> RedirectResponseException(
                                response,
                                exceptionResponseText
                            )

                            in 400..499 -> ClientRequestException(
                                response,
                                exceptionResponseText
                            )

                            in 500..599 -> ServerResponseException(
                                response,
                                exceptionResponseText
                            )

                            else -> ResponseException(response, exceptionResponseText)
                        }
                        throw exception
                    }
                }
            }
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
                proxy = proxyConfig
                if (logRequest) {
                    addNetworkInterceptor(HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.HEADERS
                    })
                }
                config {
                    followRedirects(true)
                    if (disableSSLCheck) {
                        sslSocketFactory(DefaultSSLSocketFactory, DefaultTrustManager)
                        hostnameVerifier { _, _ -> true }
                    }
                }
                addInterceptor { chain ->
                    val resp = chain.proceed(chain.request())
                    if (resp.code != 200) {
                        return@addInterceptor resp
                    }
                    var code = resp.code
                    val plainBytes = resp.body
                        ?.string()
                        ?.let {
                            try {
                                decrypt(it)
                            } catch (ex: Exception) {
                                code = 500
                                "解密响应内容失败:${ex.message}".toByteArray(
                                    resp.body?.contentType()?.charset() ?: Charsets.UTF_8
                                )
                            }
                        }
                    if (plainBytes == null) {
                        resp
                    } else {
                        resp.newBuilder()
                            .code(code)
                            .body(plainBytes.toResponseBody(resp.body!!.contentType()))
                            .build()
                    }
                }
            }
        }
    }

    private fun decrypt(encryptedText: String): ByteArray {
        val cipherText = (encryptedText).decodeHexString()
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
        return httpClient.post("/show/search/hotwords/WEB/3.2") {
            withSign()
        }.body()
    }

    suspend fun queryFilterCondition(): FilterConditionResponse {
        return httpClient.post("/show/filter/condition/WEB/3.2") {
            withSign()
        }.body()
    }

    suspend fun queryVideoOfCategories(
        sortBy: Int,
        channelId: Int,
        showTypeId: Int,
        regionId: Int,
        langId: Int,
        yearRange: String,
        start: Int
    ): VideoCategoriesSearchResponse {
        return httpClient.post("/show/filter/WEB/3.2") {
            withSign(
                body = mapOf(
                    "sort_by" to sortBy.toString(),
                    "channel_id" to channelId.toString(),
                    "show_type_id" to showTypeId.toString(),
                    "region_id" to regionId.toString(),
                    "lang_id" to langId.toString(),
                    "year_range" to yearRange,
                    "start" to start.toString()
                )
            )
        }.body()
    }

}
