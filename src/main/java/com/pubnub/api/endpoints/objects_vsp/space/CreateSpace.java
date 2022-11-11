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

public interface CreateSpace extends RemoteAction<Space>, CustomIncludeAware<CreateSpace> {

    static CreateSpace create(
            final SpaceId spaceId,
            final PubNub pubNub,
            final TelemetryManager telemetryManager,
            final RetrofitManager retrofitManager,
            final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault();
        return new CreateSpaceCommand(spaceId, pubNub, telemetryManager, retrofitManager, tokenManager, compositeParameterEnricher);
    }

    CreateSpace name(String name);

    CreateSpace description(String description);

    CreateSpace custom(Map<String, Object> custom);

    CreateSpace status(String status);

    CreateSpace type(String type);

}
