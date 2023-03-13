package com.pubnub.space.models.consumer

import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadata

data class Space(
    val id: SpaceId,
    val name: String? = null,
    val description: String? = null,
    val custom: Map<String, Any>? = null,
    val updated: String? = null,
    val eTag: String? = null,
    val type: String? = null,
    val status: String? = null
)

fun PNChannelMetadata.toSpace(): Space {
    @Suppress("UNCHECKED_CAST")
    return Space(
        id = SpaceId(id),
        name = name,
        description = description,
        custom = custom as Map<String, Any>?,
        updated = updated,
        eTag = eTag,
        type = type,
        status = status
    )
}
