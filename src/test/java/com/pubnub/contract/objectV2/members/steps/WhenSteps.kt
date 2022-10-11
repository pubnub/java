package com.pubnub.contract.objectV2.members.steps

import com.pubnub.contract.objectV2.channelmetadata.state.GetChannelMetadataState
import com.pubnub.contract.objectV2.members.state.GetChannelMembersState
import com.pubnub.contract.state.World
import io.cucumber.java.en.When

class WhenSteps(
    val world: World,
    val getChannelMembersState: GetChannelMembersState
) {

    @When("I get the channel members")
    fun I_get_the_channel_members(){
        world.pubnub.getChannelMembers().channel(getChannelMembersState.id).sync()
    }
}
