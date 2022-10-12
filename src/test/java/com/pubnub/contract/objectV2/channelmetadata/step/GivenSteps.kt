package com.pubnub.contract.objectV2.channelmetadata.step

import com.pubnub.contract.objectV2.channelmetadata.state.GetChannelMetadataState
import com.pubnub.contract.objectV2.channelmetadata.state.RemoveChannelMetadataState
import com.pubnub.contract.objectV2.channelmetadata.state.SetChannelMetadataState
import com.pubnub.contract.objectV2.uuidmetadata.step.loadChannelMetadata
import io.cucumber.java.en.Given

class GivenSteps(
    private val getChannelMetadataState: GetChannelMetadataState,
    private val setChannelMetadataState: SetChannelMetadataState,
    private val removeChannelMetadataState: RemoveChannelMetadataState

) {

    @Given("the id for {string} channel")
    fun the_id_for_Chat_channel(channelFileName: String) {
        val pnChannelMetadata = loadChannelMetadata(channelFileName)
        val channelId = pnChannelMetadata.id
        getChannelMetadataState.id = channelId
        removeChannelMetadataState.id = channelId
    }

    @Given("the data for {string} channel")
    fun the_data_for_Chat_channel(channelFileName: String) {
        val pnChannelMetadata = loadChannelMetadata(channelFileName)
        setChannelMetadataState.id = pnChannelMetadata.id
        setChannelMetadataState.pnChannelMetadata = pnChannelMetadata
        setChannelMetadataState.pnChannelMetadata?.name = pnChannelMetadata.name
        setChannelMetadataState.pnChannelMetadata?.description = pnChannelMetadata.description
        setChannelMetadataState.pnChannelMetadata?.status = pnChannelMetadata.status
        setChannelMetadataState.pnChannelMetadata?.type = pnChannelMetadata.type
    }
}
