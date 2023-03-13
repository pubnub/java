package com.pubnub.apikt.models.server

import com.google.gson.JsonElement

class Envelope<T> {
    internal val status: Int = 0
    internal val message: String? = null
    internal val service: String? = null
    internal val payload: T? = null
    internal val occupancy: Int = 0
    internal val uuids: JsonElement? = null
    internal val action: String? = null
    internal val error: Boolean = false
}
