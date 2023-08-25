package io.github.peacefulprogram.nivod_api

import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager

val DefaultTrustManager = object : X509TrustManager {
    override fun checkClientTrusted(
        p0: Array<out X509Certificate>?,
        p1: String?
    ) = Unit

    override fun checkServerTrusted(
        p0: Array<out X509Certificate>?,
        p1: String?
    ) = Unit

    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()

}

val DefaultSSLSocketFactory = SSLContext.getInstance("TLS").apply {
    init(null, arrayOf(DefaultTrustManager), SecureRandom())
}.socketFactory

