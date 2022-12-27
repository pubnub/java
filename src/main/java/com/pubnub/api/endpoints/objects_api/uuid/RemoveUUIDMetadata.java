package com.pubnub.api.endpoints.objects_api.uuid;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.UUIDEndpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.uuid.PNRemoveUUIDMetadataResult;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collections;
import java.util.Map;

public class RemoveUUIDMetadata extends UUIDEndpoint<RemoveUUIDMetadata, EntityEnvelope<JsonElement>, PNRemoveUUIDMetadataResult> {

    public RemoveUUIDMetadata(final PubNub pubnubInstance,
                              final TelemetryManager telemetry,
                              final RetrofitManager retrofitInstance,
                              TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, CompositeParameterEnricher.createDefault(true, true), tokenManager);
    }

    @Override
    protected Call<EntityEnvelope<JsonElement>> executeCommand(final Map<String, String> effectiveParams)
            throws PubNubException {
        return getRetrofit()
                .getUuidMetadataService()
                .deleteUUIDMetadata(getPubnub().getConfiguration().getSubscribeKey(), effectiveUuid(), Collections.emptyMap());
    }

    @Override
    protected PNRemoveUUIDMetadataResult createResponse(Response<EntityEnvelope<JsonElement>> input)
            throws PubNubException {
        return new PNRemoveUUIDMetadataResult(input.body());
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNRemoveUuidMetadataOperation;
    }
}
