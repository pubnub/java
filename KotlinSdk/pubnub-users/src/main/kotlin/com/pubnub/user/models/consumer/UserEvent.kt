package com.pubnub.user.models.consumer

import com.pubnub.apikt.UserId
import com.pubnub.apikt.models.consumer.pubsub.objects.PNDeleteUUIDMetadataEventMessage
import com.pubnub.apikt.models.consumer.pubsub.objects.PNObjectEventResult
import com.pubnub.apikt.models.consumer.pubsub.objects.PNSetUUIDMetadataEventMessage

sealed class UserEvent {
    abstract val spaceId: String
    abstract val timetoken: Long
}

data class UserModified(
    override val spaceId: String,
    override val timetoken: Long,
    val data: Data
) : UserEvent() {

    data class Data(
        val userId: UserId,
        val name: String?,
        val externalId: String?,
        val profileUrl: String?,
        val email: String?,
        val custom: Map<String, Any>?,
        val status: String?,
        val type: String?
    )
}

data class UserRemoved(
    override val spaceId: String,
    override val timetoken: Long,
    val data: Data
) : UserEvent() {
    data class Data(val userId: UserId)
}

fun PNObjectEventResult.toUserEvent(): UserEvent? {
    return when (val m = extractedMessage) {
        is PNSetUUIDMetadataEventMessage -> {
            @Suppress("UNCHECKED_CAST")
            UserModified(
                spaceId = channel,
                timetoken = timetoken ?: 0,
                data = UserModified.Data(
                    userId = UserId(m.data.id),
                    name = m.data.name,
                    profileUrl = m.data.profileUrl,
                    email = m.data.email,
                    status = m.data.status,
                    type = m.data.type,
                    custom = m.data.custom as? Map<String, Any>,
                    externalId = m.data.externalId
                )
            )
        }
        is PNDeleteUUIDMetadataEventMessage -> {
            UserRemoved(
                spaceId = channel, timetoken = timetoken ?: 0,
                data = UserRemoved.Data(
                    userId = UserId(m.uuid)
                )
            )
        }
        else -> {
            return null
        }
    }
}
