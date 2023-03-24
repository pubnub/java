package com.pubnub.contract.history.steps

import com.pubnub.api.models.consumer.history.PNFetchMessageItem
import com.pubnub.api.models.consumer.history.PNFetchMessagesResult
import com.pubnub.contract.history.state.FetchMessagesState
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty

class ThenSteps(
    private val fetchMessagesState: FetchMessagesState
) {

    @Then("history response contains messages with {string} and {string} types")
    fun history_response_contains_messages_with_types(
        messageTypeOfFirstMessage: String,
        messageTypeOfSecondMessage: String
    ) {
        val messageTypeValuesInAllMessagesInAllChannels =
            fetchMessagesState.pnFetchMessagesResult?.getAllFetchMessageItemsFromAllChannels()
                ?.map { it.type }

        val uniqueMessageTypeValuesInAllMessagesInAllChannels: HashSet<String> = HashSet(messageTypeValuesInAllMessagesInAllChannels)

        assertThat(
            uniqueMessageTypeValuesInAllMessagesInAllChannels,
            containsInAnyOrder(messageTypeOfFirstMessage, messageTypeOfSecondMessage)
        )
    }

    @Then("history response contains messages with {integer} and {integer} message types")
    fun history_response_contains_messages_with_message_types(
        messageTypeOfFirstMessage: Int,
        messageTypeOfSecondMessage: Int
    ) {
        val messageTypeValuesInAllMessagesInAllChannels =
            fetchMessagesState.pnFetchMessagesResult?.getAllFetchMessageItemsFromAllChannels()
                ?.map { it.messageType.geteValueFromServer() }

        val uniqueMessageTypeValuesInAllMessagesInAllChannels: HashSet<Int> = HashSet(messageTypeValuesInAllMessagesInAllChannels)

        assertThat(
            uniqueMessageTypeValuesInAllMessagesInAllChannels,
            containsInAnyOrder(messageTypeOfFirstMessage, messageTypeOfSecondMessage)
        )
    }


    @Then("history response contains messages without types")
    fun history_response_contains_messages_without_types() {
        val messagesWithType = fetchMessagesState.pnFetchMessagesResult?.getAllFetchMessageItemsFromAllChannels()
            ?.mapNotNull { it.type }
        assertThat(messagesWithType, empty())
    }

    @Then("history response contains messages with message types")
    fun history_response_contains_messages_with_message_types() {
        val messagesWithMessageTypeEqualNull: List<String>? =
            fetchMessagesState.pnFetchMessagesResult?.getAllFetchMessageItemsFromAllChannels()
                ?.map { it.type }?.filter { it -> it == null }
        assertThat(messagesWithMessageTypeEqualNull, empty())
    }

    @Then("history response contains messages without space ids")
    fun history_response_contains_messages_without_space_ids() {
        val messagesWithSpaceId = fetchMessagesState.pnFetchMessagesResult?.getAllFetchMessageItemsFromAllChannels()
            ?.mapNotNull { it.spaceId }
        assertThat(messagesWithSpaceId, empty())
    }

    @Then("history response contains messages with space ids")
    fun history_response_contains_messages_with_space_ids() {
        val messagesWithSpaceIdEqualNull =
            fetchMessagesState.pnFetchMessagesResult?.getAllFetchMessageItemsFromAllChannels()
                ?.map { it.spaceId }?.filter { it -> it == null }
        assertThat(messagesWithSpaceIdEqualNull, empty())
    }

    private fun PNFetchMessagesResult.getAllFetchMessageItemsFromAllChannels(): List<PNFetchMessageItem> {
        return channels.values.flatten()
    }
}
