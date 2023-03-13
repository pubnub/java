package com.pubnub.apikt.models.consumer.access_manager.v3

interface UUIDGrant : PNGrant {
    companion object {
        fun id(
            id: String,
            get: Boolean = false,
            update: Boolean = false,
            delete: Boolean = false
        ): UUIDGrant = PNUUIDResourceGrant(
            id = id,
            delete = delete,
            get = get,
            update = update
        )

        fun pattern(
            pattern: String,
            get: Boolean = false,
            update: Boolean = false,
            delete: Boolean = false
        ): UUIDGrant = PNUUIDPatternGrant(
            id = pattern,
            delete = delete,
            get = get,
            update = update
        )
    }
}
