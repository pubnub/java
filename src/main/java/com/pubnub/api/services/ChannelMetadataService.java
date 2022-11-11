package com.pubnub.api.services;

import com.google.gson.JsonElement;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadata;
import com.pubnub.api.models.server.objects_api.SetChannelMetadataPayload;
import com.pubnub.api.models.consumer.objects_api.member.PNMembers;
import com.pubnub.api.models.server.objects_api.PatchMemberPayload;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ChannelMetadataService {


    @GET("v2/objects/{subKey}/channels")
    Call<EntityArrayEnvelope<PNChannelMetadata>> getChannelMetadata(@Path("subKey") String subKey, @QueryMap(encoded = true) Map<String, String> options);

    @GET("v2/objects/{subKey}/channels/{channel}")
    Call<EntityEnvelope<PNChannelMetadata>> getChannelMetadata(@Path("subKey") String subKey, @Path("channel") String channel, @QueryMap(encoded = true) Map<String, String> options);

    @PATCH("/v2/objects/{subKey}/channels/{channel}")
    Call<EntityEnvelope<PNChannelMetadata>> setChannelsMetadata(@Path("subKey") String subKey, @Path("channel") String channel, @Body SetChannelMetadataPayload setChannelMetadataPayload, @QueryMap(encoded = true) Map<String, String> options);

    @DELETE("/v2/objects/{subKey}/channels/{channel}")
    Call<EntityEnvelope<JsonElement>> deleteChannelMetadata(@Path("subKey") String subKey, @Path("channel") String channel, @QueryMap(encoded = true) Map<String, String> options);

    //FIXME if you find we need a separate service for this
    @GET("v2/objects/{subKey}/channels/{channel}/uuids")
    Call<EntityArrayEnvelope<PNMembers>> getMembers(@Path("subKey") String subKey, @Path("channel") String channel, @QueryMap(encoded = true) Map<String, String> options);

    @PATCH("v2/objects/{subKey}/channels/{channel}/uuids")
    Call<EntityArrayEnvelope<PNMembers>> patchMembers(@Path("subKey") String subKey, @Path("channel") String channel, @Body PatchMemberPayload patchMemberPayload, @QueryMap(encoded = true) Map<String, String> options);
}
