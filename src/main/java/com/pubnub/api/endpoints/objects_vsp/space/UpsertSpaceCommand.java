package com.pubnub.api.endpoints.objects_vsp.space;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.ObjectApiEndpoint;
import com.pubnub.api.endpoints.objects_api.utils.Include.HavingCustomInclude;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.space.Space;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import com.pubnub.api.models.server.objects_vsp.space.UpsertSpacePayload;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true, fluent = true)
final class UpsertSpaceCommand extends ObjectApiEndpoint<EntityEnvelope<Space>, Space> implements UpsertSpace, HavingCustomInclude<UpsertSpace> {
    private final SpaceId spaceId;
    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    private Map<String, Object> custom;
    @Setter
    private String status;
    @Setter
    private String type;

    UpsertSpaceCommand(
            final SpaceId spaceId,
            final PubNub pubNub,
            final TelemetryManager telemetryManager,
            final RetrofitManager retrofitManager,
            final TokenManager tokenManager,
            final CompositeParameterEnricher compositeParameterEnricher) {
        super(pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
        this.spaceId = spaceId;
    }

    @Override
    protected Call<EntityEnvelope<Space>> executeCommand(Map<String, String> effectiveParams) throws PubNubException {
        final HashMap<String, Object> customHashMap = new HashMap<>();
        if (custom != null) {
            customHashMap.putAll(custom);
        }

        final UpsertSpacePayload upsertUserPayload = new UpsertSpacePayload(name, description, customHashMap, status, type);

        String subscribeKey = getPubnub().getConfiguration().getSubscribeKey();
        return getRetrofit().getSpaceService().upsertSpace(subscribeKey, spaceId.getValue(), upsertUserPayload, effectiveParams);
    }

    @Override
    protected Space createResponse(Response<EntityEnvelope<Space>> input) throws PubNubException {
        if (input != null) {
            return input.body().getData();
        } else {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INTERNAL_ERROR).build();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNUpsertSpaceOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }
}
