package com.pubnub.api.services;

import com.google.gson.JsonObject;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.server.access_manager.AccessManagerGrantPayload;
import com.pubnub.api.models.server.access_manager.v3.RevokeTokenResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface AccessManagerService {

    @GET("/v2/auth/grant/sub-key/{subKey}")
    Call<Envelope<AccessManagerGrantPayload>> grant(@Path("subKey") String subKey,
                                                    @QueryMap Map<String, String> options);

    @POST("/v3/pam/{subKey}/grant")
    Call<JsonObject> grantToken(@Path("subKey") String subKey,
                                @Body Object body,
                                @QueryMap Map<String, String> options);

    @DELETE("/v3/pam/{subKey}/grant/{token}")
    Call<RevokeTokenResponse> revokeToken(@Path("subKey") String subKey,
                                          @Path(value = "token", encoded = true) String token,
                                          @QueryMap Map<String, String> queryParams);
}
