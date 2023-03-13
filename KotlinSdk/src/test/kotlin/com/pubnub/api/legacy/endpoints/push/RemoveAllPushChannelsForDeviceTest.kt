package com.pubnub.apikt.legacy.endpoints.push

import com.github.tomakehurst.wiremock.client.WireMock.*
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

class RemoveAllPushChannelsForDeviceTest : BaseTest() {

    @Test
    fun testAppleSuccessSyncRemoveAll() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice/remove"))
                .willReturn(aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS
        ).sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("apns", requests[0].queryParameter("type").firstValue())
        assertFalse(requests[0].queryParameter("environment").isPresent)
        assertFalse(requests[0].queryParameter("topic").isPresent)
    }

    @Test
    fun testFirebaseSuccessSyncRemoveAll() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice/remove"))
                .willReturn(aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
            deviceId = "niceDevice",
            pushType = PNPushType.FCM
        ).sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("gcm", requests[0].queryParameter("type").firstValue())
        assertFalse(requests[0].queryParameter("environment").isPresent)
        assertFalse(requests[0].queryParameter("topic").isPresent)
    }

    @Test
    fun testMicrosoftSuccessSyncRemoveAll() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice/remove"))
                .willReturn(aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
            deviceId = "niceDevice",
            pushType = PNPushType.MPNS
        ).sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("mpns", requests[0].queryParameter("type").firstValue())
        assertFalse(requests[0].queryParameter("environment").isPresent)
        assertFalse(requests[0].queryParameter("topic").isPresent)
    }

    @Test
    fun testApns2SuccessSyncRemoveAll() {
        stubFor(
            get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/niceDevice/remove"))
                .willReturn(aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
            deviceId = "niceDevice",
            pushType = PNPushType.APNS2,
            topic = "news"
        ).sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals(
            "development",
            requests[0].queryParameter("environment").firstValue()
        )
        assertEquals("news", requests[0].queryParameter("topic").firstValue())
        assertFalse(requests[0].queryParameter("type").isPresent)
    }

    @Test
    fun testIsAuthRequiredSuccessRemoveAll() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice/remove"))
                .willReturn(aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        pubnub.configuration.authKey = "myKey"

        pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
            deviceId = "niceDevice",
            pushType = PNPushType.FCM
        ).sync()

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testOperationTypeSuccessRemoveAll() {
        stubFor(
            get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice/remove"))
                .willReturn(aResponse().withBody("[1, \"Modified Channels\"]"))
        )

        val success = AtomicBoolean()

        pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
            deviceId = "niceDevice",
            pushType = PNPushType.FCM
        ).async { _, status ->
            assertFalse(status.error)
            assertEquals(
                PNOperationType.PNRemoveAllPushNotificationsOperation,
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
    fun testEmptySubscribeKeyRemoveAll() {
        pubnub.configuration.subscribeKey = " "

        try {
            pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
                deviceId = "niceDevice",
                pushType = PNPushType.FCM
            ).sync()!!
            failTest()
        } catch (e: PubNubException) {
            assertPnException(PubNubError.SUBSCRIBE_KEY_MISSING, e)
        }
    }

    @Test
    fun testEmptyDeviceIdRemoveAll() {
        try {
            pubnub.removeAllPushNotificationsFromDeviceWithPushToken(
                pushType = PNPushType.FCM,
                deviceId = " "
            ).sync()!!
        } catch (e: PubNubException) {
            assertPnException(PubNubError.DEVICE_ID_MISSING, e)
        }
    }
}
