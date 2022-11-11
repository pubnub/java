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

public interface CreateUser extends RemoteAction<User>, CustomIncludeAware<CreateUser> {

    static CreateUser create(
            final PubNub pubNub,
            final TelemetryManager telemetryManager,
            final RetrofitManager retrofitManager,
            final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault();
        return new CreateUserCommand(pubNub, telemetryManager, retrofitManager, tokenManager, compositeParameterEnricher);
    }

    CreateUser userId(UserId userId);

    CreateUser name(String name);

    CreateUser email(String email);

    CreateUser profileUrl(String profileUrl);

    CreateUser externalId(String externalId);

    CreateUser custom(Map<String, Object> custom);

    CreateUser status(String status);

    CreateUser type(String type);
}
