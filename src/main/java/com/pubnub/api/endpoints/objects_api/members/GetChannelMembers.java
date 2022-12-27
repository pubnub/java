package com.pubnub.api.endpoints.objects_api.members;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.BuilderSteps;
import com.pubnub.api.endpoints.objects_api.ChannelEnpoint;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.utils.Include.CustomIncludeAware;
import com.pubnub.api.endpoints.objects_api.utils.Include.HavingCustomInclude;
import com.pubnub.api.endpoints.objects_api.utils.Include.HavingUUIDInclude;
import com.pubnub.api.endpoints.objects_api.utils.Include.UUIDIncludeAware;
import com.pubnub.api.endpoints.objects_api.utils.ListCapabilities.HavingListCapabilites;
import com.pubnub.api.endpoints.objects_api.utils.ListCapabilities.ListCapabilitiesAware;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.member.PNGetChannelMembersResult;
import com.pubnub.api.models.consumer.objects_api.member.PNMembers;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import lombok.AllArgsConstructor;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Map;

public abstract class GetChannelMembers extends ChannelEnpoint<EntityArrayEnvelope<PNMembers>, PNGetChannelMembersResult>
        implements CustomIncludeAware<GetChannelMembers>, UUIDIncludeAware<GetChannelMembers>, ListCapabilitiesAware<GetChannelMembers> {
    GetChannelMembers(final String channel,
                      final PubNub pubnubInstance,
                      final TelemetryManager telemetry,
                      final RetrofitManager retrofitInstance,
                      final CompositeParameterEnricher compositeParameterEnricher,
                      final TokenManager tokenManager) {
        super(channel, pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
    }

    public static Builder builder(final PubNub pubnubInstance,
                                  final TelemetryManager telemetry,
                                  final RetrofitManager retrofitInstance,
                                  final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault(true, false);
        return new Builder(pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
    }

    @AllArgsConstructor
    public static class Builder implements BuilderSteps.ChannelStep<GetChannelMembers> {
        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final CompositeParameterEnricher compositeParameterEnricher;
        private final TokenManager tokenManager;

        @Override
        public GetChannelMembers channel(final String channel) {
            return new GetChannelMembersCommand(channel, pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
        }
    }
}

final class GetChannelMembersCommand extends GetChannelMembers implements
        HavingCustomInclude<GetChannelMembers>,
        HavingUUIDInclude<GetChannelMembers>,
        HavingListCapabilites<GetChannelMembers> {

    GetChannelMembersCommand(final String channel,
                             final PubNub pubNub,
                             final TelemetryManager telemetryManager,
                             final RetrofitManager retrofitManager,
                             final CompositeParameterEnricher compositeParameterEnricher,
                             final TokenManager tokenManager) {
        super(channel, pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
    }

    @Override
    protected Call<EntityArrayEnvelope<PNMembers>> executeCommand(final Map<String, String> effectiveParams)
            throws PubNubException {
        return getRetrofit()
                .getChannelMetadataService()
                .getMembers(getPubnub().getConfiguration().getSubscribeKey(), channel, effectiveParams);
    }

    @Override
    protected PNGetChannelMembersResult createResponse(Response<EntityArrayEnvelope<PNMembers>> input) throws PubNubException {
        if (input.body() != null) {
            return new PNGetChannelMembersResult(input.body());
        } else {
            return new PNGetChannelMembersResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNGetChannelMembersOperation;
    }


    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }
}
