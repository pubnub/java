package com.pubnub.membership.models.consumer

import com.pubnub.apikt.models.consumer.objects.member.PNMemberArrayResult
import com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembershipArrayResult

data class MembershipsStatusResult(
    val status: Int
)

internal fun PNChannelMembershipArrayResult.toUserMembershipsResult(): MembershipsStatusResult {
    return MembershipsStatusResult(
        status = status
    )
}

internal fun PNMemberArrayResult.toSpaceMembershipResult(): MembershipsStatusResult {
    return MembershipsStatusResult(
        status = status
    )
}
