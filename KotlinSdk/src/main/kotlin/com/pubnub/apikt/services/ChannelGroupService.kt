package com.pubnub.apikt.services

import com.pubnub.apikt.models.server.Envelope
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

internal interface ChannelGroupService {

    @GET("v1/channel-registration/sub-key/{subKey}/channel-group")
    fun listAllChannelGroup(
        @Path("subKey") subKey: String,
        @QueryMap options: Map<String, String>
    ): Call<Envelope<Map<String, Any>>>

    @GET("v1/channel-registration/sub-key/{subKey}/channel-group/{group}")
    fun allChannelsChannelGroup(
        @Path("subKey") subKey: String,
        @Path("group") group: String,
        @QueryMap options: Map<String, String>
    ): Call<Envelope<Map<String, Any>>>

    @GET("v1/channel-registration/sub-key/{subKey}/channel-group/{group}")
    fun addChannelChannelGroup(
        @Path("subKey") subKey: String,
        @Path("group") group: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>

    @GET("v1/channel-registration/sub-key/{subKey}/channel-group/{group}")
    fun removeChannel(
        @Path("subKey") subKey: String,
        @Path("group") group: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>

    @GET("v1/channel-registration/sub-key/{subKey}/channel-group/{group}/remove")
    fun deleteChannelGroup(
        @Path("subKey") subKey: String,
        @Path("group") group: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>
}
