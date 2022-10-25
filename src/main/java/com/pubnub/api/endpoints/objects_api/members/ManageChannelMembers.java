package com.pubnub.api.endpoints.objects_api.members;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
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
import com.pubnub.api.models.consumer.objects_api.member.PNManageChannelMembersResult;
import com.pubnub.api.models.consumer.objects_api.member.PNMembers;
import com.pubnub.api.models.consumer.objects_api.member.PNUUID;
import com.pubnub.api.models.server.objects_api.PatchMemberPayload;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import lombok.AllArgsConstructor;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class ManageChannelMembers extends ChannelEnpoint<EntityArrayEnvelope<PNMembers>, PNManageChannelMembersResult>
        implements CustomIncludeAware<ManageChannelMembers>, UUIDIncludeAware<ManageChannelMembers>, ListCapabilitiesAware<ManageChannelMembers> {
    ManageChannelMembers(final String channel,
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
    public static class Builder implements ObjectsBuilderSteps.ChannelStep<ObjectsBuilderSteps.RemoveOrSetStep<ManageChannelMembers, PNUUID>> {
        private final PubNub pubnubInstance;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofitInstance;
        private final TokenManager tokenManager;

        @Override
        public ObjectsBuilderSteps.RemoveOrSetStep<ManageChannelMembers, PNUUID> channel(final String channel) {
            return new ObjectsBuilderSteps.RemoveOrSetStep<ManageChannelMembers, PNUUID>() {
                @Override
                public RemoveStep<ManageChannelMembers, PNUUID> set(final Collection<PNUUID> uuidsToSet) {
                    return new RemoveStep<ManageChannelMembers, PNUUID>() {
                        @Override
                        public ManageChannelMembers remove(final Collection<PNUUID> uuidsToRemove) {
                            final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher
                                    .createDefault(true, false);
                            return new ManageChannelMembersCommand(channel,
                                    uuidsToSet,
                                    uuidsToRemove,
                                    pubnubInstance,
                                    telemetry,
                                    retrofitInstance,
                                    compositeParameterEnricher,
                                    tokenManager);
                        }
                    };
                }

                @Override
                public SetStep<ManageChannelMembers, PNUUID> remove(final Collection<PNUUID> uuidsToRemove) {
                    return new SetStep<ManageChannelMembers, PNUUID>() {
                        @Override
                        public ManageChannelMembers set(final Collection<PNUUID> uuidsToSet) {
                            final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher
                                    .createDefault(true, false);
                            return new ManageChannelMembersCommand(channel,
                                    uuidsToSet,
                                    uuidsToRemove,
                                    pubnubInstance,
                                    telemetry,
                                    retrofitInstance,
                                    compositeParameterEnricher,
                                    tokenManager);
                        }
                    };
                }
            };
        }
    }
}

final class ManageChannelMembersCommand extends ManageChannelMembers implements
        HavingCustomInclude<ManageChannelMembers>,
        HavingUUIDInclude<ManageChannelMembers>,
        HavingListCapabilites<ManageChannelMembers> {
    private final Collection<PNUUID> uuidsToSet;
    private final Collection<PNUUID> uuidsToRemove;

    ManageChannelMembersCommand(final String channel,
                                final Collection<PNUUID> uuidsToSet,
                                final Collection<PNUUID> uuidsToRemove,
                                final PubNub pubnubInstance,
                                final TelemetryManager telemetry,
                                final RetrofitManager retrofitInstance,
                                final CompositeParameterEnricher compositeParameterEnricher,
                                final TokenManager tokenManager) {
        super(channel, pubnubInstance, telemetry, retrofitInstance, compositeParameterEnricher, tokenManager);
        this.uuidsToSet = uuidsToSet;
        this.uuidsToRemove = uuidsToRemove;
    }

    @Override
    protected Call<EntityArrayEnvelope<PNMembers>> executeCommand(final Map<String, String> effectiveParams) throws PubNubException {
        final PatchMemberPayload patchMemberBody = new PatchMemberPayload(
                (uuidsToSet != null) ? uuidsToSet : Collections.emptyList(),
                (uuidsToRemove != null) ? uuidsToRemove : Collections.emptyList());

        return getRetrofit()
                .getChannelMetadataService()
                .patchMembers(getPubnub().getConfiguration().getSubscribeKey(), channel, patchMemberBody,
                        effectiveParams);
    }

    @Override
    protected PNManageChannelMembersResult createResponse(final Response<EntityArrayEnvelope<PNMembers>> input) throws PubNubException {
        if (input.body() != null) {
            return new PNManageChannelMembersResult(input.body());
        } else {
            return new PNManageChannelMembersResult();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNManageChannelMembersOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }
}
