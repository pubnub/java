package com.pubnub.membership.models.consumer

import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.UserId
import com.pubnub.apikt.models.consumer.objects.PNPage
import com.pubnub.apikt.models.consumer.objects.member.PNMemberArrayResult
import com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembershipArrayResult

data class MembershipsResult(
    val data: Collection<Membership>,
    val totalCount: Int?,
    val next: PNPage?,
    val prev: PNPage?
)

internal fun PNChannelMembershipArrayResult.toUserFetchMembershipsResult(userId: UserId): MembershipsResult {
    val userMembershipList: Collection<Membership> = data.map { pnChannelMembership ->
        pnChannelMembership.toUserMembership(userId)
    }
    return MembershipsResult(
        data = userMembershipList,
        totalCount = totalCount,
        next = next,
        prev = prev
    )
}

internal fun PNMemberArrayResult.toSpaceFetchMembershipResult(spaceId: SpaceId): MembershipsResult {
    val spaceMemberships: Collection<Membership> = data.map { pnMember ->
        pnMember.toSpaceMembership(spaceId)
    }

    return MembershipsResult(
        data = spaceMemberships,
        totalCount = totalCount,
        next = next,
        prev = prev
    )
}
