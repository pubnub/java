package com.pubnub.contract.history.steps

import com.pubnub.api.MessageType
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

    @Then("history response contains messages with {string} and {string} message types")
    fun history_response_contains_messages_with_message_types(
        messageTypeOfFirstMessage: String,
        messageTypeOfSecondMessage: String
    ) {
        val messageTypeValuesInAllMessagesInAllChannels =
            fetchMessagesState.pnFetchMessagesResult?.getAllFetchMessageItemsFromAllChannels()
                ?.map { it.messageType.value }

        val uniqueMessageTypeValuesInAllMessagesInAllChannels: HashSet<String> = HashSet(messageTypeValuesInAllMessagesInAllChannels)

        assertThat(
            uniqueMessageTypeValuesInAllMessagesInAllChannels,
            containsInAnyOrder(messageTypeOfFirstMessage, messageTypeOfSecondMessage)
        )
    }

    @Then("history response contains messages without message types")
    fun history_response_contains_messages_without_message_types() {
        val messagesWithMessageType = fetchMessagesState.pnFetchMessagesResult?.getAllFetchMessageItemsFromAllChannels()
            ?.mapNotNull { it.messageType }
        assertThat(messagesWithMessageType, empty())
    }

    @Then("history response contains messages with message types")
    fun history_response_contains_messages_with_message_types() {
        val messagesWithMessageTypeEqualNull: List<MessageType>? =
            fetchMessagesState.pnFetchMessagesResult?.getAllFetchMessageItemsFromAllChannels()
                ?.map { it.messageType }?.filter { it -> it == null }
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
