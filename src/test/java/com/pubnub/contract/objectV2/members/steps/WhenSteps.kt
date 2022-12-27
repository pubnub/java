package com.pubnub.contract.objectV2.members.steps

import com.pubnub.api.endpoints.objects_api.utils.Include
import com.pubnub.api.models.consumer.objects_api.member.*
import com.pubnub.contract.objectV2.members.state.ChannelMembersState
import com.pubnub.contract.state.World
import io.cucumber.java.en.When

class WhenSteps(
    val world: World,
    val channelMembersState: ChannelMembersState
) {

    @When("I get the channel members")
    fun I_get_the_channel_members() {
        val pnGetChannelMembersResult =
            world.pubnub.getChannelMembers().channel(channelMembersState.channelId).sync()
        channelMembersState.resultMemberList = pnGetChannelMembersResult?.data
        world.responseStatus = pnGetChannelMembersResult?.status
    }

    @When("I get the channel members including custom and UUID custom information")
    fun I_get_the_channel_members_including_custom_and_UUID_custom_information() {
        val pnGetChannelMembersResult = world.pubnub.getChannelMembers()
            .channel(channelMembersState.channelId)
            .includeCustom(true)
            .includeUUID(Include.PNUUIDDetailsLevel.UUID_WITH_CUSTOM)
            .sync()
        channelMembersState.resultMemberList = pnGetChannelMembersResult?.data
        world.responseStatus = pnGetChannelMembersResult?.status
    }

    @When("I set a channel member")
    fun I_set_a_channel_member() {
        val pnUuidList = listOf(PNUUID.uuid(channelMembersState.memberToBeAdded?.uuid?.id))
        val pnSetChannelMembersResult = world.pubnub.setChannelMembers()
            .channel(channelMembersState.channelId)
            .uuids(pnUuidList)
            .sync()

        channelMembersState.resultMemberList = pnSetChannelMembersResult?.data
        world.responseStatus = pnSetChannelMembersResult?.status
    }

    @When("I set a channel member including custom and UUID with custom")
    fun I_set_a_channel_member_including_custom_and_UUID_with_custom() {
        val pnUuidList = listOf(PNUUID.uuid(channelMembersState.memberToBeAdded?.uuid?.id))
        val pnSetChannelMembersResult = world.pubnub.setChannelMembers()
            .channel(channelMembersState.channelId)
            .uuids(pnUuidList)
            .includeCustom(true)
            .includeUUID(Include.PNUUIDDetailsLevel.UUID_WITH_CUSTOM)
            .sync()

        channelMembersState.resultMemberList = pnSetChannelMembersResult?.data
        world.responseStatus = pnSetChannelMembersResult?.status
    }

    @When("I remove a channel member")
    fun I_remove_a_channel_member() {
        val pnUuidList = listOf(PNUUID.uuid(channelMembersState.memberToBeRemoved?.uuid?.id))
        val pnRemoveChannelMembersResult = world.pubnub.removeChannelMembers()
            .channel(channelMembersState.channelId)
            .uuids(pnUuidList)
            .sync()

        world.responseStatus = pnRemoveChannelMembersResult?.status
    }

    @When("I manage channel members")
    fun I_manage_channel_members() {
        val pnUuidListToBeAdded = listOf(PNUUID.uuid(channelMembersState.memberToBeAdded?.uuid?.id))
        val pnUuidListToBeDeleted = listOf(PNUUID.uuid(channelMembersState.memberToBeRemoved?.uuid?.id))
        val pnManageChannelMembersResult = world.pubnub.manageChannelMembers()
            .channel(channelMembersState.channelId)
            .set(pnUuidListToBeAdded)
            .remove(pnUuidListToBeDeleted)
            .sync()

        world.responseStatus = pnManageChannelMembersResult?.status
        channelMembersState.resultMemberList =
            pnManageChannelMembersResult?.data as MutableList<PNMembers>
    }
}
