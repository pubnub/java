package com.pubnub.apikt.models.server

import com.google.gson.JsonElement

class PresenceEnvelope(
    internal val action: String? = null,
    internal val uuid: String? = null,
    internal val occupancy: Int? = null,
    internal val timestamp: Long? = null,
    internal val data: JsonElement? = null
)
