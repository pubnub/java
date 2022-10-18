package com.pubnub.contract.objectV2.membership.steps

import com.pubnub.contract.objectV2.membership.state.MembershipState
import com.pubnub.contract.objectV2.uuidmetadata.step.loadChannelMembership
import io.cucumber.java.en.Given

class GivenSteps(
    private var membershipState: MembershipState
) {

    @Given("the data for {string} membership")
    fun the_data_for_ChatMembership_membership(channelMembershipName: String){
        val channelMembership = loadChannelMembership(channelMembershipName)
        membershipState.membershipList = listOf(channelMembership)
    }
}
