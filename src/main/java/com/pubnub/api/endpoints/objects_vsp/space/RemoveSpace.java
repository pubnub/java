package com.pubnub.api.endpoints.objects_vsp.space;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.ObjectApiEndpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.space.RemoveSpaceResult;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collections;
import java.util.Map;

public class RemoveSpace extends ObjectApiEndpoint<EntityEnvelope<JsonElement>, RemoveSpaceResult> {
    private SpaceId spaceId;

    public RemoveSpace(
            final SpaceId spaceId,
            final PubNub pubNub,
            final TelemetryManager telemetry,
            final RetrofitManager retrofitInstance,
            final TokenManager tokenManager) {
        super(pubNub, telemetry, retrofitInstance, CompositeParameterEnricher.createDefault(), tokenManager);
        this.spaceId = spaceId;
    }

    @Override
    protected Call<EntityEnvelope<JsonElement>> executeCommand(Map<String, String> effectiveParams) throws PubNubException {
        return getRetrofit()
                .getSpaceService()
                .removeSpace(getPubnub().getConfiguration().getSubscribeKey(), spaceId.getValue(), Collections.emptyMap());
    }

    @Override
    protected RemoveSpaceResult createResponse(Response<EntityEnvelope<JsonElement>> input) throws PubNubException {
        int status = input.body().getStatus();
        return new RemoveSpaceResult(status);
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNRemoveUserOperation;
    }

}
