package com.pubnub.api.endpoints.objects_api.memberships;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.UUIDEndpoint;
import com.pubnub.api.endpoints.objects_api.utils.Include.ChannelIncludeAware;
import com.pubnub.api.endpoints.objects_api.utils.Include.CustomIncludeAware;
import com.pubnub.api.endpoints.objects_api.utils.Include.HavingChannelInclude;
import com.pubnub.api.endpoints.objects_api.utils.Include.HavingCustomInclude;
import com.pubnub.api.endpoints.objects_api.utils.ListCapabilities.HavingListCapabilites;
import com.pubnub.api.endpoints.objects_api.utils.ListCapabilities.ListCapabilitiesAware;
import com.pubnub.api.endpoints.objects_api.utils.ObjectsBuilderSteps;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.membership.PNSetMembershipResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNChannelMembership;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembership;
import com.pubnub.api.models.server.objects_api.PatchMembershipPayload;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class SetMemberships extends UUIDEndpoint<SetMemberships, EntityArrayEnvelope<PNMembership>, PNSetMembershipResult>
        implements CustomIncludeAware<SetMemberships>, ChannelIncludeAware<SetMemberships>,
        ListCapabilitiesAware<SetMemberships> {
    SetMemberships(final PubNub pubnubInstance,
                   final TelemetryManager telemetry,
                   final RetrofitManager retrofitInstance,
                   final CompositeParameterEnricher compositeParameterEnricher,
                   final TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
    }

    public static  Builder builder(final PubNub pubnubInstance,
                                   final TelemetryManager telemetry,
                                   final RetrofitManager retrofitInstance,
                                   final TokenManager tokenManager) {
        return new Builder(pubnubInstance, telemetry, retrofitInstance, tokenManager);
    }

    @AllArgsConstructor
    public static class Builder implements ObjectsBuilderSteps.ChannelMembershipsStep<SetMemberships> {
        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final TokenManager tokenManager;

        @Override
        public SetMemberships channelMemberships(@NotNull final Collection<PNChannelMembership> channelMemberships) {
            final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault(true, false);
            return new SetMembershipsCommand(channelMemberships, pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher,
                    tokenManager);
        }
    }
}

final class SetMembershipsCommand extends SetMemberships implements HavingCustomInclude<SetMemberships>,
        HavingChannelInclude<SetMemberships>,
        HavingListCapabilites<SetMemberships> {
    private final Collection<PNChannelMembership> channelMemberships;

    SetMembershipsCommand(final Collection<PNChannelMembership> channelMemberships,
                          final PubNub pubNub,
                          final TelemetryManager telemetryManager,
                          final RetrofitManager retrofitManager,
                          final CompositeParameterEnricher compositeParameterEnricher,
                          final TokenManager tokenManager) {
        super(pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
        this.channelMemberships = channelMemberships;
    }

    @Override
    protected Call<EntityArrayEnvelope<PNMembership>> executeCommand(final Map<String, String> effectiveParams)
            throws PubNubException {
        final PatchMembershipPayload patchMembershipBody = new PatchMembershipPayload(channelMemberships,
                Collections.emptyList());
        return getRetrofit()
                .getUuidMetadataService()
                .patchMembership(getPubnub().getConfiguration().getSubscribeKey(), effectiveUuid(), patchMembershipBody,
                        effectiveParams);
    }

    @Override
    protected PNSetMembershipResult createResponse(Response<EntityArrayEnvelope<PNMembership>> input)
            throws PubNubException {
        if (input.body() != null) {
            return new PNSetMembershipResult(input.body());
        } else {
            return new PNSetMembershipResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNSetMembershipsOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }
}
