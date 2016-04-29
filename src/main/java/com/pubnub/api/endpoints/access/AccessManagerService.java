package com.pubnub.api.endpoints.access;

import com.pubnub.api.models.server.access_manager.AccessManagerGrantPayload;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.server.access_manager.AccessManagerAuditPayload;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

import java.util.Map;

public interface AccessManagerService {

    @GET("/v1/auth/grant/sub-key/{subKey}")
    Call<Envelope<AccessManagerGrantPayload>> grant(@Path("subKey") String subKey, @QueryMap Map<String, String> options);

    @GET("/v1/auth/audit/sub-key/{subKey}")
    Call<Envelope<AccessManagerAuditPayload>> audit(@Path("subKey") String subKey, @QueryMap Map<String, String> options);

}
