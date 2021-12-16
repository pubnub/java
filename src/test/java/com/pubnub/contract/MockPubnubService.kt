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
import org.junit.Test
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface MockPubnubService {

    @GET("init")
    fun init(
        @QueryMap options: Map<String?, String?>
    ): Call<Any>

    @GET("expect")
    fun expect(): Call<ExpectResponse>
}

class A {

}

data class ExpectResponse(
    val contract: String,
    val expectations: Expectations
) {
    data class Expectations(
        val pending: List<String>,
        val failed: List<String>
    )
}
