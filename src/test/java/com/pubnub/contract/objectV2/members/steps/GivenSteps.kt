package com.pubnub.contract.objectV2.members.steps

import com.pubnub.api.models.consumer.objects_api.member.PNMembers
import com.pubnub.api.models.consumer.objects_api.member.PNUUID
import com.pubnub.contract.objectV2.members.state.ChannelMembersState
import com.pubnub.contract.objectV2.uuidmetadata.step.loadMember
import io.cucumber.java.en.Given

class GivenSteps(
    private val channelMembersState: ChannelMembersState
) {

    @Given("the data for {string} member")
    fun the_data_for_member(memberName: String) {
        val channelMember: PNMembers = loadMember(memberName)
        channelMembersState.uuids = listOf(PNUUID.uuid(channelMember.uuid.id))
    }
}
