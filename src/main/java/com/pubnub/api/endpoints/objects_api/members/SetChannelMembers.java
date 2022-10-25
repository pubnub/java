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
import com.pubnub.api.models.consumer.objects_api.member.PNSetChannelMembersResult;
import com.pubnub.api.models.consumer.objects_api.member.PNMembers;
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

public abstract class SetChannelMembers extends ChannelEnpoint<EntityArrayEnvelope<PNMembers>, PNSetChannelMembersResult>
        implements CustomIncludeAware<SetChannelMembers>, UUIDIncludeAware<SetChannelMembers>, ListCapabilitiesAware<SetChannelMembers> {

    SetChannelMembers(final String channel,
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
    public static class Builder implements BuilderSteps.ChannelStep<ObjectsBuilderSteps.UUIDsStep<SetChannelMembers>> {
        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final TokenManager tokenManager;

        @Override
        public ObjectsBuilderSteps.UUIDsStep<SetChannelMembers> channel(final String channel) {
            return new ObjectsBuilderSteps.UUIDsStep<SetChannelMembers>() {
                @Override
                public SetChannelMembers uuids(@NotNull final Collection<PNUUID> uuids) {
                    final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault(true, false);
                    return new SetChannelMembersCommand(channel, uuids, pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher,
                            tokenManager);
                }
            };
        }
    }
}

final class SetChannelMembersCommand extends SetChannelMembers
        implements HavingCustomInclude<SetChannelMembers>, HavingUUIDInclude<SetChannelMembers>, HavingListCapabilites<SetChannelMembers> {
    private final Collection<PNUUID> uuids;

    SetChannelMembersCommand(final String channel,
                             final Collection<PNUUID> uuids,
                             final PubNub pubNub,
                             final TelemetryManager telemetryManager,
                             final RetrofitManager retrofitManager,
                             final CompositeParameterEnricher compositeParameterEnricher,
                             final TokenManager tokenManager) {
        super(channel, pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
        this.uuids = uuids;
    }

    @Override
    protected Call<EntityArrayEnvelope<PNMembers>> executeCommand(final Map<String, String> effectiveParams)
            throws PubNubException {
        final PatchMemberPayload patchMemberBody = new PatchMemberPayload(uuids, Collections.emptyList());

        return getRetrofit()
                .getChannelMetadataService()
                .patchMembers(getPubnub().getConfiguration().getSubscribeKey(), channel, patchMemberBody,
                        effectiveParams);
    }

    @Override
    protected PNSetChannelMembersResult createResponse(final Response<EntityArrayEnvelope<PNMembers>> input)
            throws PubNubException {
        if (input.body() != null) {
            return new PNSetChannelMembersResult(input.body());
        } else {
            return new PNSetChannelMembersResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNSetChannelMembersOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }
}
