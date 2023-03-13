package com.pubnub.apikt.models.consumer.objects.member

import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata

data class PNMember(
    val uuid: PNUUIDMetadata?,
    val custom: Any? = null,
    val updated: String,
    val eTag: String,
    val status: String?
) {
    data class Partial(
        val uuidId: String,
        override val custom: Any? = null,
        override val status: String? = null,

    ) : MemberInput {
        override val uuid: String = uuidId
    }
}
