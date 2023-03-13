package com.pubnub.membership.models.consumer

import com.pubnub.apikt.models.consumer.objects.member.PNUUIDDetailsLevel

enum class UserDetailsLevel {
    USER,
    USER_WITH_CUSTOM
}

internal fun UserDetailsLevel.toPNUUIDDetailsLevel(): PNUUIDDetailsLevel {
    return when (this) {
        UserDetailsLevel.USER -> PNUUIDDetailsLevel.UUID
        UserDetailsLevel.USER_WITH_CUSTOM -> PNUUIDDetailsLevel.UUID_WITH_CUSTOM
    }
}
