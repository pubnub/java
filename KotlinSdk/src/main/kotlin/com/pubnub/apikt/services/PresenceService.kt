package com.pubnub.apikt.services

import com.google.gson.JsonElement
import com.pubnub.apikt.models.server.Envelope
import com.pubnub.apikt.models.server.presence.WhereNowPayload
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

internal interface PresenceService {

    @GET("v2/presence/sub-key/{subKey}/channel/{channel}/leave")
    fun leave(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>

    @GET("v2/presence/sub-key/{subKey}/channel/{channel}/heartbeat")
    fun heartbeat(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>

    @GET("v2/presence/sub-key/{subKey}/uuid/{uuid}")
    fun whereNow(
        @Path("subKey") subKey: String,
        @Path("uuid") uuid: String,
        @QueryMap options: Map<String, String>
    ): Call<Envelope<WhereNowPayload>>

    @GET("v2/presence/sub_key/{subKey}/channel/{channel}")
    fun hereNow(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @QueryMap options: Map<String, String>
    ): Call<Envelope<JsonElement>>

    @GET("v2/presence/sub_key/{subKey}")
    fun globalHereNow(
        @Path("subKey") subKey: String,
        @QueryMap options: Map<String, String>
    ): Call<Envelope<JsonElement>>

    @GET("v2/presence/sub-key/{subKey}/channel/{channel}/uuid/{uuid}")
    fun getState(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @Path("uuid") uuid: String,
        @QueryMap options: Map<String, String>
    ): Call<Envelope<JsonElement>>

    @GET("v2/presence/sub-key/{subKey}/channel/{channel}/uuid/{uuid}/data")
    fun setState(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @Path("uuid") uuid: String,
        @QueryMap options: Map<String, String>
    ): Call<Envelope<JsonElement>>
}
