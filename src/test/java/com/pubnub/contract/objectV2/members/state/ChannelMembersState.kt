package com.pubnub.contract.objectV2.members.state

import com.pubnub.api.models.consumer.objects_api.member.PNMembers

class ChannelMembersState {
    var channelId: String? = null
    var resultMemberList: List<PNMembers>? = mutableListOf()
    var memberToBeAdded: PNMembers? = null
    var memberToBeDeleted: PNMembers? = null
}
