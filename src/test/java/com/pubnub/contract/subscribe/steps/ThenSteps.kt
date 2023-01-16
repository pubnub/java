package com.pubnub.contract.subscribe.steps

import com.pubnub.api.models.consumer.pubsub.PNMessageResult
import com.pubnub.api.models.consumer.pubsub.PNSignalResult
import com.pubnub.contract.subscribe.state.SubscribeState
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Assert
import java.util.*
import java.util.concurrent.TimeUnit

class ThenSteps(
    private val subscribeState: SubscribeState
) {

    @Then("I receive the message in my subscribe response")
    fun I_receive_the_message_in_my_subscribe_response() {
        if (!subscribeState.messageDeliveredToListener.await(3, TimeUnit.SECONDS)) {
            Assert.fail("Message/Signal has not been received by listener")
        }
    }

    @Then("subscribe response contains messages with {string} and {string} message types")
    fun subscribe_response_contains_messages_with_message_types(
        messageTypeOfFirstMessage: String,
        messageTypeOfSecondMessage: String
    ) {
        assertThat(subscribeState.messages.size, equalTo(2));
        val listOfReceivedMessageTypes = subscribeState.messages.map { it.messageType.value }
        assertThat(listOfReceivedMessageTypes, containsInAnyOrder(messageTypeOfFirstMessage, messageTypeOfSecondMessage))
    }

    @Then("subscribe response contains messages without space ids")
    fun subscribe_response_contains_messages_without_space_ids() {
        subscribeState.messages.stream().allMatch { it is PNMessageResult && it.spaceId == null }
    }

    @Then("subscribe response contains messages with space ids")
    fun subscribe_response_contains_messages_with_space_ids(){
        subscribeState.messages.stream().allMatch { it is PNMessageResult && it.spaceId != null }
    }
}
