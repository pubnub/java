package com.pubnub.contract.history.steps

import com.pubnub.contract.history.state.FetchMessagesState
import com.pubnub.contract.state.World
import io.cucumber.java.en.When
import java.util.Collections

class WhenSteps(
    private val world: World,
    private val fetchMessagesState: FetchMessagesState
) {


    @When("I fetch message history for {string} channel")
    fun I_fetch_message_history_for_channel(channelName: String){
        val pnFetchMessagesResult = world.pubnub.fetchMessages()
            .channels(Collections.singletonList(channelName))
            .sync();

        fetchMessagesState.pnFetchMessagesResult = pnFetchMessagesResult
        world.responseStatus = 200
    }

    @When("I fetch message history with 'includeMessageType' set to {string} for {string} channel")
    fun I_fetch_message_history_with_includeMessageType_set_to_value_fo_channel(includeMessageTypeValue: String, channelName: String){
        val pnFetchMessagesResult = world.pubnub.fetchMessages()
            .channels(listOf(channelName))
            .includeMessageType(includeMessageTypeValue.toBoolean())
            .sync()

        fetchMessagesState.pnFetchMessagesResult = pnFetchMessagesResult
        world.responseStatus = 200
    }

    @When("I fetch message history with 'includeSpaceId' set to {string} for {string} channel")
    fun I_fetch_message_history_with_includeSpaceId_set_to_value_fo_channel(includeSpaceIdValue: String, channelName: String){
        val pnFetchMessagesResult =world.pubnub.fetchMessages()
            .channels(listOf(channelName))
            .includeSpaceId(includeSpaceIdValue.toBoolean())
            .sync()
        fetchMessagesState.pnFetchMessagesResult = pnFetchMessagesResult
        world.responseStatus = 200
    }
}