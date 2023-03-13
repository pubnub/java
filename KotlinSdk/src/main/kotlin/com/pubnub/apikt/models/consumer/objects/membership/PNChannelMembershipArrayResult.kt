package com.pubnub.apikt.models.consumer.objects.membership

import com.pubnub.apikt.models.consumer.objects.PNPage

data class PNChannelMembershipArrayResult(
    val status: Int,
    val data: Collection<PNChannelMembership>,
    val totalCount: Int?,
    val next: PNPage.PNNext?,
    val prev: PNPage.PNPrev?
)
