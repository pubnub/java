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
import com.pubnub.api.models.consumer.objects_api.membership.PNManageMembershipResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembership;
import com.pubnub.api.models.server.objects_api.PatchMembershipPayload;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import lombok.AllArgsConstructor;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class ManageMemberships extends UUIDEndpoint<ManageMemberships, EntityArrayEnvelope<PNMembership>, PNManageMembershipResult>
        implements CustomIncludeAware<ManageMemberships>, ChannelIncludeAware<ManageMemberships>,
        ListCapabilitiesAware<ManageMemberships> {

    ManageMemberships(final PubNub pubnubInstance,
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
    public static class Builder implements ObjectsBuilderSteps.RemoveOrSetStep<ManageMemberships, PNChannelMembership> {
        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final TokenManager tokenManager;

        @Override
        public RemoveStep<ManageMemberships, PNChannelMembership> set(final Collection<PNChannelMembership> channelsToSet) {
            return new RemoveStep<ManageMemberships, PNChannelMembership>() {
                @Override
                public ManageMemberships remove(final Collection<PNChannelMembership> channelsToRemove) {
                    final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher
                            .createDefault(true, false);
                    return new ManageMembershipsCommand(channelsToSet,
                            channelsToRemove,
                            pubnubInstance,
                            telemetry,
                            retrofitInstance,
                            compositeParameterEnricher,
                            tokenManager);
                }
            };
        }

        @Override
        public SetStep<ManageMemberships, PNChannelMembership> remove(final Collection<PNChannelMembership> channelsToRemove) {
            return new SetStep<ManageMemberships, PNChannelMembership>() {
                @Override
                public ManageMemberships set(final Collection<PNChannelMembership> channelsToSet) {
                    final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher
                            .createDefault(true, false);
                    return new ManageMembershipsCommand(channelsToSet,
                            channelsToRemove,
                            pubnubInstance,
                            telemetry,
                            retrofitInstance,
                            compositeParameterEnricher,
                            tokenManager);
                }
            };
        }
    }
}

final class ManageMembershipsCommand extends ManageMemberships implements
        HavingCustomInclude<ManageMemberships>,
        HavingChannelInclude<ManageMemberships>,
        HavingListCapabilites<ManageMemberships> {
    private final Collection<PNChannelMembership> channelsToSet;
    private final Collection<PNChannelMembership> channelsToRemove;

    ManageMembershipsCommand(final Collection<PNChannelMembership> channelsToSet,
                             final Collection<PNChannelMembership> channelsToRemove,
                             final PubNub pubnubInstance,
                             final TelemetryManager telemetry,
                             final RetrofitManager retrofitInstance,
                             final CompositeParameterEnricher compositeParameterEnricher,
                             final TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
        this.channelsToSet = channelsToSet;
        this.channelsToRemove = channelsToRemove;
    }

    @Override
    protected Call<EntityArrayEnvelope<PNMembership>> executeCommand(final Map<String, String> effectiveParams) throws PubNubException {
        final PatchMembershipPayload patchMembershipBody = new PatchMembershipPayload(
                (channelsToSet != null) ? channelsToSet : Collections.emptyList(),
                (channelsToRemove != null) ? channelsToRemove : Collections.emptyList());

        return getRetrofit()
                .getUuidMetadataService()
                .patchMembership(getPubnub().getConfiguration().getSubscribeKey(), effectiveUuid(), patchMembershipBody,
                        effectiveParams);
    }

    @Override
    protected PNManageMembershipResult createResponse(final Response<EntityArrayEnvelope<PNMembership>> input) throws PubNubException {
        if (input.body() != null) {
            return new PNManageMembershipResult(input.body());
        } else {
            return new PNManageMembershipResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNManageMembershipsOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }
}

