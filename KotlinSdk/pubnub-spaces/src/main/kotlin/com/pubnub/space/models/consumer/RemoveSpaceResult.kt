package com.pubnub.space.models.consumer

import com.pubnub.apikt.models.consumer.objects.PNRemoveMetadataResult

data class RemoveSpaceResult(
    val status: Int
)

internal fun PNRemoveMetadataResult?.toRemoveSpaceResult(): RemoveSpaceResult? {
    return this?.let { RemoveSpaceResult(status = it.status) }
}
