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
import lombok.experimental.Accessors;

@Accessors(chain = true, fluent = true)
public interface FetchUser extends RemoteAction<User>, CustomIncludeAware<FetchUser> {

    static FetchUser create(
            final PubNub pubNub,
            final TelemetryManager telemetryManager,
            final RetrofitManager retrofitManager,
            final TokenManager tokenManager) {
        final CompositeParameterEnricher compositeParameterEnricher = CompositeParameterEnricher.createDefault();
        return new FetchUserCommand(pubNub, telemetryManager, retrofitManager, tokenManager, compositeParameterEnricher);
    }

    FetchUser userId(UserId userId);

}
