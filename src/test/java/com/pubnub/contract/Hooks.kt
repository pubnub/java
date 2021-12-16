package com.pubnub.contract

import com.pubnub.api.PNConfiguration
import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.enums.PNLogVerbosity
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGrant
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import io.cucumber.java.After
import io.cucumber.java.Before
import io.cucumber.java.Scenario
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class Hooks {
    private val interceptor = HttpLoggingInterceptor();
    private val mockPubnubService: MockPubnubService = Retrofit.Builder()
            .client(OkHttpClient.Builder().addInterceptor(interceptor).build())
            .baseUrl("http://" + CONTRACT_TEST_CONFIG.serverHostPort())
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(MockPubnubService::class.java)

    @Before
    fun before(scenario: Scenario) {
        if (!CONTRACT_TEST_CONFIG.serverMock()) {
            return
        }
        scenario.contractName()?.let {
            mockPubnubService.init(options = mapOf("__contract__script__" to it)).execute()
        }
    }

    @After
    fun after(scenario: Scenario) {
        if (!CONTRACT_TEST_CONFIG.serverMock()) {
            return
        }
        scenario.contractName()?.let {
            val responseBody = mockPubnubService.expect().execute().body()
            if (responseBody == null) {
                fail("Expect response body is null")
            } else {
                if (responseBody.expectations.pending.isNotEmpty() ||
                        responseBody.expectations.failed.isNotEmpty()) {
                    fail("""Scenario ${responseBody.contract} considered failure: 
                            pending - ${responseBody.expectations.pending.joinToString()},
                            failed - ${responseBody.expectations.failed.joinToString()}""".trimIndent())
                }
            }

        }
    }

    private fun Scenario.contractName(): String? {
        return sourceTagNames
                .filter { it: String -> it.startsWith("@contract") }
                .map { it: String ->
                    it.split("=").toTypedArray()[1]
                }
                .firstOrNull()
    }
}
