package com.pubnub.api.endpoints.objects_api.channel;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.ObjectApiEndpoint;
import com.pubnub.api.endpoints.objects_api.utils.Include.CustomIncludeAware;
import com.pubnub.api.endpoints.objects_api.utils.Include.HavingCustomInclude;
import com.pubnub.api.endpoints.objects_api.utils.ListCapabilities.HavingListCapabilites;
import com.pubnub.api.endpoints.objects_api.utils.ListCapabilities.ListCapabilitiesAware;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadata;
import com.pubnub.api.models.consumer.objects_api.channel.PNGetAllChannelsMetadataResult;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Map;

public abstract class GetAllChannelsMetadata
        extends ObjectApiEndpoint<EntityArrayEnvelope<PNChannelMetadata>, PNGetAllChannelsMetadataResult>
        implements CustomIncludeAware<GetAllChannelsMetadata>, ListCapabilitiesAware<GetAllChannelsMetadata> {
    GetAllChannelsMetadata(final PubNub pubnubInstance,
                           final TelemetryManager telemetry,
                           final RetrofitManager retrofitInstance,
                           final CompositeParameterEnricher compositeParameterEnricher,
                           final TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
    }

    public static GetAllChannelsMetadata create(final PubNub pubnubInstance,
                                                final TelemetryManager telemetry,
                                                final RetrofitManager retrofitInstance,
                                                final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault(true, true);
        return new GetAllChannelsMetadataCommand(pubnubInstance, telemetry, retrofitInstance,
                compositeParameterEnricher, tokenManager);
    }
}

final class GetAllChannelsMetadataCommand extends GetAllChannelsMetadata implements
        HavingCustomInclude<GetAllChannelsMetadata>,
        HavingListCapabilites<GetAllChannelsMetadata> {
    GetAllChannelsMetadataCommand(final PubNub pubnubInstance,
                                  final TelemetryManager telemetry,
                                  final RetrofitManager retrofitInstance,
                                  final CompositeParameterEnricher compositeParameterEnricher,
                                  final TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
    }

    @Override
    protected Call<EntityArrayEnvelope<PNChannelMetadata>> executeCommand(final Map<String, String> effectiveParams)
            throws PubNubException {
        return getRetrofit()
                .getChannelMetadataService()
                .getChannelMetadata(getPubnub().getConfiguration().getSubscribeKey(), effectiveParams);
    }

    @Override
    protected PNGetAllChannelsMetadataResult createResponse(Response<EntityArrayEnvelope<PNChannelMetadata>> input)
            throws PubNubException {
        if (input.body() != null) {
            return new PNGetAllChannelsMetadataResult(input.body());
        } else {
            return new PNGetAllChannelsMetadataResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNGetAllChannelsMetadataOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }
}
