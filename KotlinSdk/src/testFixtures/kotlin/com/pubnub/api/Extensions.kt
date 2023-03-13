package com.pubnub.apikt

import com.pubnub.apikt.models.consumer.PNStatus
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.awaitility.pollinterval.FibonacciPollInterval
import org.hamcrest.core.IsEqual
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

fun <Input, Output> Endpoint<Input, Output>.await(function: (result: Output?, status: PNStatus) -> Unit) {
    val success = AtomicBoolean()
    async { result, status ->
        function.invoke(result, status)
        success.set(true)
    }
    success.listen()
}

fun PNStatus.param(param: String) = clientRequest!!.url.queryParameter(param)

fun PNStatus.encodedParam(param: String) =
    clientRequest!!.url.encodedQuery!!.encodedParamString(param)

fun String.encodedParamString(param: String): String {
    return split("&")
        .first { it.startsWith(param) }
        .split("=")[1]
}

fun AtomicBoolean.listen(function: () -> Boolean): AtomicBoolean {
    Awaitility.await()
        .atMost(Durations.FIVE_SECONDS)
        .with()
        .until {
            function.invoke()
        }
    return this
}

fun <Input, Output> Endpoint<Input, Output>.asyncRetry(
    function: (result: Output?, status: PNStatus) -> Unit
) {
    val hits = AtomicInteger(0)

    val block = {
        hits.incrementAndGet()
        val latch = CountDownLatch(1)
        val success = AtomicBoolean()
        queryParam += mapOf("key" to UUID.randomUUID().toString())
        async { result, status ->
            try {
                function.invoke(result, status)
                success.set(true)
            } catch (e: Throwable) {
                success.set(false)
            }
            latch.countDown()
        }
        latch.await(2L, TimeUnit.SECONDS)
        success.get()
    }

    Awaitility.await()
        .atMost(CommonUtils.DEFAULT_LISTEN_DURATION.toLong(), TimeUnit.SECONDS)
        .pollInterval(FibonacciPollInterval(TimeUnit.SECONDS))
        .until {
            block.invoke()
        }
}

fun <Input, Output> Endpoint<Input, Output>.retryForbidden(
    onFail: (status: PNStatus) -> Unit,
    function: (result: Output?, status: PNStatus) -> Unit
) {
    val success = AtomicBoolean()

    // first run should return forbidden
    async { _, status ->
        if (status.error && status.statusCode == 403) {
            onFail.invoke(status)
            success.set(false)
        }
    }

    Awaitility.await()
        .atMost(10L, TimeUnit.SECONDS)
        .untilAtomic(success, IsEqual.equalTo(false))

    Thread.sleep(2_000L)

    // retry and invoke callback
    queryParam += mapOf("key" to UUID.randomUUID().toString())
    async { result, status ->
        try {
            function.invoke(result, status)
            success.set(true)
        } catch (e: Throwable) {
            success.set(false)
        }
    }

    Awaitility.await()
        .atMost(CommonUtils.DEFAULT_LISTEN_DURATION.toLong(), TimeUnit.SECONDS)
        .pollInterval(FibonacciPollInterval(TimeUnit.SECONDS))
        .untilAtomic(success, IsEqual.equalTo(true))
}

fun AtomicBoolean.listen(seconds: Int = CommonUtils.DEFAULT_LISTEN_DURATION) {
    CommonUtils.observe(this, seconds)
}

fun PubNub.subscribeToBlocking(vararg channels: String) {
    this.subscribe(
        channels = listOf(*channels),
        withPresence = true
    )
    Thread.sleep(2000)
}

fun PubNub.unsubscribeFromBlocking(vararg channels: String) {
    unsubscribe(
        channels = listOf(*channels)
    )
    Thread.sleep(2000)
}
