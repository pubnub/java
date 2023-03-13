package com.pubnub.apikt.models.consumer.access_manager.v3

import com.pubnub.apikt.models.consumer.access_manager.sum.SpacePermissions
import com.pubnub.apikt.models.consumer.access_manager.sum.UserPermissions

interface PNGrant {
    val read: Boolean
    val write: Boolean
    val manage: Boolean
    val delete: Boolean
    val create: Boolean
    val get: Boolean
    val join: Boolean
    val update: Boolean
    val id: String
}

internal sealed class PNAbstractGrant(
    override val read: Boolean = false,
    override val write: Boolean = false,
    override val manage: Boolean = false,
    override val delete: Boolean = false,
    override val create: Boolean = false,
    override val get: Boolean = false,
    override val join: Boolean = false,
    override val update: Boolean = false
) : PNGrant

internal sealed class PNResourceGrant : PNAbstractGrant()

internal sealed class PNPatternGrant : PNAbstractGrant()

internal data class PNChannelResourceGrant(
    override val id: String,
    override val read: Boolean = false,
    override val write: Boolean = false,
    override val manage: Boolean = false,
    override val delete: Boolean = false,
    override val create: Boolean = false,
    override val get: Boolean = false,
    override val join: Boolean = false,
    override val update: Boolean = false
) : PNResourceGrant(), ChannelGrant {
    constructor(spacePermissions: SpacePermissions) : this(
        id = spacePermissions.id,
        read = spacePermissions.read,
        write = spacePermissions.write,
        manage = spacePermissions.manage,
        delete = spacePermissions.delete,
        create = false,
        get = spacePermissions.get,
        join = spacePermissions.join,
        update = spacePermissions.update
    )
}

internal data class PNSpacePermissionsGrant(
    override val id: String,
    override val read: Boolean = false,
    override val write: Boolean = false,
    override val manage: Boolean = false,
    override val delete: Boolean = false,
    override val get: Boolean = false,
    override val join: Boolean = false,
    override val update: Boolean = false
) : PNResourceGrant(), SpacePermissions

internal data class PNChannelPatternGrant(
    override val id: String,
    override val read: Boolean = false,
    override val write: Boolean = false,
    override val manage: Boolean = false,
    override val delete: Boolean = false,
    override val create: Boolean = false,
    override val get: Boolean = false,
    override val join: Boolean = false,
    override val update: Boolean = false
) : PNPatternGrant(), ChannelGrant {
    constructor(spacePermissions: SpacePermissions) : this(
        id = spacePermissions.id,
        read = spacePermissions.read,
        write = spacePermissions.write,
        manage = spacePermissions.manage,
        delete = spacePermissions.delete,
        create = false,
        get = spacePermissions.get,
        join = spacePermissions.join,
        update = spacePermissions.update
    )
}

internal data class PNSpacePatternPermissionsGrant(
    override val id: String,
    override val read: Boolean = false,
    override val write: Boolean = false,
    override val manage: Boolean = false,
    override val delete: Boolean = false,
    override val get: Boolean = false,
    override val join: Boolean = false,
    override val update: Boolean = false
) : PNPatternGrant(), SpacePermissions

internal data class PNChannelGroupResourceGrant(
    override val id: String,
    override val read: Boolean = false,
    override val manage: Boolean = false
) : PNResourceGrant(), ChannelGroupGrant

internal data class PNChannelGroupPatternGrant(
    override val id: String,
    override val read: Boolean = false,
    override val manage: Boolean = false
) : PNPatternGrant(), ChannelGroupGrant

internal data class PNUUIDResourceGrant(
    override val id: String,
    override val get: Boolean = false,
    override val update: Boolean = false,
    override val delete: Boolean = false
) : PNResourceGrant(), UUIDGrant {
    constructor(userPermissions: UserPermissions) : this(
        id = userPermissions.id,
        get = userPermissions.get,
        update = userPermissions.update,
        delete = userPermissions.delete
    )
}

internal data class PNUserPermissionsGrant(
    override val id: String,
    override val get: Boolean = false,
    override val update: Boolean = false,
    override val delete: Boolean = false
) : PNResourceGrant(), UserPermissions

internal data class PNUUIDPatternGrant(
    override val id: String,
    override val get: Boolean = false,
    override val update: Boolean = false,
    override val delete: Boolean = false
) : PNPatternGrant(), UUIDGrant {
    constructor(userPermissions: UserPermissions) : this(
        id = userPermissions.id,
        get = userPermissions.get,
        update = userPermissions.update,
        delete = userPermissions.delete
    )
}

internal data class PNUserPatternPermissionsGrant(
    override val id: String,
    override val get: Boolean = false,
    override val update: Boolean = false,
    override val delete: Boolean = false
) : PNPatternGrant(), UserPermissions
