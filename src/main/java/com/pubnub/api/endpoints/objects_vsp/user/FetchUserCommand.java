package com.pubnub.api.endpoints.objects_vsp.user;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.objects_api.CompositeParameterEnricher;
import com.pubnub.api.endpoints.objects_api.ObjectApiEndpoint;
import com.pubnub.api.endpoints.objects_api.utils.Include.HavingCustomInclude;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.objects_vsp.user.User;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Map;

@Accessors(chain = true, fluent = true)
final class FetchUserCommand extends ObjectApiEndpoint<EntityEnvelope<User>, User>  implements FetchUser, HavingCustomInclude<FetchUser> {
    @Setter
    private UserId userId;

    FetchUserCommand(
            final PubNub pubNub,
            final TelemetryManager telemetryManager,
            final RetrofitManager retrofitManager,
            final TokenManager tokenManager,
            final CompositeParameterEnricher compositeParameterEnricher) {
        super(pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
    }

    @Override
    protected Call<EntityEnvelope<User>> executeCommand(Map<String, String> effectiveParams) throws PubNubException {
        return getRetrofit()
                .getUserService()
                .fetchUser(getPubnub().getConfiguration().getSubscribeKey(), effectiveUserId().getValue(), effectiveParams);
    }

    @Override
    protected User createResponse(Response<EntityEnvelope<User>> input) throws PubNubException {
        if (input.body() != null) {
            return input.body().getData();
        } else {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INTERNAL_ERROR).build();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNFetchUserOperation;
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
    }

    protected UserId effectiveUserId() {
        try {
            return (userId != null) ? userId : getPubnub().getConfiguration().getUserId();
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }
    }
}
