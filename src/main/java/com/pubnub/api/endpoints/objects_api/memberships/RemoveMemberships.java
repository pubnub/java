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
import com.pubnub.api.models.consumer.objects_api.membership.PNChannelMembership;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembership;
import com.pubnub.api.models.consumer.objects_api.membership.PNRemoveMembershipResult;
import com.pubnub.api.models.server.objects_api.PatchMembershipPayload;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class RemoveMemberships extends UUIDEndpoint<RemoveMemberships, EntityArrayEnvelope<PNMembership>, PNRemoveMembershipResult>
        implements CustomIncludeAware<RemoveMemberships>, ChannelIncludeAware<RemoveMemberships>,
        ListCapabilitiesAware<RemoveMemberships> {

    RemoveMemberships(final PubNub pubnubInstance,
                      final TelemetryManager telemetry,
                      final RetrofitManager retrofitInstance,
                      final CompositeParameterEnricher compositeParameterEnricher,
                      final TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
    }

    public static Builder builder(final PubNub pubnubInstance,
                                  final TelemetryManager telemetry,
                                  final RetrofitManager retrofitInstance,
                                  final TokenManager tokenManager) {
        return new Builder(pubnubInstance, telemetry, retrofitInstance, tokenManager);
    }

    @AllArgsConstructor
    public static class Builder implements ObjectsBuilderSteps.ChannelMembershipsStep<RemoveMemberships> {
        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final TokenManager tokenManager;

        @Override
        public RemoveMemberships channelMemberships(@NotNull final Collection<PNChannelMembership> channelMemberships) {
            final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault(true, false);
            return new RemoveMembershipsCommand(channelMemberships, pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher,
                    tokenManager);
        }
    }
}

final class RemoveMembershipsCommand extends RemoveMemberships
        implements HavingCustomInclude<RemoveMemberships>, HavingChannelInclude<RemoveMemberships>,
        HavingListCapabilites<RemoveMemberships> {
    private final Collection<PNChannelMembership> channels;

    RemoveMembershipsCommand(final Collection<PNChannelMembership> channels,
                             final PubNub pubNub,
                             final TelemetryManager telemetryManager,
                             final RetrofitManager retrofitManager,
                             final CompositeParameterEnricher compositeParameterEnricher,
                             final TokenManager tokenManager) {
        super(pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
        this.channels = channels;
    }

    @Override
    protected Call<EntityArrayEnvelope<PNMembership>> executeCommand(final Map<String, String> effectiveParams)
            throws PubNubException {
        final PatchMembershipPayload patchMembershipBody = new PatchMembershipPayload(Collections.emptyList(),
                channels);

        return getRetrofit()
                .getUuidMetadataService()
                .patchMembership(getPubnub().getConfiguration().getSubscribeKey(), effectiveUuid(), patchMembershipBody,
                        effectiveParams);
    }

    @Override
    protected PNRemoveMembershipResult createResponse(Response<EntityArrayEnvelope<PNMembership>> input)
            throws PubNubException {
        if (input.body() != null) {
            return new PNRemoveMembershipResult(input.body());
        } else {
            return new PNRemoveMembershipResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNRemoveMembershipsOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }
}
