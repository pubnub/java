package com.pubnub.api.services;

import com.google.gson.JsonElement;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.server.presence.WhereNowPayload;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface ExtendedPresenceService {

    @GET("v2/presence/sub-key/{subKey}/uuid/{uuid}")
    Call<Envelope<WhereNowPayload>> whereNow(@Path("subKey") String subKey,
                                             @Path("uuid") String uuid,
                                             @QueryMap Map<String, String> options);

    @GET("v2/presence/sub_key/{subKey}")
    Call<Envelope<JsonElement>> globalHereNow(@Path("subKey") String subKey,
                                              @QueryMap Map<String, String> options);

    @GET("v2/presence/sub_key/{subKey}/channel/{channel}")
    Call<Envelope<JsonElement>> hereNow(@Path("subKey") String subKey,
                                        @Path("channel") String channel,
                                        @QueryMap Map<String, String> options);

    @GET("v2/presence/sub-key/{subKey}/channel/{channel}/uuid/{uuid}")
    Call<Envelope<JsonElement>> getState(@Path("subKey") String subKey,
                                         @Path("channel") String channel,
                                         @Path("uuid") String uuid,
                                         @QueryMap Map<String, String> options);

    @GET("v2/presence/sub-key/{subKey}/channel/{channel}/uuid/{uuid}/data")
    Call<Envelope<JsonElement>> setState(@Path("subKey") String subKey,
                                         @Path("channel") String channel,
                                         @Path("uuid") String uuid,
                                         @QueryMap(encoded = true) Map<String, String> options);

}
