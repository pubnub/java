package com.pubnub.apikt.services

import com.pubnub.apikt.models.server.SubscribeEnvelope
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

internal interface SubscribeService {
    @GET("v2/subscribe/{subKey}/{channel}/0")
    fun subscribe(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @QueryMap options: Map<String, String>
    ): Call<SubscribeEnvelope>
}
