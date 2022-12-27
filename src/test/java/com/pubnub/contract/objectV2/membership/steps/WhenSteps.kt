package com.pubnub.contract.objectV2.membership.steps

import com.pubnub.api.endpoints.objects_api.utils.Include
import com.pubnub.api.models.consumer.objects_api.membership.PNChannelMembership
import com.pubnub.api.models.consumer.objects_api.membership.PNRemoveMembershipResult
import com.pubnub.api.models.consumer.objects_api.membership.PNSetMembershipResult
import com.pubnub.contract.objectV2.membership.state.MembershipState
import com.pubnub.contract.state.World
import io.cucumber.java.en.When

class WhenSteps(
    private var world: World,
    private var membershipState: MembershipState
) {

    @When("I get the memberships")
    fun I_get_the_memberships() {
        val pnGetMembershipsResult = world.pubnub.getMemberships()
            .uuid(membershipState.uuid)
            .sync()

        membershipState.resultMembershipList = pnGetMembershipsResult?.data
        world.responseStatus = pnGetMembershipsResult?.status
    }

    @When("I get the memberships for current user")
    fun I_get_the_memberships_for_current_user() {
        val pnGetMembershipsResult = world.pubnub.getMemberships().sync()

        membershipState.resultMembershipList = pnGetMembershipsResult?.data
        world.responseStatus = pnGetMembershipsResult?.status
    }

    @When("I get the memberships including custom and channel custom information")
    fun I_get_the_memberships_including_custom_and_channel_custom_information() {
        val pnGetMembershipsResult = world.pubnub.getMemberships()
            .uuid(membershipState.uuid)
            .includeCustom(true)
            .includeChannel(Include.PNChannelDetailsLevel.CHANNEL_WITH_CUSTOM)
            .sync()

        membershipState.resultMembershipList = pnGetMembershipsResult?.data
        world.responseStatus = pnGetMembershipsResult?.status
    }

    @When("I set the membership")
    fun I_set_the_membership() {
        val channelId = membershipState.membershipToBeAdded?.channel?.id
        val channelMembership = listOf(PNChannelMembership.channel(channelId))
        val pnSetMembershipResult: PNSetMembershipResult? = world.pubnub.setMemberships()
            .channelMemberships(channelMembership)
            .uuid(membershipState.uuid)
            .sync()

        membershipState.resultMembershipList = pnSetMembershipResult?.data
        world.responseStatus = pnSetMembershipResult?.status
    }

    @When("I set the membership for current user")
    fun I_set_the_membership_for_current_user() {
        val channelId = membershipState.membershipToBeAdded?.channel?.id
        val channelMembership = listOf(PNChannelMembership.channel(channelId))
        val pnSetMembershipResult: PNSetMembershipResult? = world.pubnub.setMemberships()
            .channelMemberships(channelMembership)
            .sync()

        membershipState.resultMembershipList = pnSetMembershipResult?.data
        world.responseStatus = pnSetMembershipResult?.status
    }

    @When("I remove the membership")
    fun I_remove_the_membership() {
        val channelId = membershipState.membershipToBeRemoved?.channel?.id
        val channelMembership = listOf(PNChannelMembership.channel(channelId))
        val pnRemoveMembershipResult: PNRemoveMembershipResult? = world.pubnub.removeMemberships()
            .channelMemberships(channelMembership)
            .uuid(membershipState.uuid)
            .sync()

        membershipState.resultMembershipList = pnRemoveMembershipResult?.data
        world.responseStatus = pnRemoveMembershipResult?.status
    }

    @When("I remove the membership for current user")
    fun I_remove_the_membership_for_current_user() {
        val channelId = membershipState.membershipToBeRemoved?.channel?.id
        val channelMembership = listOf(PNChannelMembership.channel(channelId))
        val pnRemoveMembershipResult: PNRemoveMembershipResult? = world.pubnub.removeMemberships()
            .channelMemberships(channelMembership)
            .sync()

        membershipState.resultMembershipList = pnRemoveMembershipResult?.data
        world.responseStatus = pnRemoveMembershipResult?.status
    }

    @When("I manage memberships")
    fun I_manage_memberships() {
        val channelIdToBeAdded = membershipState.membershipToBeAdded?.channel?.id
        val channelMembershipToBeAdded = listOf(PNChannelMembership.channel(channelIdToBeAdded))
        val channelIdToBeRemoved = membershipState.membershipToBeRemoved?.channel?.id
        val channelMembershipToBeRemoved = listOf(PNChannelMembership.channel(channelIdToBeRemoved))
        val pnManageMembershipResult = world.pubnub.manageMemberships()
            .set(channelMembershipToBeAdded)
            .remove(channelMembershipToBeRemoved)
            .uuid(membershipState.uuid)
            .sync()

        membershipState.resultMembershipList = pnManageMembershipResult?.data
        world.responseStatus = pnManageMembershipResult?.status
    }
}
