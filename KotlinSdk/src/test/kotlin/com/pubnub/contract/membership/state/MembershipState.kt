package com.pubnub.contract.uuidmetadata.state

import com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembership

class MembershipState(
    private val uuidMetadataState: UUIDMetadataState
) {
    fun uuid(): String? = uuidMetadataState.uuid
    lateinit var returnedMemberships: Collection<PNChannelMembership>
    val membershipsToRemove: MutableCollection<PNChannelMembership> = mutableListOf()
    val memberships: MutableCollection<PNChannelMembership> = mutableListOf()
}
