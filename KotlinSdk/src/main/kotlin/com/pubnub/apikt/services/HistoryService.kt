package com.pubnub.apikt.services

import com.google.gson.JsonElement
import com.pubnub.apikt.models.server.FetchMessagesEnvelope
import retrofit2.Call
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

internal interface HistoryService {

    @GET("v2/history/sub-key/{subKey}/channel/{channel}")
    fun fetchHistory(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @QueryMap options: Map<String, String>
    ): Call<JsonElement>

    @DELETE("v3/history/sub-key/{subKey}/channel/{channels}")
    fun deleteMessages(
        @Path("subKey") subKey: String,
        @Path("channels") channels: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>

    @GET("v3/history/sub-key/{subKey}/channel/{channels}")
    fun fetchMessages(
        @Path("subKey") subKey: String,
        @Path("channels") channels: String,
        @QueryMap options: Map<String, String>
    ): Call<FetchMessagesEnvelope>

    @GET("v3/history-with-actions/sub-key/{subKey}/channel/{channel}")
    fun fetchMessagesWithActions(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @QueryMap options: Map<String, String>
    ): Call<FetchMessagesEnvelope>

    @GET("v3/history/sub-key/{subKey}/message-counts/{channels}")
    fun fetchCount(
        @Path("subKey") subKey: String,
        @Path("channels") channels: String,
        @QueryMap options: Map<String, String>
    ): Call<JsonElement>
}
