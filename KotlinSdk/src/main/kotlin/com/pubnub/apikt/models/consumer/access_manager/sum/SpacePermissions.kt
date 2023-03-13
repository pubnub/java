package com.pubnub.apikt.models.consumer.access_manager.sum

import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.models.consumer.access_manager.v3.*

interface SpacePermissions : PNGrant {

    companion object {
        fun id(
            spaceId: SpaceId,
            read: Boolean = false,
            write: Boolean = false,
            manage: Boolean = false,
            delete: Boolean = false,
            get: Boolean = false,
            join: Boolean = false,
            update: Boolean = false
        ): SpacePermissions = PNSpacePermissionsGrant(
            id = spaceId.value,
            read = read,
            write = write,
            manage = manage,
            delete = delete,
            get = get,
            join = join,
            update = update
        )

        fun pattern(
            pattern: String,
            read: Boolean = false,
            write: Boolean = false,
            manage: Boolean = false,
            delete: Boolean = false,
            get: Boolean = false,
            join: Boolean = false,
            update: Boolean = false
        ): SpacePermissions = PNSpacePatternPermissionsGrant(
            id = pattern,
            read = read,
            write = write,
            manage = manage,
            delete = delete,
            get = get,
            join = join,
            update = update
        )
    }
}

fun SpacePermissions.toChannelGrant(): ChannelGrant {
    return when (this) {
        is PNSpacePermissionsGrant -> PNChannelResourceGrant(spacePermissions = this)
        is PNSpacePatternPermissionsGrant -> PNChannelPatternGrant(spacePermissions = this)
        else -> throw PubNubException(pubnubError = PubNubError.INVALID_ARGUMENTS)
    }
}
