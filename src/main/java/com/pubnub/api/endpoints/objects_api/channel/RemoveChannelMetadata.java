package com.pubnub.api.endpoints.objects_api.channel;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.BuilderSteps;
import com.pubnub.api.endpoints.objects_api.ChannelEnpoint;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.channel.PNRemoveChannelMetadataResult;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import lombok.AllArgsConstructor;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Map;

public class RemoveChannelMetadata extends ChannelEnpoint<EntityEnvelope<JsonElement>, PNRemoveChannelMetadataResult> {
    RemoveChannelMetadata(final String channel,
                          final PubNub pubnubInstance,
                          final TelemetryManager telemetry,
                          final RetrofitManager retrofitInstance,
                          final TokenManager tokenManager) {
        super(channel, pubnubInstance, telemetry, retrofitInstance, CompositeParameterEnricher.createDefault(true, true),
                tokenManager);
    }

    @Override
    protected Call<EntityEnvelope<JsonElement>> executeCommand(Map<String, String> effectiveParams)
            throws PubNubException {
        return getRetrofit()
                .getChannelMetadataService()
                .deleteChannelMetadata(getPubnub().getConfiguration().getSubscribeKey(), channel, effectiveParams);
    }

    @Override
    protected PNRemoveChannelMetadataResult createResponse(Response<EntityEnvelope<JsonElement>> input)
            throws PubNubException {
        return new PNRemoveChannelMetadataResult(input.body());
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNRemoveChannelMetadataOperation;
    }

    public static Builder builder(final PubNub pubnubInstance,
                                  final TelemetryManager telemetry,
                                  final RetrofitManager retrofitInstance,
                                  final TokenManager tokenManager) {
        return new Builder(pubnubInstance, telemetry, retrofitInstance, tokenManager);
    }

    @AllArgsConstructor
    public static class Builder implements BuilderSteps.ChannelStep<RemoveChannelMetadata> {
        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final TokenManager tokenManager;

        @Override
        public RemoveChannelMetadata channel(final String channel) {
            return new RemoveChannelMetadata(channel, pubnubInstance, telemetry, retrofitInstance, tokenManager);
        }
    }

}
