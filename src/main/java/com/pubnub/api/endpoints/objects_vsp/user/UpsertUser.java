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

public interface UpsertUser extends RemoteAction<User>, CustomIncludeAware<UpsertUser> {

    static UpsertUser create(
            final PubNub pubNub,
            final TelemetryManager telemetryManager,
            final RetrofitManager retrofitManager,
            final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault();
        return new UpsertUserCommand(pubNub, telemetryManager, retrofitManager, tokenManager, compositeParameterEnricher);
    }

    UpsertUser userId(UserId userId);

    UpsertUser name(String name);

    UpsertUser email(String email);

    UpsertUser profileUrl(String profileUrl);

    UpsertUser externalId(String externalId);

    UpsertUser custom(Map<String, Object> custom);

    UpsertUser status(String status);

    UpsertUser type(String type);
}
