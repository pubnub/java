package com.pubnub.apikt.manager

import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.managers.TelemetryManager
import io.mockk.MockKAnnotations
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.collection.IsMapWithSize
import org.hamcrest.core.Is
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Test

class TelemetryManagerTest {

    private lateinit var telemetryManager: TelemetryManager

    @Before
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @After
    fun teardown() = unmockkAll()

    @Test
    fun `should call cleanup every operationsLatency is called`() {
        telemetryManager = spyk(recordPrivateCalls = true)

        telemetryManager.operationsLatency()
        verify { telemetryManager["cleanUpTelemetryData"](any<Long>()) }
    }

    @Test
    fun `should convert latency to map`() {
        telemetryManager = spyk(recordPrivateCalls = true)
        val latencies = arrayOf(
            0L to PNOperationType.PNHistoryOperation,
            0L to PNOperationType.PNHistoryOperation,
            4L to PNOperationType.PNPublishOperation,
            2L to PNOperationType.PNPublishOperation,
            0L to PNOperationType.PNHistoryOperation,
            1L to PNOperationType.PNAccessManagerGrant
        )
        latencies.forEach {
            telemetryManager.storeLatency(it.first, it.second)
        }

        val expected: Map<String, String> = mapOf(
            "l_pub" to "0.003",
            // "l_his" to "0.000" // should be skipped
            "l_pam" to "0.001"
        )
        assertThat(telemetryManager.operationsLatency(), IsEqual.equalTo(expected))
    }

    @Test
    fun `should skip latency when endpoints average latency le 0f`() {
        telemetryManager = spyk(recordPrivateCalls = true)

        val latencies = arrayOf(
            0L to PNOperationType.PNSubscribeOperation,
            0L to PNOperationType.PNSubscribeOperation,
            0L to PNOperationType.PNSubscribeOperation,
            0L to PNOperationType.PNSubscribeOperation,
            0L to PNOperationType.PNSubscribeOperation
        )
        latencies.forEach {
            telemetryManager.storeLatency(it.first, it.second)
        }

        assertThat(telemetryManager.operationsLatency().keys, Matchers.emptyCollectionOf(String::class.java))
    }

    @Test
    fun `should skip endpoint latency when average latency le 0f`() {
        telemetryManager = spyk(recordPrivateCalls = true)
        val latencies = arrayOf(
            0L to PNOperationType.PNHistoryOperation,
            0L to PNOperationType.PNHistoryOperation,
            1L to PNOperationType.PNPublishOperation,
            2L to PNOperationType.PNPublishOperation,
            0L to PNOperationType.PNHistoryOperation
        )
        latencies.forEach {
            telemetryManager.storeLatency(it.first, it.second)
        }

        val publishKey = "l_${PNOperationType.PNPublishOperation.queryParam}"
        assertThat(telemetryManager.operationsLatency().keys, Is.`is`(setOf(publishKey)))
    }

    @Test
    fun `should remove outdated latencies from map`() {
        telemetryManager = spyk(recordPrivateCalls = true)
        val latency = 100L to PNOperationType.PNHistoryOperation
        telemetryManager.storeLatency(latency.first, latency.second)

        val operations = telemetryManager.operationsLatency(System.currentTimeMillis() + 65_000L)
        assertThat(operations, IsMapWithSize.anEmptyMap<String, String>())
    }
}
