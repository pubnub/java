package com.pubnub.api.endpoints.message_actions;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.models.consumer.message_actions.PNAddMessageActionResult;
import com.pubnub.api.models.consumer.message_actions.PNMessageAction;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class AddMessageAction extends Endpoint<EntityEnvelope<PNMessageAction>, PNAddMessageActionResult> {

    @Setter
    private String channel;

    @Setter
    private Long messageTimetoken;

    @Setter
    private String type;

    @Setter
    private String value;

    public AddMessageAction(PubNub pubnubInstance, TelemetryManager telemetry, RetrofitManager retrofitInstance) {
        super(pubnubInstance, telemetry, retrofitInstance);
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
        if (type == null || type.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_MESSAGE_ACTION_TYPE_MISSING)
                    .build();
        }
        if (value == null || value.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_MESSAGE_ACTION_VALUE_MISSING)
                    .build();
        }
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null
                || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
    }

    @Override
    protected Call<EntityEnvelope<PNMessageAction>> doWork(Map<String, String> params) {

        params.putAll(encodeParams(params));

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("type", type);
        requestBody.addProperty("value", value);

        return this.getRetrofit()
                .getMessageActionService()
                .addMessageAction(this.getPubnub().getConfiguration().getSubscribeKey(), channel,
                        Long.toString(messageTimetoken).toLowerCase(), requestBody, params);
    }

    @Override
    protected PNAddMessageActionResult createResponse(Response<EntityEnvelope<PNMessageAction>> input)
            throws PubNubException {
        PNAddMessageActionResult.PNAddMessageActionResultBuilder builder = PNAddMessageActionResult.builder();
        if (input.body() != null) {
            builder.pnMessageAction(input.body().getData());
        }
        return builder.build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNAddMessageAction;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }
}
