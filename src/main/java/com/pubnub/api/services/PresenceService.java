package com.pubnub.api.services;

import com.pubnub.api.models.server.Envelope;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface PresenceService {

    @GET("v2/presence/sub-key/{subKey}/channel/{channel}/leave")
    Call<Envelope> leave(@Path("subKey") String subKey,
                         @Path("channel") String channel,
                         @QueryMap Map<String, String> options);

    @GET("v2/presence/sub-key/{subKey}/channel/{channel}/heartbeat")
    Call<Envelope> heartbeat(@Path("subKey") String subKey,
                             @Path("channel") String channel,
                             @QueryMap(encoded = true) Map<String, String> options);
}
