package com.pubnub.apikt.services

import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadata
import com.pubnub.apikt.models.consumer.objects.member.PNMember
import com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembership
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata
import com.pubnub.apikt.models.server.objects_api.*
import retrofit2.Call
import retrofit2.http.*

internal interface ObjectsService {
    @GET("v2/objects/{subKey}/uuids")
    fun getAllUUIDMetadata(
        @Path("subKey") subKey: String,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityArrayEnvelope<PNUUIDMetadata>>

    @GET("v2/objects/{subKey}/uuids/{uuid}")
    fun getUUIDMetadata(
        @Path("subKey") subKey: String,
        @Path("uuid") uuid: String,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityEnvelope<PNUUIDMetadata>>

    @PATCH("v2/objects/{subKey}/uuids/{uuid}")
    @Headers("Content-Type: application/json; charset=UTF-8")
    fun setUUIDMetadata(
        @Path("subKey") subKey: String,
        @Path("uuid") uuid: String,
        @Body body: UUIDMetadataInput,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityEnvelope<PNUUIDMetadata>>

    @DELETE("v2/objects/{subKey}/uuids/{uuid}")
    fun deleteUUIDMetadata(
        @Path("subKey") subKey: String,
        @Path("uuid") uuid: String
    ): Call<EntityEnvelope<Any?>>

    @GET("v2/objects/{subKey}/channels")
    fun getAllChannelMetadata(
        @Path("subKey") subKey: String,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityArrayEnvelope<PNChannelMetadata>>

    @GET("v2/objects/{subKey}/channels/{channel}")
    fun getChannelMetadata(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityEnvelope<PNChannelMetadata>>

    @PATCH("v2/objects/{subKey}/channels/{channel}")
    @Headers("Content-Type: application/json; charset=UTF-8")
    fun setChannelMetadata(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @Body body: ChannelMetadataInput,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityEnvelope<PNChannelMetadata>>

    @DELETE("v2/objects/{subKey}/channels/{channel}")
    fun deleteChannelMetadata(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String
    ): Call<EntityEnvelope<Any?>>

    @GET("/v2/objects/{subKey}/uuids/{uuid}/channels")
    fun getMemberships(
        @Path("subKey") subKey: String,
        @Path("uuid") uuid: String,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityArrayEnvelope<PNChannelMembership>>

    @PATCH("/v2/objects/{subKey}/uuids/{uuid}/channels")
    @Headers("Content-Type: application/json; charset=UTF-8")
    fun patchMemberships(
        @Path("subKey") subKey: String,
        @Path("uuid") uuid: String,
        @Body body: ChangeMembershipInput,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityArrayEnvelope<PNChannelMembership>>

    @GET("/v2/objects/{subKey}/channels/{channel}/uuids")
    fun getChannelMembers(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityArrayEnvelope<PNMember>>

    @PATCH("/v2/objects/{subKey}/channels/{channel}/uuids")
    @Headers("Content-Type: application/json; charset=UTF-8")
    fun patchChannelMembers(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @Body body: ChangeMemberInput,
        @QueryMap(encoded = true) options: Map<String, String> = mapOf()
    ): Call<EntityArrayEnvelope<PNMember>>
}
