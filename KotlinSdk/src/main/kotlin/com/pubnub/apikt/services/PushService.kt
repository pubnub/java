package com.pubnub.apikt.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

internal interface PushService {

    @GET("v1/push/sub-key/{subKey}/devices/{pushToken}")
    fun modifyChannelsForDevice(
        @Path("subKey") subKey: String,
        @Path("pushToken") pushToken: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>

    @GET("v1/push/sub-key/{subKey}/devices/{pushToken}/remove")
    fun removeAllChannelsForDevice(
        @Path("subKey") subKey: String,
        @Path("pushToken") pushToken: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>

    @GET("v1/push/sub-key/{subKey}/devices/{pushToken}")
    fun listChannelsForDevice(
        @Path("subKey") subKey: String,
        @Path("pushToken") pushToken: String,
        @QueryMap options: Map<String, String>
    ): Call<List<String>>

    // V2 (APNS2)

    // V2 (APNS2)
    @GET("v2/push/sub-key/{subKey}/devices-apns2/{deviceApns2}")
    fun modifyChannelsForDeviceApns2(
        @Path("subKey") subKey: String,
        @Path("deviceApns2") deviceApns2: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>

    @GET("v2/push/sub-key/{subKey}/devices-apns2/{deviceApns2}")
    fun listChannelsForDeviceApns2(
        @Path("subKey") subKey: String,
        @Path("deviceApns2") deviceApns2: String,
        @QueryMap options: Map<String, String>
    ): Call<List<String>>

    @GET("v2/push/sub-key/{subKey}/devices-apns2/{deviceApns2}/remove")
    fun removeAllChannelsForDeviceApns2(
        @Path("subKey") subKey: String,
        @Path("deviceApns2") deviceApns2: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>
}
