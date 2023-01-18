package com.pubnub.contract.subscribe.steps

import com.pubnub.api.PubNub
import com.pubnub.api.callbacks.SubscribeCallback
import com.pubnub.api.models.consumer.PNStatus
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult
import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.contract.state.World
import com.pubnub.contract.subscribe.state.SubscribeState
import io.cucumber.java.en.Given
import java.util.*

class WhenSteps(
    private val world: World,
    private val subscribeState: SubscribeState
) {


    @Given("I subscribe to {string} channel")
    fun I_subscribe_to_the_channel(channelName: String) {
        val listener = getSubscribeCallbackImpl()
        world.pubnub.addListener(listener)
        world.pubnub.subscribe()
            .channels(Collections.singletonList(channelName))
            .execute()
    }


    private fun getSubscribeCallbackImpl(): SubscribeCallback {
        val listener = object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
            }

            override fun message(pubnub: PubNub, pnMessageResult: PNMessageResult) {
                subscribeState.messages += pnMessageResult
            }

            override fun presence(pubnub: PubNub, pnPresenceEventResult: PNPresenceEventResult) {
            }

            override fun signal(pubnub: PubNub, pnSignalResult: PNSignalResult) {
                subscribeState.messages += pnSignalResult
            }

            override fun uuid(pubnub: PubNub, pnUUIDMetadataResult: PNUUIDMetadataResult) {
            }

            override fun channel(pubnub: PubNub, pnChannelMetadataResult: PNChannelMetadataResult) {
            }

            override fun membership(pubnub: PubNub, pnMembershipResult: PNMembershipResult) {
            }

            override fun messageAction(pubnub: PubNub, pnMessageActionResult: PNMessageActionResult) {
            }

            override fun file(pubnub: PubNub, pnFileEventResult: PNFileEventResult) {
            }
        }

        return listener
    }
}