package com.pubnub.api.endpoints.publishKT

import retrofit2.Call

interface PublishServiceExternal {
    fun publish(
        pubKey: String,
        subKey: String,
        channel: String,
        message: String,
        options: Map<String, String>
    ): Call<List<Any>>

    fun publishWithPost(
        pubKey: String,
        subKey: String,
        channel: String,
        message: Any,
        options: Map<String, String>
    ): Call<List<Any>>
}
