package com.pubnub.extension

import com.pubnub.apikt.models.consumer.objects.PNPage
import com.pubnub.apikt.models.consumer.objects.member.PNMember
import com.pubnub.apikt.models.consumer.objects.member.PNMemberArrayResult
import com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembership
import com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembershipArrayResult
import com.pubnub.apikt.models.server.objects_api.EntityArrayEnvelope
import retrofit2.Response

internal fun Response<EntityArrayEnvelope<PNMember>>.toPNMemberArrayResult(): PNMemberArrayResult? =
    body()?.let { arrayEnvelope ->
        PNMemberArrayResult(
            status = arrayEnvelope.status,
            data = arrayEnvelope.data,
            totalCount = arrayEnvelope.totalCount,
            next = arrayEnvelope.next?.let { PNPage.PNNext(it) },
            prev = arrayEnvelope.prev?.let { PNPage.PNPrev(it) }
        )
    }

internal fun Response<EntityArrayEnvelope<PNChannelMembership>>.toPNChannelMembershipArrayResult(): PNChannelMembershipArrayResult? =
    body()?.let { arrayEnvelope ->
        PNChannelMembershipArrayResult(
            status = arrayEnvelope.status,
            data = arrayEnvelope.data,
            totalCount = arrayEnvelope.totalCount,
            next = arrayEnvelope.next?.let { PNPage.PNNext(it) },
            prev = arrayEnvelope.prev?.let { PNPage.PNPrev(it) }
        )
    }
