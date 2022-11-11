package com.pubnub.api.services;

import com.google.gson.JsonElement;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembership;
import com.pubnub.api.models.server.objects_api.PatchMembershipPayload;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata;
import com.pubnub.api.models.server.objects_api.SetUUIDMetadataPayload;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;


public interface UUIDMetadataService {


    @GET("v2/objects/{subKey}/uuids")
    Call<EntityArrayEnvelope<PNUUIDMetadata>> getUUIDMetadata(@Path("subKey") String subKey, @QueryMap(encoded = true) Map<String, String> options);

    @GET("v2/objects/{subKey}/uuids/{uuid}")
    Call<EntityEnvelope<PNUUIDMetadata>> getUUIDMetadata(@Path("subKey") String subKey, @Path("uuid") String uuid, @QueryMap(encoded = true) Map<String, String> options);

    @PATCH("/v2/objects/{subKey}/uuids/{uuid}")
    Call<EntityEnvelope<PNUUIDMetadata>> setUUIDsMetadata(@Path("subKey") String subKey, @Path("uuid") String uuid, @Body SetUUIDMetadataPayload setUUIDMetadataPayload, @QueryMap(encoded = true) Map<String, String> options);

    @DELETE("/v2/objects/{subKey}/uuids/{uuid}")
    Call<EntityEnvelope<JsonElement>> deleteUUIDMetadata(@Path("subKey") String subKey, @Path("uuid") String uuid, @QueryMap(encoded = true) Map<String, String> options);

    //FIXME if you find we need a separate service for this
    @GET("v2/objects/{subKey}/uuids/{uuid}/channels")
    Call<EntityArrayEnvelope<PNMembership>> getMemberships(@Path("subKey") String subKey, @Path("uuid") String uuid, @QueryMap(encoded = true) Map<String, String> options);

    @PATCH("v2/objects/{subKey}/uuids/{uuid}/channels")
    Call<EntityArrayEnvelope<PNMembership>> patchMembership(@Path("subKey") String subKey, @Path("uuid") String uuid, @Body PatchMembershipPayload patchMembershipPayload, @QueryMap(encoded = true) Map<String, String> options);
}
