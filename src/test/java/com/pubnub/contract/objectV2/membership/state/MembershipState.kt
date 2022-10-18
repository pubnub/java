package com.pubnub.contract.objectV2.membership.state

import com.pubnub.api.models.consumer.objects_api.membership.PNMembership

class MembershipState {
    var uuid: String? = null
    var membershipList: Collection<PNMembership>? = mutableListOf()
}
