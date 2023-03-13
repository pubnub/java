package com.pubnub.apikt.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

internal interface SignalService {

    @GET("/signal/{pubKey}/{subKey}/0/{channel}/0/{payload}")
    fun signal(
        @Path("pubKey") pubKey: String,
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @Path(value = "payload") message: String,
        @QueryMap options: Map<String, String>
    ): Call<List<Any>>
}
