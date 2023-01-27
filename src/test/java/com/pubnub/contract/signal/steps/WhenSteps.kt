package com.pubnub.contract.signal.steps

import com.pubnub.api.MessageType
import com.pubnub.api.PubNubException
import com.pubnub.api.SpaceId
import com.pubnub.contract.state.World
import io.cucumber.java.en.When

class WhenSteps(
    val world: World
) {

    @When("I send a signal with {string} space id and {string} message type")
    fun I_send_a_signal_with_space_id_and_message_type(spaceIdValue: String, userMessageTypeValue: String) {
        val spaceId = SpaceId(spaceIdValue)
        val userMessageType = MessageType(userMessageTypeValue)
        val message = "how are you today?"
        val channel = "my favourite channel"
        try {
            val pnPublishResult = world.pubnub.signal()
                .channel(channel)
                .message(message)
                .spaceId(spaceId)
                .messageType(userMessageType)
                .sync()

            val timetoken = pnPublishResult?.timetoken

            world.responseStatus = if (timetoken != null && timetoken > 0) 200 else 400
        } catch (ex: PubNubException) {
            world.pnException = ex
        }
    }
}