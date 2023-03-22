package com.pubnub.api.endpoints.publishKT

import com.pubnub.api.endpoints.publishKT.PNConfiguration.Companion.isValid
import okhttp3.Request
import okio.Buffer
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal object PubNubUtil {

    private val log = LoggerFactory.getLogger("PubNubUtil")

    private const val CHARSET = "UTF-8"
    const val SIGNATURE_QUERY_PARAM_NAME = "signature"
    const val TIMESTAMP_QUERY_PARAM_NAME = "timestamp"
    const val AUTH_QUERY_PARAM_NAME = "auth"

    fun replaceLast(string: String, toReplace: String, replacement: String): String {
        val pos = string.lastIndexOf(toReplace)
        return if (pos > -1) {
            string.substring(0, pos) + replacement + string.substring(
                pos + toReplace.length,
                string.length
            )
        } else {
            string
        }
    }

    /**
     * Returns decoded String
     *
     * @param stringToEncode , input string
     * @return , decoded string
     */
    fun urlDecode(stringToEncode: String?): String? {
        return try {
            URLDecoder.decode(stringToEncode, CHARSET)
        } catch (e: UnsupportedEncodingException) {
            null
        }
    }

    fun signRequest(
        originalRequest: Request,
        pnConfiguration: PNConfiguration,
        timestamp: Int
    ): Request {
        // only sign if we have a secret key in place.
        if (!pnConfiguration.secretKey.isValid()) {
            return originalRequest
        }
        val signature = generateSignature(pnConfiguration, originalRequest, timestamp)
        val rebuiltUrl = originalRequest.url.newBuilder()
            .addQueryParameter("timestamp", timestamp.toString())
            .addQueryParameter("signature", signature)
            .build()
        return originalRequest.newBuilder().url(rebuiltUrl).build()
    }

    fun shouldSignRequest(configuration: PNConfiguration): Boolean {
        return configuration.secretKey.isValid()
    }

    fun generateSignature(
        configuration: PNConfiguration,
        requestURL: String,
        queryParams: MutableMap<String, String>,
        method: String,
        requestBody: String?,
        timestamp: Int
    ): String {

        val signatureBuilder = StringBuilder()
        queryParams["timestamp"] = timestamp.toString()

        val classic = true
        val encodedQueryString = if (classic) {
            preparePamArguments(queryParams)
        } else {
            preparePamArguments("$requestURL&timestamp=$timestamp")
        }

        val isV2Signature: Boolean = !(requestURL.startsWith("/publish") && method.equals("post", ignoreCase = true))
        if (!isV2Signature) {
            signatureBuilder.append(configuration.subscribeKey).append("\n")
            signatureBuilder.append(configuration.publishKey).append("\n")
            signatureBuilder.append(requestURL).append("\n")
            signatureBuilder.append(encodedQueryString)
        } else {
            signatureBuilder.append(method.uppercase(Locale.getDefault())).append("\n")
            signatureBuilder.append(configuration.publishKey).append("\n")
            signatureBuilder.append(requestURL).append("\n")
            signatureBuilder.append(encodedQueryString).append("\n")
            signatureBuilder.append(requestBody)
        }

        var signature = ""
        try {
            signature = signSHA256(configuration.secretKey, signatureBuilder.toString())
            if (isV2Signature) {
                signature = removeTrailingEqualSigns(signature)
                signature = "v2.$signature"
            }
        } catch (e: PubNubException) {
            log.warn("signature failed on SignatureInterceptor: $e")
        } catch (e: UnsupportedEncodingException) {
            log.warn("signature failed on SignatureInterceptor: $e")
        }
        return signature
    }

    internal fun signSHA256(key: String, data: String): String {
        val sha256HMAC: Mac
        val hmacData: ByteArray
        val secretKey = SecretKeySpec(key.toByteArray(charset(CHARSET)), "HmacSHA256")
        sha256HMAC = try {
            Mac.getInstance("HmacSHA256")
        } catch (e: NoSuchAlgorithmException) {
            throw Crypto.newCryptoError(0, e)
        }
        try {
            sha256HMAC.init(secretKey)
        } catch (e: InvalidKeyException) {
            throw Crypto.newCryptoError(0, e)
        }
        hmacData = sha256HMAC.doFinal(data.toByteArray(charset(CHARSET)))

        val signed = String(Base64.encode(hmacData, Base64.NO_WRAP), Charset.forName(CHARSET))
            .replace('+', '-')
            .replace('/', '_')
        return signed
    }

    private fun generateSignature(
        configuration: PNConfiguration,
        request: Request,
        timestamp: Int
    ): String? {
        val queryParams: MutableMap<String, String> = mutableMapOf()
        for (queryKey: String in request.url.queryParameterNames) {
            val value = request.url.queryParameter(queryKey)
            if (value != null) {
                queryParams[queryKey] = value
            }
        }
        return generateSignature(
            configuration,
            request.url.encodedPath,
            queryParams,
            request.method,
            requestBodyToString(request),
            timestamp
        )
    }

    fun removeTrailingEqualSigns(signature: String): String {
        var cleanSignature = signature
        while (cleanSignature[cleanSignature.length - 1] == '=') {
            cleanSignature = cleanSignature.substring(0, cleanSignature.length - 1)
        }
        return cleanSignature
    }

    internal fun requestBodyToString(request: Request): String? {
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

    internal fun preparePamArguments(pamArgs: Map<String, String>): String {
        val pamKeys: Set<String?> = TreeSet(pamArgs.keys)
        var stringifiedArguments = ""
        var i = 0
        for (pamKey in pamKeys) {
            if (i != 0) {
                stringifiedArguments = "$stringifiedArguments&"
            }
            stringifiedArguments = stringifiedArguments + pamKey + "=" + pamEncode(pamArgs[pamKey]!!)
            i += 1
        }
        return stringifiedArguments
    }

    private fun preparePamArguments(encodedQueryString: String): String {
        return encodedQueryString.split("&")
            .toSortedSet()
            .map { pamEncode(it, true) }
            .joinToString("&")
    }

    /**
     * Returns encoded String
     *
     * @param stringToEncode , input string
     * @return , encoded string
     */
    internal fun pamEncode(stringToEncode: String, alreadyPercentEncoded: Boolean = false): String {
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

    internal fun require(value: Boolean, error: PubNubError) {
        if (!value) {
            throw PubNubException(error)
        }
    }
}

internal fun <E> List<E>.toCsv(): String {
    if (this.isNotEmpty())
        return this.joinToString(",")
    return ","
}
