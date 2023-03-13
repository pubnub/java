package com.pubnub.contract.member.state

import com.pubnub.apikt.models.consumer.objects.member.PNMember
import com.pubnub.contract.channelmetadata.state.ChannelMetadataState

class MemberState(
    private val channelMetadataState: ChannelMetadataState
) {
    fun channelId(): String = channelMetadataState.channelId
    val members: MutableCollection<PNMember> = mutableListOf()
    val membersToRemove: MutableCollection<PNMember> = mutableListOf()
    lateinit var returnedMembers: Collection<PNMember>
}
