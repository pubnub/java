package com.pubnub.contract.publish.steps

import com.pubnub.api.models.consumer.history.MessageType
import com.pubnub.api.PubNubException
import com.pubnub.api.SpaceId
import com.pubnub.contract.state.World
import io.cucumber.java.en.When

class WhenSteps(
    private val world: World
) {

    @When("I publish message with {string} space id and {string} message type")
    fun I_publish_message_with_spaceId_and_messageType(spaceIdValue: String, userMessageTypeValue: String) {
        val spaceId = SpaceId(spaceIdValue)
        val userMessageType = MessageType(userMessageTypeValue)
        val message = "how are you today?"
        val channel = "my favourite channel"

        try {
            val pnPublishResult = world.pubnub.publish()
                .message(message)
                .channel(channel)
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
