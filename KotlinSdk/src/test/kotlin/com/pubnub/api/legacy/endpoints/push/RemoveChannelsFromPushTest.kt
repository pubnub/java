package com.pubnub.apikt.legacy.endpoints.push

import com.github.tomakehurst.wiremock.client.WireMock
import com.pubnub.apikt.CommonUtils.assertPnException
import com.pubnub.apikt.CommonUtils.failTest
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNPushType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.legacy.BaseTest
import com.pubnub.apikt.listen
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean

class RemoveChannelsFromPushTest : BaseTest() {

    @Test
    fun testRemoveAppleSuccessSync() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(WireMock.aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.removePushNotificationsFromChannels(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS,
            channels = listOf("chr1", "chr2", "chr3")
        ).sync()!!

        val requests = WireMock.findAll(WireMock.getRequestedFor(WireMock.urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("apns", requests[0].queryParameter("type").firstValue())
        assertEquals("chr1,chr2,chr3", requests[0].queryParameter("remove").firstValue())
        assertFalse(requests[0].queryParameter("environment").isPresent)
        assertFalse(requests[0].queryParameter("topic").isPresent)
    }

    @Test
    fun testRemoveFirebaseSuccessSync() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(WireMock.aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.removePushNotificationsFromChannels(
            deviceId = "niceDevice",
            pushType = PNPushType.FCM,
            channels = listOf("chr1", "chr2", "chr3")
        ).sync()!!

        val requests = WireMock.findAll(WireMock.getRequestedFor(WireMock.urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("gcm", requests[0].queryParameter("type").firstValue())
        assertEquals("chr1,chr2,chr3", requests[0].queryParameter("remove").firstValue())
        assertFalse(requests[0].queryParameter("environment").isPresent)
        assertFalse(requests[0].queryParameter("topic").isPresent)
    }

    @Test
    fun testRemoveMicrosoftSuccessSync() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(WireMock.aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.removePushNotificationsFromChannels(
            deviceId = "niceDevice",
            pushType = PNPushType.MPNS,
            channels = listOf("chr1", "chr2", "chr3")
        ).sync()!!

        val requests = WireMock.findAll(WireMock.getRequestedFor(WireMock.urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("mpns", requests[0].queryParameter("type").firstValue())
        assertEquals("chr1,chr2,chr3", requests[0].queryParameter("remove").firstValue())
        assertFalse(requests[0].queryParameter("environment").isPresent)
        assertFalse(requests[0].queryParameter("topic").isPresent)
    }

    @Test
    fun testRemoveApns2SuccessSync() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/niceDevice"))
                .willReturn(WireMock.aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.removePushNotificationsFromChannels(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS2,
            channels = listOf("chr1", "chr2", "chr3"),
            topic = "news"
        ).sync()!!

        val requests = WireMock.findAll(WireMock.getRequestedFor(WireMock.urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("chr1,chr2,chr3", requests[0].queryParameter("remove").firstValue())
        assertEquals(
            "development",
            requests[0].queryParameter("environment").firstValue()
        )
        assertEquals("news", requests[0].queryParameter("topic").firstValue())
        assertFalse(requests[0].queryParameter("type").isPresent)
    }

    @Test
    fun testIsAuthRequiredSuccessRemove() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(WireMock.aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.configuration.authKey = "myKey"

        pubnub.removePushNotificationsFromChannels(
            deviceId = "niceDevice",
            pushType = PNPushType.FCM,
            channels = listOf("chr1", "chr2", "chr3"),
            topic = "news"
        ).sync()!!

        val requests = WireMock.findAll(WireMock.getRequestedFor(WireMock.urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testOperationTypeSuccessRemove() {
        WireMock.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(WireMock.aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        val success = AtomicBoolean()

        pubnub.removePushNotificationsFromChannels(
            deviceId = "niceDevice",
            pushType = PNPushType.FCM,
            channels = listOf("chr1", "chr2", "chr3")
        ).async { _, status ->
            assertFalse(status.error)
            assertEquals(
                PNOperationType.PNRemovePushNotificationsFromChannelsOperation,
                status.operation
            )
            assertEquals(PNStatusCategory.PNAcknowledgmentCategory, status.category)
            assertEquals(status.affectedChannels, listOf("chr1", "chr2", "chr3"))
            assertTrue(status.affectedChannelGroups.isEmpty())
            success.set(true)
        }

        success.listen()
    }

    @Test
    fun testEmptySubscribeKeyRemove() {
        pubnub.configuration.subscribeKey = " "

        try {
            pubnub.removePushNotificationsFromChannels(
                deviceId = "niceDevice",
                pushType = PNPushType.FCM,
                channels = listOf("chr1", "chr2", "chr3")
            ).sync()!!
            failTest()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testEmptyDeviceId() {
        try {
            pubnub.removePushNotificationsFromChannels(
                pushType = PNPushType.FCM,
                deviceId = " ",
                channels = listOf("chr1", "chr2", "chr3")
            ).sync()!!
            failTest()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.DEVICE_ID_MISSING, e)
        }
    }

    @Test
    fun testEmptyChannels() {
        try {
            pubnub.removePushNotificationsFromChannels(
                pushType = PNPushType.FCM,
                deviceId = "niceDevice",
                channels = emptyList()
            ).sync()!!
            failTest()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.CHANNEL_MISSING, e)
        }
    }
}
