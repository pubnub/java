package com.pubnub.space.models.consumer

import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.models.consumer.pubsub.objects.PNDeleteChannelMetadataEventMessage
import com.pubnub.apikt.models.consumer.pubsub.objects.PNObjectEventResult
import com.pubnub.apikt.models.consumer.pubsub.objects.PNSetChannelMetadataEventMessage

sealed class SpaceEvent {
    abstract val spaceId: SpaceId
    abstract val timetoken: Long
}

data class SpaceModified(
    override val spaceId: SpaceId,
    override val timetoken: Long,
    val data: Data
) : SpaceEvent() {
    data class Data(
        val spaceId: SpaceId,
        val name: String?,
        val description: String?,
        val custom: Map<String, Any>?,
        val status: String?,
        val type: String?
    )
}

data class SpaceRemoved(
    override val spaceId: SpaceId,
    override val timetoken: Long,
    val data: Data
) : SpaceEvent() {
    data class Data(val spaceId: SpaceId)
}

fun PNObjectEventResult.toSpaceEvent(): SpaceEvent? {
    return when (val m = extractedMessage) {
        is PNSetChannelMetadataEventMessage -> {
            @Suppress("UNCHECKED_CAST")
            SpaceModified(
                spaceId = SpaceId(channel), timetoken = timetoken ?: 0,
                data = SpaceModified.Data(
                    spaceId = SpaceId(m.data.id),
                    name = m.data.name,
                    description = m.data.description,
                    custom = m.data.custom as? Map<String, Any>,
                    status = m.data.status,
                    type = m.data.type
                )
            )
        }
        is PNDeleteChannelMetadataEventMessage -> {
            SpaceRemoved(
                spaceId = SpaceId(channel), timetoken = timetoken ?: 0,
                data = SpaceRemoved.Data(
                    spaceId = SpaceId(m.channel)
                )
            )
        }
        else -> {
            return null
        }
    }
}
