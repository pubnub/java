package com.pubnub.api.endpoints.objects_api.channel;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.BuilderSteps;
import com.pubnub.api.endpoints.objects_api.ChannelEnpoint;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.utils.Include.CustomIncludeAware;
import com.pubnub.api.endpoints.objects_api.utils.Include.HavingCustomInclude;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadata;
import com.pubnub.api.models.consumer.objects_api.channel.PNSetChannelMetadataResult;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.models.server.objects_api.SetChannelMetadataPayload;
import lombok.AllArgsConstructor;
import retrofit2.Call;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

public abstract class SetChannelMetadata
        extends ChannelEnpoint<EntityEnvelope<PNChannelMetadata>, PNSetChannelMetadataResult>
        implements CustomIncludeAware<SetChannelMetadata> {
    SetChannelMetadata(final String channel,
                       final PubNub pubnubInstance,
                       final TelemetryManager telemetry,
                       final RetrofitManager retrofitInstance,
                       final CompositeParameterEnricher compositeParameterEnricher,
                       final TokenManager tokenManager) {
        super(channel, pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
    }

    public abstract SetChannelMetadata description(String description);

    public abstract SetChannelMetadata name(String name);

    public abstract SetChannelMetadata custom(Map<String, Object> custom);

    public abstract SetChannelMetadata status(String status);

    public abstract SetChannelMetadata type(String status);

    public static Builder builder(final PubNub pubnubInstance,
                                  final TelemetryManager telemetry,
                                  final RetrofitManager retrofitInstance,
                                  final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault();
        return new Builder(pubnubInstance, telemetry, retrofitInstance,
                compositeParameterEnricher, tokenManager);
    }

    @AllArgsConstructor
    public static class Builder implements BuilderSteps.ChannelStep<SetChannelMetadata> {
        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final CompositeParameterEnricher compositeParameterEnricher;
        private final TokenManager tokenManager;

        @Override
        public SetChannelMetadata channel(final String channel) {
            return new SetChannelMetadataCommand(channel, pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher,
                    tokenManager);
        }
    }
}

final class SetChannelMetadataCommand extends SetChannelMetadata implements HavingCustomInclude<SetChannelMetadata> {
    private String name;
    private String description;
    private Object custom;
    private String status;
    private String type;

    SetChannelMetadataCommand(final String channel,
                              final PubNub pubNub,
                              final TelemetryManager telemetryManager,
                              final RetrofitManager retrofitManager,
                              final CompositeParameterEnricher compositeParameterEnricher,
                              final TokenManager tokenManager) {
        super(channel, pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
    }

    @Override
    protected Call<EntityEnvelope<PNChannelMetadata>> executeCommand(final Map<String, String> effectiveParams)
            throws PubNubException {
        final SetChannelMetadataPayload setChannelMetadataPayload = new SetChannelMetadataPayload(name, description,
                custom, status, type);
        return getRetrofit()
                .getChannelMetadataService()
                .setChannelsMetadata(getPubnub().getConfiguration().getSubscribeKey(), channel,
                        setChannelMetadataPayload, effectiveParams);
    }

    @Override
    protected PNSetChannelMetadataResult createResponse(Response<EntityEnvelope<PNChannelMetadata>> input)
            throws PubNubException {
        if (input.body() != null) {
            return new PNSetChannelMetadataResult(input.body());
        } else {
            return new PNSetChannelMetadataResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNSetChannelMetadataOperation;
    }


    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }

    @Override
    public SetChannelMetadata description(final String description) {
        this.description = description;
        return this;
    }

    @Override
    public SetChannelMetadata name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public SetChannelMetadata custom(final Map<String, Object> custom) {
        this.custom = new HashMap<>(custom);
        return this;
    }

    @Override
    public SetChannelMetadata status(String status) {
        this.status = status;
        return this;
    }

    @Override
    public SetChannelMetadata type(String type) {
        this.type = type;
        return this;
    }
}
