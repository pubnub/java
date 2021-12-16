package com.pubnub.api.endpoints.access;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.access_manager.v3.PNRevokeTokenResult;
import com.pubnub.api.models.server.access_manager.v3.RevokeTokenResponse;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class RevokeToken extends Endpoint<RevokeTokenResponse, PNRevokeTokenResult> {

    @Setter
    private String token;

    public RevokeToken(PubNub pubnub,
                       TelemetryManager telemetryManager,
                       RetrofitManager retrofit,
                       TokenManager tokenManager) {
        super(pubnub, telemetryManager, retrofit, tokenManager);
    }

    @Override
    protected List<String> getAffectedChannels() {
        return null;
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return null;
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (this.getPubnub().getConfiguration().getSecretKey() == null || this.getPubnub()
                .getConfiguration()
                .getSecretKey()
                .isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SECRET_KEY_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub()
                .getConfiguration()
                .getSubscribeKey()
                .isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (this.token == null) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_TOKEN_MISSING)
                    .build();
        }
    }

    @Override
    protected Call<RevokeTokenResponse> doWork(Map<String, String> baseParams) throws PubNubException {
        return getRetrofit()
                .getAccessManagerService()
                .revokeToken(getPubnub().getConfiguration().getSubscribeKey(), repairEncoding(token), baseParams);
    }

    @Override
    protected PNRevokeTokenResult createResponse(Response<RevokeTokenResponse> input) throws PubNubException {
        if (input.body() == null) {
            return null;
        }

        return new PNRevokeTokenResult();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNAccessManagerRevokeToken;
    }

    @Override
    protected boolean isAuthRequired() {
        return false;
    }

    private String repairEncoding(String token) throws PubNubException {
        try {
            return URLEncoder.encode(token, "utf-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_PUBNUB_ERROR)
                    .cause(e)
                    .build();
        }
    }
}
