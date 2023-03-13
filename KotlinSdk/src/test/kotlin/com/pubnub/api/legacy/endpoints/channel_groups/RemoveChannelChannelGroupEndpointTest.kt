package com.pubnub.apikt.legacy.endpoints.channel_groups

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.legacy.BaseTest
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class RemoveChannelChannelGroupEndpointTest : BaseTest() {

    @Test
    fun testSyncSuccess() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "status": 200,
                             "message": "OK",
                             "payload": {},
                             "service": "ChannelGroups"
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.removeChannelsFromChannelGroup(
            channelGroup = "groupA",
            channels = arrayListOf("ch1", "ch2")
        ).sync()!!
    }

    @Test
    fun testIsAuthRequiredSuccessSync() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "status": 200,
                             "message": "OK",
                             "payload": {},
                             "service": "ChannelGroups"
                            }
                        """.trimIndent()
                    )
                )
        )

        pubnub.configuration.authKey = "myKey"

        pubnub.removeChannelsFromChannelGroup(
            channelGroup = "groupA",
            channels = arrayListOf("ch1", "ch2")
        ).sync()!!

        val requests = findAll(getRequestedFor(urlMatching("/.*")))
        assertEquals(1, requests.size)
        assertEquals("myKey", requests[0].queryParameter("auth").firstValue())
    }

    @Test
    fun testOperationTypeSuccessAsync() {
        stubFor(
            get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(
                    aResponse().withBody(
                        """
                            {
                             "status": 200,
                             "message": "OK",
                             "payload": {},
                             "service": "ChannelGroups"
                            }
                        """.trimIndent()
                    )
                )
        )

        val atomic = AtomicInteger(0)

        pubnub.removeChannelsFromChannelGroup(
            channelGroup = "groupA",
            channels = arrayListOf("ch1", "ch2")
        ).async { _, status ->
            assertFalse(status.error)
            assertEquals(PNOperationType.PNRemoveChannelsFromGroupOperation, status.operation)
            assertEquals(PNStatusCategory.PNAcknowledgmentCategory, status.category)
            assertTrue(status.affectedChannels == listOf("ch1", "ch2"))
            assertTrue(status.affectedChannelGroups == listOf("groupA"))
            atomic.incrementAndGet()
        }

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, IsEqual.equalTo(1))
    }
}
