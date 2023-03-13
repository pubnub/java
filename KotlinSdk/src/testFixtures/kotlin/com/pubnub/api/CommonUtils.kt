package com.pubnub.apikt

import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.pubnub.apikt.models.consumer.PNPublishResult
import com.pubnub.apikt.models.consumer.PNStatus
import okhttp3.logging.HttpLoggingInterceptor
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.json.JSONObject
import org.junit.Assert.*
import org.slf4j.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Collectors

object CommonUtils {

    var DEFAULT_LISTEN_DURATION = 5

    internal fun observe(success: AtomicBoolean, seconds: Int) {
        Awaitility.await()
            .atMost(seconds.toLong(), TimeUnit.SECONDS)
            .untilTrue(success)
    }

    fun assertPnException(expectedPubNubError: PubNubError, pnStatus: PNStatus) {
        assertTrue(pnStatus.error)
        assertEquals(expectedPubNubError, pnStatus.exception!!.pubnubError)
    }

    fun assertPnException(expectedPubNubError: PubNubError, exception: Exception) {
        exception as PubNubException
        assertEquals(expectedPubNubError, exception.pubnubError)
    }

    fun emptyJson() = WireMock.aResponse().withBody("{}")

    fun failTest(message: String? = null) {
        fail(message)
    }

    fun getSpecialCharsMap(): List<SpecialChar> {
        val resourceFileAsString = getResourceFileAsString("special_chars.json")
        return Gson().fromJson(
            resourceFileAsString,
            object : TypeToken<List<SpecialChar>>() {}.type
        )
    }

    fun getResourceFileAsString(fileName: String?): String? {
        val classLoader = ClassLoader.getSystemClassLoader()
        classLoader.getResourceAsStream(fileName).use { inputStream ->
            if (inputStream == null) return null
            InputStreamReader(inputStream).use { isr ->
                BufferedReader(isr).use { reader ->
                    return reader.lines().collect(Collectors.joining(System.lineSeparator()))
                }
            }
        }
    }

    class SpecialChar(
        val name: String,
        val regular: String,
        val encoded: String
    )

    fun retry(function: () -> Unit) {
        val success = AtomicBoolean()
        val block = {
            try {
                val executor = Executors.newSingleThreadExecutor()
                val submit = executor.submit(
                    Callable {
                        try {
                            function.invoke()
                            true
                        } catch (t: Throwable) {
                            false
                        }
                    }
                )
                success.set(submit.get())
            } catch (t: Throwable) {
                success.set(false)
            }
        }

        Awaitility.await()
            .atMost(DEFAULT_LISTEN_DURATION.toLong(), TimeUnit.SECONDS)
            .pollInterval(Durations.FIVE_HUNDRED_MILLISECONDS)
            .until {
                block.invoke()
                success.get()
            }
    }

    fun randomValue(length: Int = 10): String {
        return UUID.randomUUID().toString()
            .replace("-", "")
            .take(length)
            .uppercase(Locale.getDefault())
    }

    fun randomChannel(): String {
        return "ch_${System.currentTimeMillis()}_${randomValue()}"
    }

    fun randomNumeric(length: Int = 10): String {
        return generateSequence { (0..9).random() }.take(length).toList().shuffled()
            .joinToString(separator = "")
    }

    fun publishMixed(pubnub: PubNub, count: Int, channel: String): List<PNPublishResult> {
        val list = mutableListOf<PNPublishResult>()
        repeat(count) {
            val sync = pubnub.publish(
                channel = channel,
                message = "${it}_msg",
                meta = when {
                    it % 2 == 0 -> generateMap()
                    it % 3 == 0 -> randomValue(4)
                    else -> null
                }
            ).sync()
            list.add(sync!!)
        }
        return list
    }

    fun generatePayload(): JsonObject {
        return JsonObject().apply {
            addProperty("text", randomValue())
            addProperty("uncd", unicode(8))
            addProperty("info", randomValue())
        }
    }

    fun generateMessage(pubnub: PubNub): JsonObject {
        return JsonObject().apply {
            addProperty("publisher", pubnub.configuration.userId.value)
            addProperty("text", randomValue())
            addProperty("uncd", unicode(8))
        }
    }

    fun generatePayloadJSON(): JSONObject {
        return JSONObject().apply {
            put("text", randomValue())
            put("uncd", unicode(8))
            put("info", randomValue())
        }
    }

    fun unicode(length: Int = 5): String {
        val unicodeChars = "!?+-="
        return unicodeChars.toList().shuffled().take(length).joinToString(separator = "")
    }

    fun emoji(): String {
        return listOf(
            "😀",
            "😁",
            "😂",
            "🤣",
            "😃",
            "😄",
            "😅",
            "😆",
            "😉",
            "😊",
            "😋",
            "😎",
            "😍",
            "😘",
            "🥰",
            "😗",
            "😙",
            "😚",
            "☺️",
            "🙂",
            "🤗",
            "🤩",
            "🤔",
            "🤨",
            "😐",
            "😑",
            "😶",
            "🙄",
            "😏",
            "😣",
            "😥",
            "😮",
            "🤐",
            "😯",
            "😪",
            "😫",
            "😴",
            "😌",
            "😛",
            "😜",
            "😝",
            "🤤",
            "😒",
            "😓",
            "😔",
            "😕",
            "🙃",
            "🤑",
            "😲",
            "☹️",
            "🙁",
            "😖",
            "😞",
            "😟",
            "😤",
            "😢",
            "😭",
            "😦",
            "😧",
            "😨",
            "😩",
            "🤯",
            "😬",
            "😰",
            "😱",
            "🥵",
            "🥶",
            "😳",
            "🤪",
            "😵",
            "😡",
            "😠",
            "🤬",
            "😷",
            "🤒",
            "🤕",
            "🤢",
            "🤮",
            "🤧",
            "😇",
            "🤠",
            "🤡",
            "🥳",
            "🥴",
            "🥺",
            "🤥",
            "🤫",
            "🤭",
            "🧐",
            "🤓",
            "😈",
            "👿",
            "👹",
            "👺",
            "💀",
            "👻",
            "👽",
            "🤖",
            "💩",
            "😺",
            "😸",
            "😹",
            "😻",
            "😼",
            "😽",
            "🙀",
            "😿",
            "😾"
        ).shuffled().take(5).joinToString("")
    }

    fun generateMap() = mapOf(
        "text" to randomValue(8),
        "uncd" to unicode(),
        "info" to randomValue(8)
    )

    fun createInterceptor(logger: Logger) =
        HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                logger.debug(message)
            }
        }).apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
}
