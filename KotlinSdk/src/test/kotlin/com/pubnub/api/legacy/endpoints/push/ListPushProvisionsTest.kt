package com.pubnub.apikt.legacy.endpoints.push

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushEnvironment
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.listen
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class ListPushProvisionsTest : BaseTest() {

    @Test
    fun testAppleSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("""["ch1", "ch2", "ch3"]"""))
        )

        val response = pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS,
            topic = "irrelevant"
        ).sync()!!

        assertEquals(listOf("ch1", "ch2", "ch3"), response.channels)

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("apns", requests[0].queryParameter("type").firstValue())
        assertFalse(requests[0].queryParameter("environment").isPresent)
        assertFalse(requests[0].queryParameter("topic").isPresent)
    }

    @Test
    fun testFirebaseSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("""["ch1", "ch2", "ch3"]"""))
        )

        val response = pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.FCM,
            topic = "irrelevant"
        ).sync()!!

        assertEquals(listOf("ch1", "ch2", "ch3"), response.channels)

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("gcm", requests[0].queryParameter("type").firstValue())
        assertFalse(requests[0].queryParameter("environment").isPresent)
        assertFalse(requests[0].queryParameter("topic").isPresent)
    }

    @Test
    fun testMicrosoftSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("""["ch1", "ch2", "ch3"]"""))
        )

        val response = pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.MPNS,
            topic = "irrelevant"
        ).sync()!!

        assertEquals(listOf("ch1", "ch2", "ch3"), response.channels)

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("mpns", requests[0].queryParameter("type").firstValue())
        assertFalse(requests[0].queryParameter("environment").isPresent)
        assertFalse(requests[0].queryParameter("topic").isPresent)
    }

    @Test
    fun testApns2SuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/niceDevice"))
                .willReturn(aResponse().withBody("""["ch1", "ch2", "ch3"]"""))
        )

        val response = pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS2,
            topic = "news"
        ).sync()!!

        assertEquals(listOf("ch1", "ch2", "ch3"), response.channels)

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("development", requests[0].queryParameter("environment").firstValue())
        assertEquals("news", requests[0].queryParameter("topic").firstValue())
    }

    @Test
    fun testApns2SuccessSync_Environment() {
        stubFor(
            get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/niceDevice"))
                .willReturn(aResponse().withBody("""["ch1", "ch2", "ch3"]"""))
        )

        val response = pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS2,
            topic = "news",
            environment = PNPushEnvironment.PRODUCTION
        ).sync()!!

        assertEquals(listOf("ch1", "ch2", "ch3"), response.channels)

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("production", requests[0].queryParameter("environment").firstValue())
        assertEquals("news", requests[0].queryParameter("topic").firstValue())
    }

    @Test
    fun testEmptyArray() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[]"))
        )

        val response = pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.FCM
        ).sync()!!

        assertEquals(emptyList<String>(), response.channels)

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
    }

    @Test
    fun testEmptyBody() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody(""))
        )

        try {
            pubnub.auditPushChannelProvisions(
                deviceId = "niceDevice",
                pushType = PNPushType.FCM
            ).sync()!!
            failTest()
        } catch (e: Exception) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testMalformedResponseSync() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(ok().withBody("{"))
        )

        try {
            pubnub.auditPushChannelProvisions(
                deviceId = "niceDevice",
                pushType = PNPushType.FCM
            ).sync()!!
            failTest()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.PARSING_ERROR, e)
        }
    }

    @Test
    fun testMalformedResponseAsync() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(ok().withBody("{"))
        )

        val success = AtomicBoolean()
        pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.FCM
        ).async { _, status ->
            assertPnException(PubNubError.PARSING_ERROR, status)
            assertEquals(PNStatusCategory.PNMalformedResponseCategory, status.category)
            success.set(true)
        }
        success.listen()
    }

    @Test
    fun testIsAuthRequiredSuccess() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("""["ch1", "ch2", "ch3"]"""))
        )

        pubnub.configuration.authKey = "myKey"

        pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS
        ).sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testOperationTypeSuccess() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("""["ch1", "ch2", "ch3"]"""))
        )

        val success = AtomicBoolean()

        pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS
        ).async { _, status ->
            assertFalse(status.error)
            assertEquals(
                PNOperationType.PNPushNotificationEnabledChannelsOperation,
                status.operation
            )
            assertEquals(PNStatusCategory.PNAcknowledgmentCategory, status.category)
            assertTrue(status.affectedChannels.isEmpty())
            assertTrue(status.affectedChannelGroups.isEmpty())
            success.set(true)
        }

        success.listen()
    }

    @Test
    fun testNullSubscribeKey() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("""["ch1", "ch2", "ch3"]"""))
        )

        pubnub.configuration.subscribeKey = " "

        try {
            pubnub.auditPushChannelProvisions(
                deviceId = "niceDevice",
                pushType = PNPushType.APNS
            ).sync()!!
            failTest("Didn't throw SUBSCRIBE_KEY_MISSING")
        } catch (e: Exception) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testValidationNoTopic() {
        try {
            pubnub.auditPushChannelProvisions(
                deviceId = "niceDevice",
                pushType = PNPushType.APNS2
            ).sync()!!
            failTest("Should throw no topic")
        } catch (e: PubNubException) {
            assertPnException(PubNubError.PUSH_TOPIC_MISSING, e)
        }
    }

    @Test
    fun testValidationDefaultEnvironment() {
        stubFor(
            get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/niceDevice"))
                .willReturn(aResponse().withBody("""["ch1", "ch2", "ch3"]"""))
        )

        pubnub.auditPushChannelProvisions(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS2,
            topic = UUID.randomUUID().toString()
        ).sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("development", requests[0].queryParameter("environment").firstValue())
    }
}
