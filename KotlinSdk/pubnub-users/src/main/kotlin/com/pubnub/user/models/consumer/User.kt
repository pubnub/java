package com.pubnub.user.models.consumer

import com.pubnub.apikt.UserId
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata

data class User(
    val id: UserId,
    val name: String? = null,
    val externalId: String? = null,
    val profileUrl: String? = null,
    val email: String? = null,
    val custom: Map<String, Any>? = null,
    val updated: String? = null,
    val eTag: String? = null,
    val type: String? = null,
    val status: String? = null
)

fun PNUUIDMetadata.toUser(): User {
    @Suppress("UNCHECKED_CAST")
    return User(
        id = UserId(id),
        name = name,
        externalId = externalId,
        profileUrl = profileUrl,
        email = email,
        custom = custom as Map<String, Any>?,
        updated = updated,
        eTag = eTag,
        type = type,
        status = status
    )
}
