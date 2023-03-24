package com.pubnub.contract.subscribe.steps

import com.pubnub.contract.subscribe.state.SubscribeState
import io.cucumber.java.en.Then
import org.awaitility.Awaitility.await
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*
import java.util.concurrent.TimeUnit

class ThenSteps(
    private val subscribeState: SubscribeState
) {

    @Then("I receive 2 messages in my subscribe response")
    fun I_receive_2_messages_in_my_subscribe_response() {
        await()
            .atMost(500, TimeUnit.MILLISECONDS)
            .until {
                subscribeState.messages.size == 2
            }
    }

    @Then("response contains messages with {string} and {string} types")
    fun response_contains_messages_with_message_types(
        messageTypeOfFirstMessage: String,
        messageTypeOfSecondMessage: String
    ) {
        val listOfReceivedMessageTypes = subscribeState.messages.map { it.type }
        assertThat(
            listOfReceivedMessageTypes,
            containsInAnyOrder(messageTypeOfFirstMessage, messageTypeOfSecondMessage)
        )
    }

    @Then("response contains messages without space ids")
    fun response_contains_messages_without_space_ids() {
        assertEquals(listOf<String>(), subscribeState.messages.mapNotNull { it.spaceId })
    }

    @Then("response contains messages with space ids")
    fun response_contains_messages_with_space_ids() {
        assertTrue(subscribeState.messages.all { it.spaceId != null })
    }
}
