package com.pubnub.space.models.consumer

import com.pubnub.apikt.models.consumer.objects.PNPage
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadataArrayResult
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadataResult

data class SpacesResult(
    val data: Collection<Space>,
    val totalCount: Int?,
    val next: PNPage?,
    val prev: PNPage?
)

internal fun PNChannelMetadataArrayResult?.toSpacesResult(): SpacesResult? {
    if (this == null) {
        return null
    }

    val spaces = data.map { pnChannelMetadata ->
        pnChannelMetadata.toSpace()
    }

    return SpacesResult(data = spaces, totalCount = totalCount, next = next, prev = prev)
}

internal fun PNChannelMetadataResult?.toSpace(): Space? {
    if (this == null) {
        return null
    }

    return data?.toSpace()
}
