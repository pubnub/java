package com.pubnub.api.endpoints.message_actions;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.message_actions.PNRemoveMessageActionResult;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class RemoveMessageAction extends Endpoint<Object, PNRemoveMessageActionResult> {

    @Setter
    private String channel;

    @Setter
    private Long messageTimetoken;

    @Setter
    private Long actionTimetoken;

    public RemoveMessageAction(PubNub pubnubInstance,
                               TelemetryManager telemetry,
                               RetrofitManager retrofitInstance,
                               TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, tokenManager);
    }

    @Override
    protected List<String> getAffectedChannels() {
        return Collections.singletonList(channel);
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return null;
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (channel == null || channel.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING).build();
        }
        if (messageTimetoken == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_MESSAGE_TIMETOKEN_MISSING).build();
        }
        if (actionTimetoken == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_MESSAGE_ACTION_TIMETOKEN_MISSING)
                    .build();
        }
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub()
                .getConfiguration()
                .getSubscribeKey()
                .isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
    }

    @Override
    protected Call<Object> doWork(Map<String, String> params) throws PubNubException {

        params.putAll(encodeAuthParamValue(params));

        return this.getRetrofit()
                .getMessageActionService()
                .deleteMessageAction(this.getPubnub().getConfiguration().getSubscribeKey(), channel,
                        Long.toString(messageTimetoken).toLowerCase(), Long.toString(actionTimetoken).toLowerCase(),
                        params);
    }

    @Override
    protected PNRemoveMessageActionResult createResponse(Response<Object> input) throws PubNubException {
        return new PNRemoveMessageActionResult();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNDeleteMessageAction;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }
}
