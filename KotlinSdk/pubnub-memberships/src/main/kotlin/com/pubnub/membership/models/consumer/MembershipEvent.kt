package com.pubnub.membership.models.consumer

import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.UserId
import com.pubnub.apikt.models.consumer.pubsub.objects.PNDeleteMembershipEventMessage
import com.pubnub.apikt.models.consumer.pubsub.objects.PNObjectEventResult
import com.pubnub.apikt.models.consumer.pubsub.objects.PNSetMembershipEventMessage
import com.pubnub.space.models.consumer.Space
import com.pubnub.user.models.consumer.User

sealed class MembershipEvent {
    abstract val spaceId: SpaceId
    abstract val timetoken: Long
}

data class MembershipModified(
    override val spaceId: SpaceId,
    override val timetoken: Long,
    val data: Data
) : MembershipEvent() {
    data class Data(
        val space: Space,
        val user: User,
        val custom: Map<String, Any>?,
        val status: String?
    )
}

data class MembershipRemoved(
    override val spaceId: SpaceId,
    override val timetoken: Long,
    val data: Data
) : MembershipEvent() {
    data class Data(
        val space: Space,
        val user: User
    )
}

fun PNObjectEventResult.toMembershipEvent(): MembershipEvent? {
    return when (val m = extractedMessage) {
        is PNSetMembershipEventMessage -> {
            @Suppress("UNCHECKED_CAST")
            MembershipModified(
                data = MembershipModified.Data(
                    space = Space(id = SpaceId(m.data.channel)),
                    user = User(id = UserId(m.data.uuid)),
                    custom = m.data.custom as? Map<String, Any>,
                    status = m.data.status
                ),
                spaceId = SpaceId(channel), timetoken = timetoken ?: 0
            )
        }
        is PNDeleteMembershipEventMessage -> {
            MembershipRemoved(
                data = MembershipRemoved.Data(
                    space = Space(id = SpaceId(m.data.channelId)), user = User(id = UserId(m.data.uuid))
                ),
                spaceId = SpaceId(channel), timetoken = timetoken ?: 0
            )
        }
        else -> {
            return null
        }
    }
}
