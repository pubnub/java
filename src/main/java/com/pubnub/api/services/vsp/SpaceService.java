package com.pubnub.api.services.vsp;

import com.google.gson.JsonElement;
import com.pubnub.api.models.consumer.objects_vsp.space.Space;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.models.server.objects_vsp.space.CreateSpacePayload;
import com.pubnub.api.models.server.objects_vsp.space.UpdateSpacePayload;
import com.pubnub.api.models.server.objects_vsp.space.UpsertSpacePayload;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface SpaceService {

    @POST("/v3/objects/{subKey}/spaces/{spaceId}")
    Call<EntityEnvelope<Space>> createSpace(@Path("subKey") String subKey, @Path("spaceId") String spaceId, @Body CreateSpacePayload createSpacePayload, @QueryMap(encoded = true) Map<String, String> options);

    @GET("/v3/objects/{subKey}/spaces/{spaceId}")
    Call<EntityEnvelope<Space>> fetchSpace(@Path("subKey") String subKey, @Path("spaceId") String userId, @QueryMap(encoded = true) Map<String, String> options);

    @DELETE("/v3/objects/{subKey}/spaces/{spaceId}")
    Call<EntityEnvelope<JsonElement>> removeSpace(@Path("subKey") String subKey, @Path("spaceId") String spaceId, @QueryMap(encoded = true) Map<String, String> options);

    @PATCH("/v3/objects/{subKey}/spaces/{spaceId}")
    Call<EntityEnvelope<Space>> updateSpace(@Path("subKey") String subKey, @Path("spaceId") String spaceId, @Body UpdateSpacePayload updateSpacePayload, @QueryMap(encoded = true) Map<String, String> options);

    @PUT("/v3/objects/{subKey}/spaces/{spaceId}")
    Call<EntityEnvelope<Space>> upsertSpace(@Path("subKey") String subKey, @Path("spaceId") String spaceId, @Body UpsertSpacePayload upsertSpacePayload, @QueryMap(encoded = true) Map<String, String> options);
}
