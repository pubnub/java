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
import com.pubnub.api.models.server.objects_vsp.user.CreateUserPayload;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true, fluent = true)
final class CreateUserCommand extends ObjectApiEndpoint<EntityEnvelope<User>, User> implements CreateUser, HavingCustomInclude<CreateUser> {
    @Setter
    private UserId userId;
    @Setter
    private String name;
    @Setter
    private String email;
    @Setter
    private String profileUrl;
    @Setter
    private String externalId;
    @Setter
    private Map<String, Object> custom;
    @Setter
    private String status;
    @Setter
    private String type;

    CreateUserCommand(
            final PubNub pubNub,
            final TelemetryManager telemetryManager,
            final RetrofitManager retrofitManager,
            final TokenManager tokenManager,
            final CompositeParameterEnricher compositeParameterEnricher) {
        super(pubNub, telemetryManager, retrofitManager, compositeParameterEnricher, tokenManager);
    }

    @Override
    protected Call<EntityEnvelope<User>> executeCommand(Map<String, String> effectiveParams) throws PubNubException {
        //This is workaround to accept custom maps that are instances of anonymous classes not handled by gson
        final HashMap<String, Object> customHashMap = new HashMap<>();
        if (custom != null) {
            customHashMap.putAll(custom);
        }

        final CreateUserPayload createUserPayload = new CreateUserPayload(name, email, externalId, profileUrl, customHashMap, status, type);
        String subscribeKey = getPubnub().getConfiguration().getSubscribeKey();
        return getRetrofit()
                .getUserService()
                .createUser(subscribeKey, effectiveUserId().getValue(), createUserPayload, effectiveParams);
    }

    @Override
    public CompositeParameterEnricher getCompositeParameterEnricher() {
        return super.getCompositeParameterEnricher();
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
        return PNOperationType.PNCreateUserOperation;
    }

    private UserId effectiveUserId() {
        try {
            return (userId != null) ? userId : getPubnub().getConfiguration().getUserId();
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }
    }

}
