package com.pubnub.apikt

import com.github.tomakehurst.wiremock.verification.LoggedRequest
import com.pubnub.apikt.vendor.Base64
import com.pubnub.apikt.vendor.Crypto
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Request
import okio.Buffer
import org.apache.commons.codec.digest.HmacAlgorithms
import org.apache.commons.codec.digest.HmacUtils
import org.junit.Assert.assertEquals
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SignatureUtils {

    fun decomposeAndVerifySignature(configuration: PNConfiguration, request: LoggedRequest) {
        decomposeAndVerifySignature(
            configuration = configuration,
            url = request.absoluteUrl,
            method = request.method.name,
            body = request.bodyAsString
        )
    }

    fun decomposeAndVerifySignature(configuration: PNConfiguration, request: Request) {
        decomposeAndVerifySignature(
            configuration = configuration,
            url = request.url.toString(),
            method = request.method,
            body = requestBodyToString(request)
        )
    }

    private fun decomposeAndVerifySignature(
        configuration: PNConfiguration,
        url: String,
        method: String,
        body: String = ""
    ) {
        val httpUrl = url.toHttpUrlOrNull()
        println(httpUrl)

        val sortedQueryString = httpUrl!!.run {
            queryParameterNames
                .filter { it != "signature" }
                .mapNotNull { queryParameterName ->
                    queryParameterValues(queryParameterName).first()?.let { queryParameterName to pamEncode(it) }
                }
                .toMap()
                .toSortedMap()
                .map { "${it.key}=${it.value}" }
                .joinToString("&")
        }

        var v2Signature = true

        val input =
            if (!(httpUrl.encodedPath.startsWith("/publish") && method.lowercase(Locale.getDefault()) == "post")) {
                """
                ${method.uppercase(Locale.getDefault())}
                ${configuration.publishKey}
                ${httpUrl.encodedPath}
                $sortedQueryString
                $body
                """.trimIndent()
            } else {
                v2Signature = false
                """
                ${configuration.subscribeKey}
                ${configuration.publishKey}
                ${httpUrl.encodedPath}
                $sortedQueryString
                """.trimIndent()
            }

        val actualSignature = httpUrl.queryParameter("signature")
        val verifiedSignature = verifyViaKotlin(configuration.secretKey, input, v2Signature)

        val rebuiltSignature = signSHA256(configuration.secretKey, input).run {
            if (v2Signature) "v2.${this.trim('=')}"
            else this
        }

        println("originalTimestamp:\t${httpUrl.queryParameter("timestamp")}")
        println("signatureInput:\t$input")
        println("actualSignature:\t$actualSignature")
        println("rebuiltSignature:\t$rebuiltSignature")
        println("verifiedSignature:\t$verifiedSignature")

        assertEquals(actualSignature, rebuiltSignature)
        assertEquals(actualSignature, verifiedSignature)
    }

    private fun signSHA256(key: String, data: String): String {
        val hmacData: ByteArray
        val secretKey = SecretKeySpec(key.toByteArray(charset("UTF-8")), "HmacSHA256")
        val sha256HMAC: Mac = try {
            Mac.getInstance("HmacSHA256")
        } catch (e: NoSuchAlgorithmException) {
            throw Crypto.newCryptoError(0, e)
        }
        try {
            sha256HMAC.init(secretKey)
        } catch (e: InvalidKeyException) {
            throw Crypto.newCryptoError(0, e)
        }
        hmacData = sha256HMAC.doFinal(data.toByteArray(charset("UTF-8")))
        return String(Base64.encode(hmacData, 0), Charset.forName("UTF-8"))
            .replace('+', '-')
            .replace('/', '_')
            .replace("\n", "")
    }

    private fun verifyViaKotlin(key: String, input: String, v2Signature: Boolean): String {
        val hmac = HmacUtils(HmacAlgorithms.HMAC_SHA_256, key.toByteArray())
        hmac.hmacHex(input.toByteArray()).lowercase(Locale.getDefault())
        val hmacResult = hmac.hmac(input)
        val encoder = java.util.Base64.getUrlEncoder()
        return if (v2Signature) {
            "v2.${String(encoder.withoutPadding().encode(hmacResult))}"
        } else {
            String(encoder.encode(hmacResult))
        }
    }

    private fun requestBodyToString(request: Request): String {
        if (request.body == null) {
            return ""
        }
        try {
            val buffer = Buffer()
            request.body!!.writeTo(buffer)
            return buffer.readUtf8()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    private fun pamEncode(stringToEncode: String, alreadyPercentEncoded: Boolean = false): String {
        /* !'()*~ */

        return if (alreadyPercentEncoded) {
            stringToEncode
        } else {
            URLEncoder.encode(stringToEncode, "UTF-8")
                .replace("+", "%20")
        }.run {
            replace("*", "%2A")
        }
    }
}
