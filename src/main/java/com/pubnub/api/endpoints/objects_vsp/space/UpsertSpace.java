package com.pubnub.api.endpoints.objects_vsp.space;

import com.pubnub.api.PubNub;
import com.pubnub.api.SpaceId;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.utils.Include.CustomIncludeAware;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.space.Space;

import java.util.Map;

public interface UpsertSpace extends RemoteAction<Space>, CustomIncludeAware<UpsertSpace> {

    static UpsertSpace create(
            final SpaceId spaceId,
            final PubNub pubNub,
            final TelemetryManager telemetryManager,
            final RetrofitManager retrofitManager,
            final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault();
        return new UpsertSpaceCommand(spaceId, pubNub, telemetryManager, retrofitManager, tokenManager, compositeParameterEnricher);
    }

    UpsertSpace name(String name);

    UpsertSpace description(String description);

    UpsertSpace custom(Map<String, Object> custom);

    UpsertSpace status(String status);

    UpsertSpace type(String type);
}
