package com.pubnub.contract.objectV2.channelmetadata.step

import com.pubnub.api.models.consumer.objects_api.channel.PNGetChannelMetadataResult
import com.pubnub.contract.objectV2.channelmetadata.state.GetAllChanelMetadataState
import com.pubnub.contract.objectV2.channelmetadata.state.GetChannelMetadataState
import com.pubnub.contract.objectV2.channelmetadata.state.RemoveChannelMetadataState
import com.pubnub.contract.objectV2.channelmetadata.state.SetChannelMetadataState
import com.pubnub.contract.state.World
import io.cucumber.java.en.When

class WhenSteps(
    val world: World,
    val getChannelMetadataState: GetChannelMetadataState,
    val setChannelMetadataState: SetChannelMetadataState,
    val removeChannelMetadataState: RemoveChannelMetadataState,
    val getAllChanelMetadataState: GetAllChanelMetadataState
) {

    @When("I get the channel metadata")
    fun I_get_the_channel_metadata() {
        val pnGetChannelMetadataResult: PNGetChannelMetadataResult? =
            world.pubnub.getChannelMetadata().channel(getChannelMetadataState.id).sync()
        getChannelMetadataState.pnChannelMetadata = pnGetChannelMetadataResult?.data
        world.responseStatus = pnGetChannelMetadataResult?.status
    }

    @When("I get the channel metadata with custom")
    fun I_get_the_channel_metadata_with_custom() {
        val pnGetChannelMetadataResult = world.pubnub.getChannelMetadata()
            .channel(getChannelMetadataState.id)
            .includeCustom(true)
            .sync()
        getChannelMetadataState.pnChannelMetadata = pnGetChannelMetadataResult?.data
        world.responseStatus = pnGetChannelMetadataResult?.status
    }

    @When("I set the channel metadata")
    fun I_set_the_channel_metadata() {
        val pnGetChannelMetadataResult = world.pubnub.setChannelMetadata()
            .channel(setChannelMetadataState.id)
            .name(setChannelMetadataState.pnChannelMetadata?.name)
            .description(setChannelMetadataState.pnChannelMetadata?.description)
            .status(setChannelMetadataState.pnChannelMetadata?.status)
            .type(setChannelMetadataState.pnChannelMetadata?.type)
            .sync()
        setChannelMetadataState.result = pnGetChannelMetadataResult
        world.responseStatus = pnGetChannelMetadataResult?.status
    }

    @When("I remove the channel metadata")
    fun I_remove_the_channel_metadata() {
        val pnRemoveChannelMetadataResult = world.pubnub.removeChannelMetadata()
            .channel(removeChannelMetadataState.id)
            .sync()
        world.responseStatus = pnRemoveChannelMetadataResult?.status
    }

    @When("I get all channel metadata")
    fun I_get_all_channel_metadata() {
        val pnGetAllChannelsMetadataResult = world.pubnub.getAllChannelsMetadata().sync()
        getAllChanelMetadataState.pnChannelMetadataList = pnGetAllChannelsMetadataResult?.data
        world.responseStatus = pnGetAllChannelsMetadataResult?.status
    }

    @When("I get all channel metadata with custom")
    fun I_get_all_channel_metadata_with_custom(){
        val pnGetAllChannelsMetadataResult = world.pubnub.getAllChannelsMetadata().includeCustom(true).sync()
        getAllChanelMetadataState.pnChannelMetadataList = pnGetAllChannelsMetadataResult?.data
        world.responseStatus = pnGetAllChannelsMetadataResult?.status
    }
}
