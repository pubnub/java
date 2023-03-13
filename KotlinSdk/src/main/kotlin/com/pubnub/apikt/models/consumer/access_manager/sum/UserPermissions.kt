package com.pubnub.apikt.models.consumer.access_manager.sum

import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.UserId
import com.pubnub.apikt.models.consumer.access_manager.v3.*

interface UserPermissions : PNGrant {
    companion object {
        fun id(
            userId: UserId,
            get: Boolean = false,
            update: Boolean = false,
            delete: Boolean = false
        ): UserPermissions = PNUserPermissionsGrant(
            id = userId.value,
            delete = delete,
            get = get,
            update = update
        )

        fun pattern(
            pattern: String,
            get: Boolean = false,
            update: Boolean = false,
            delete: Boolean = false
        ): UserPermissions = PNUserPatternPermissionsGrant(
            id = pattern,
            delete = delete,
            get = get,
            update = update
        )
    }
}

fun UserPermissions.toUuidGrant(): UUIDGrant {
    return when (this) {
        is PNUserPermissionsGrant -> PNUUIDResourceGrant(userPermissions = this)
        is PNUserPatternPermissionsGrant -> PNUUIDPatternGrant(userPermissions = this)
        else -> throw PubNubException(pubnubError = PubNubError.INVALID_ARGUMENTS)
    }
}
