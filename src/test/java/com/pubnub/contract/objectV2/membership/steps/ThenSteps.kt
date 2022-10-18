package com.pubnub.contract.objectV2.membership.steps

import com.pubnub.contract.objectV2.membership.state.MembershipState
import com.pubnub.contract.objectV2.uuidmetadata.step.loadChannelMembership
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

class ThenSteps(
    private var membershipState: MembershipState
) {

    @Then("the response contains list with {string} and {string} memberships")
    fun the_response_contains_list_with_memberships(
        firstChannelMembershipName: String,
        secondChannelMembershipName: String
    ) {
        val expectedFirstChannelMembership = loadChannelMembership(firstChannelMembershipName)
        val expectedSecondChannelMembership = loadChannelMembership(secondChannelMembershipName)

        MatcherAssert.assertThat(
            membershipState.membershipList,
            Matchers.containsInAnyOrder(expectedFirstChannelMembership, expectedSecondChannelMembership)
        )
    }

    @Then("the response contains list with {string} membership")
    fun the_response_contains_list_with_ChatMembership_membership(channelMembershipName: String){
        val expectedChannelMembership = loadChannelMembership(channelMembershipName)

        MatcherAssert.assertThat(membershipState.membershipList, Matchers.containsInAnyOrder(expectedChannelMembership))
    }
}
