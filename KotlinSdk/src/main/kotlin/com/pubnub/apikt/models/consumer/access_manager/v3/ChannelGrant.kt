package com.pubnub.apikt.models.consumer.access_manager.v3

interface ChannelGrant : PNGrant {
    companion object {
        fun name(
            name: String,
            read: Boolean = false,
            write: Boolean = false,
            manage: Boolean = false,
            delete: Boolean = false,
            create: Boolean = false,
            get: Boolean = false,
            join: Boolean = false,
            update: Boolean = false
        ): ChannelGrant = PNChannelResourceGrant(
            id = name,
            read = read,
            write = write,
            manage = manage,
            delete = delete,
            create = create,
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
            create: Boolean = false,
            get: Boolean = false,
            join: Boolean = false,
            update: Boolean = false
        ): ChannelGrant = PNChannelPatternGrant(
            id = pattern,
            read = read,
            write = write,
            manage = manage,
            delete = delete,
            create = create,
            get = get,
            join = join,
            update = update
        )
    }
}
