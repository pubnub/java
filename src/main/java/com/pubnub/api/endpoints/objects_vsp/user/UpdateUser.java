package com.pubnub.api.endpoints.objects_vsp.user;

import com.pubnub.api.PubNub;
import com.pubnub.api.UserId;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.utils.Include.CustomIncludeAware;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.user.User;

import java.util.Map;

public interface UpdateUser extends RemoteAction<User>, CustomIncludeAware<UpdateUser> {

    static UpdateUser create(
            final PubNub pubNub,
            final TelemetryManager telemetryManager,
            final RetrofitManager retrofitManager,
            final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault();
        return new UpdateUserCommand(pubNub, telemetryManager, retrofitManager, tokenManager, compositeParameterEnricher);
    }

    UpdateUser userId(UserId userId);

    UpdateUser name(String name);

    UpdateUser email(String email);

    UpdateUser profileUrl(String profileUrl);

    UpdateUser externalId(String externalId);

    UpdateUser custom(Map<String, Object> custom);

    UpdateUser status(String status);

    UpdateUser type(String type);
}
