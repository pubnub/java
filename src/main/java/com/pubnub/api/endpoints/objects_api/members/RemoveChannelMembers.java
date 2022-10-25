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
import com.pubnub.api.endpoints.objects_api.utils.ObjectsBuilderSteps;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_api.member.PNMembers;
import com.pubnub.api.models.consumer.objects_api.member.PNRemoveChannelMembersResult;
import com.pubnub.api.models.consumer.objects_api.member.PNUUID;
import com.pubnub.api.models.server.objects_api.PatchMemberPayload;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class RemoveChannelMembers extends ChannelEnpoint<EntityArrayEnvelope<PNMembers>, PNRemoveChannelMembersResult>
        implements CustomIncludeAware<RemoveChannelMembers>, UUIDIncludeAware<RemoveChannelMembers>, ListCapabilitiesAware<RemoveChannelMembers> {

    RemoveChannelMembers(final String channel,
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
        return new Builder(pubnubInstance, telemetry, retrofitInstance, tokenManager);
    }

    @AllArgsConstructor
    public static class Builder implements BuilderSteps.ChannelStep<ObjectsBuilderSteps.UUIDsStep<RemoveChannelMembers>> {
        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final TokenManager tokenManager;

        @Override
        public ObjectsBuilderSteps.UUIDsStep<RemoveChannelMembers> channel(final String channel) {
            return new ObjectsBuilderSteps.UUIDsStep<RemoveChannelMembers>() {
                @Override
                public RemoveChannelMembers uuids(@NotNull final Collection<PNUUID> uuids) {
                    final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault(true, false);
                    return new RemoveChannelMembersCommand(channel, uuids, pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
                }
            };
        }
    }
}

final class RemoveChannelMembersCommand extends RemoveChannelMembers implements
        HavingCustomInclude<RemoveChannelMembers>,
        HavingUUIDInclude<RemoveChannelMembers>,
        HavingListCapabilites<RemoveChannelMembers> {
    private final Collection<PNUUID> uuids;

    RemoveChannelMembersCommand(final String channel,
                                final Collection<PNUUID> uuids,
                                final PubNub pubNub,
                                final TelemetryManager telemetryManager,
                                final RetrofitManager retrofitManager,
                                final CompositeParameterEnricher compositeParameterEnricher,
                                TokenManager tokenManager) {
        super(channel, pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
        this.uuids = uuids;
    }

    @Override
    protected Call<EntityArrayEnvelope<PNMembers>> executeCommand(final Map<String, String> effectiveParams)
            throws PubNubException {
        final PatchMemberPayload patchMemberBody = new PatchMemberPayload(Collections.emptyList(), uuids);

        return getRetrofit()
                .getChannelMetadataService()
                .patchMembers(getPubnub().getConfiguration().getSubscribeKey(), channel, patchMemberBody,
                        effectiveParams);
    }

    @Override
    protected PNRemoveChannelMembersResult createResponse(Response<EntityArrayEnvelope<PNMembers>> input)
            throws PubNubException {
        if (input.body() != null) {
            return new PNRemoveChannelMembersResult(input.body());
        } else {
            return new PNRemoveChannelMembersResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNRemoveChannelMembersOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }
}
