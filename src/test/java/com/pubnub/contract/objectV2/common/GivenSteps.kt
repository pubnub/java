package com.pubnub.contract.objectV2.common

import com.pubnub.contract.objectV2.channelmetadata.state.GetChannelMetadataState
import com.pubnub.contract.objectV2.channelmetadata.state.RemoveChannelMetadataState
import com.pubnub.contract.objectV2.members.state.ChannelMembersState
import com.pubnub.contract.objectV2.uuidmetadata.step.loadChannelMetadata
import io.cucumber.java.en.Given

class GivenSteps(
    private val getChannelMetadataState: GetChannelMetadataState,
    private val removeChannelMetadataState: RemoveChannelMetadataState,
    private val channelMembersState: ChannelMembersState
) {

    @Given("the id for {string} channel")
    fun the_id_for_Chat_channel(channelFileName: String) {
        val pnChannelMetadata = loadChannelMetadata(channelFileName)
        val channelId = pnChannelMetadata.id
        getChannelMetadataState.id = channelId
        removeChannelMetadataState.id = channelId
        channelMembersState.channelId = channelId
    }
}
