package com.pubnub.contract.objectV2.membership.steps

import com.pubnub.contract.objectV2.membership.state.MembershipState
import com.pubnub.contract.objectV2.uuidmetadata.step.loadChannelMembership
import io.cucumber.java.en.Then
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.CoreMatchers.not
import org.hamcrest.MatcherAssert.*
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

        assertThat(
            membershipState.resultMembershipList,
            Matchers.containsInAnyOrder(expectedFirstChannelMembership, expectedSecondChannelMembership)
        )
    }

    @Then("the response contains list with {string} membership")
    fun the_response_contains_list_with_membership(channelMembershipName: String) {
        val expectedChannelMembership = loadChannelMembership(channelMembershipName)

        assertThat(membershipState.resultMembershipList, Matchers.containsInAnyOrder(expectedChannelMembership))
    }

    @Then("the response does not contain list with {string} membership")
    fun the_response_does_not_contain_list_with_membership(channelMembershipName: String) {
        val expectedChannelMembership = loadChannelMembership(channelMembershipName)

        assertThat(membershipState.resultMembershipList, not(hasItem(expectedChannelMembership)))
    }
}
