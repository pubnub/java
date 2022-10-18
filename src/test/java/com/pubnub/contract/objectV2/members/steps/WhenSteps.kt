package com.pubnub.contract.objectV2.members.steps

import com.pubnub.api.endpoints.objects_api.utils.Include
import com.pubnub.api.models.consumer.objects_api.member.PNGetChannelMembersResult
import com.pubnub.api.models.consumer.objects_api.member.PNRemoveChannelMembersResult
import com.pubnub.api.models.consumer.objects_api.member.PNSetChannelMembersResult
import com.pubnub.api.models.consumer.objects_api.member.PNUUID
import com.pubnub.contract.objectV2.members.state.ChannelMembersState
import com.pubnub.contract.state.World
import io.cucumber.java.en.When

class WhenSteps(
    val world: World,
    val channelMembersState: ChannelMembersState
) {

    @When("I get the channel members")
    fun I_get_the_channel_members() {
        val pnGetChannelMembersResult: PNGetChannelMembersResult? =
            world.pubnub.getChannelMembers().channel(channelMembersState.channelId).sync()
        channelMembersState.memberList = pnGetChannelMembersResult?.data
        world.responseStatus = pnGetChannelMembersResult?.status
    }

    @When("I get the channel members including custom and UUID custom information")
    fun I_get_the_channel_members_including_custom_and_UUID_custom_information() {
        val pnGetChannelMembersResult: PNGetChannelMembersResult? = world.pubnub.getChannelMembers()
            .channel(channelMembersState.channelId)
            .includeCustom(true)
            .includeUUID(Include.PNUUIDDetailsLevel.UUID_WITH_CUSTOM)
            .sync()
        channelMembersState.memberList = pnGetChannelMembersResult?.data
        world.responseStatus = pnGetChannelMembersResult?.status
    }

    @When("I set a channel member")
    fun I_set_a_channel_member() {
        val setChannelMembersResult: PNSetChannelMembersResult? = world.pubnub.setChannelMembers()
            .channel(channelMembersState.channelId)
            .uuids(channelMembersState.uuids as MutableCollection<PNUUID>)
            .sync()

        channelMembersState.memberList = setChannelMembersResult?.data
        world.responseStatus = setChannelMembersResult?.status
    }

    @When("I set a channel member including custom and UUID with custom")
    fun I_set_a_channel_member_including_custom_and_UUID_with_custom() {
        val setChannelMembersResult: PNSetChannelMembersResult? = world.pubnub.setChannelMembers()
            .channel(channelMembersState.channelId)
            .uuids(channelMembersState.uuids as MutableCollection<PNUUID>)
            .includeCustom(true)
            .includeUUID(Include.PNUUIDDetailsLevel.UUID_WITH_CUSTOM)
            .sync()

        channelMembersState.memberList = setChannelMembersResult?.data
        world.responseStatus = setChannelMembersResult?.status
    }

    @When("I remove a channel member")
    fun I_remove_a_channel_member() {
        val pnRemoveChannelMembersResult: PNRemoveChannelMembersResult? = world.pubnub.removeChannelMembers()
            .channel(channelMembersState.channelId)
            .uuids(channelMembersState.uuids as MutableCollection<PNUUID>)
            .sync()

        world.responseStatus = pnRemoveChannelMembersResult?.status
    }
}
