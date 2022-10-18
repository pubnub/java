package com.pubnub.contract.objectV2.members.steps

import com.pubnub.contract.objectV2.members.state.ChannelMembersState
import com.pubnub.contract.objectV2.uuidmetadata.step.loadMember
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers

class ThenSteps(
    val channelMembersState:ChannelMembersState
) {

    @Then("the response contains list with {string} and {string} members")
    fun the_response_contains_list_with_members(firstMemberName: String, secondMemberName: String){
        val expectedPNMembersOfFirstUUID = loadMember(firstMemberName)
        val expectedPNMembersOfSecondUUID = loadMember(secondMemberName)

        assertThat(channelMembersState.memberList, Matchers.containsInAnyOrder(expectedPNMembersOfFirstUUID, expectedPNMembersOfSecondUUID))
    }

    @Then("the response contains list with {string} member")
    fun the_response_contains_list_with_members(memberName: String){
        val expectedPNMembers = loadMember(memberName)

        assertThat(channelMembersState.memberList, Matchers.containsInAnyOrder(expectedPNMembers))
    }
}
