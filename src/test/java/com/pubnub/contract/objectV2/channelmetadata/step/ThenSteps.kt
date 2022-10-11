package com.pubnub.contract.objectV2.channelmetadata.step

import com.pubnub.contract.objectV2.channelmetadata.state.GetAllChanelMetadataState
import com.pubnub.contract.objectV2.channelmetadata.state.GetChannelMetadataState
import com.pubnub.contract.objectV2.channelmetadata.state.SetChannelMetadataState
import com.pubnub.contract.objectV2.uuidmetadata.step.loadChannelMetadata
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals

class ThenSteps(
    private val getChannelMetadataState: GetChannelMetadataState,
    private val setChannelMetadataState: SetChannelMetadataState,
    private val getAllChanelMetadataState: GetAllChanelMetadataState
) {

    @Then("the channel metadata for {string} channel")
    fun the_channel_metadata_for_specific_channel(channelName: String) {
        val expectedPNChannelMetadata = loadChannelMetadata(channelName)
        val actualPNChannelMetadata = getChannelMetadataState.pnChannelMetadata

        assertEquals(expectedPNChannelMetadata, actualPNChannelMetadata)
    }

    @Then("the channel metadata for {string} channel contains updated")
    fun the_channel_metadata_for_Chat_channel_contains_updated(channelName: String) {
        val expectedPNChannelMetadata = loadChannelMetadata(channelName)
        val actualPNChannelMetadata = setChannelMetadataState.result?.data

        assertEquals(expectedPNChannelMetadata, actualPNChannelMetadata)
    }

    @Then("the response contains list with {string} and {string} channel metadata")
    fun the_response_contains_list_for_first_and_second_channel_metadata(channel01Name: String, channel02Name: String) {
        val expectedPNChannelMetadataForFirstChannel = loadChannelMetadata(channel01Name)
        val expectedPNChannelMetadataForSecondChannel = loadChannelMetadata(channel02Name)


        assertThat(
            getAllChanelMetadataState.pnChannelMetadataList,
            Matchers.containsInAnyOrder(
                expectedPNChannelMetadataForFirstChannel,
                expectedPNChannelMetadataForSecondChannel
            )
        )
    }
}
