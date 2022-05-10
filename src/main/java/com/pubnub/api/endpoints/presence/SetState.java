package com.pubnub.api.endpoints.presence;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.builder.dto.StateOperation;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.SubscriptionManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.presence.PNSetStateResult;
import com.pubnub.api.models.server.Envelope;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Accessors(chain = true, fluent = true)
public class SetState extends Endpoint<Envelope<JsonElement>, PNSetStateResult> {

    @Getter(AccessLevel.NONE)
    private SubscriptionManager subscriptionManager;

    @Setter
    private List<String> channels;
    @Setter
    private List<String> channelGroups;
    @Setter
    private Object state;
    @Setter
    private String uuid;


    public SetState(PubNub pubnub,
                    SubscriptionManager subscriptionManagerInstance,
                    TelemetryManager telemetryManager,
                    RetrofitManager retrofit,
                    TokenManager tokenManager) {
        super(pubnub, telemetryManager, retrofit, tokenManager);
        this.subscriptionManager = subscriptionManagerInstance;
        channels = new ArrayList<>();
        channelGroups = new ArrayList<>();
    }

    @Override
    protected List<String> getAffectedChannels() {
        return channels;
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return channelGroups;
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (state == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_STATE_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (channels.size() == 0 && channelGroups.size() == 0) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_AND_GROUP_MISSING).build();
        }
    }

    @Override
    protected Call<Envelope<JsonElement>> doWork(Map<String, String> params) throws PubNubException {
        String selectedUUID = uuid != null ? uuid : this.getPubnub().getConfiguration().getUuid();
        String stringifiedState;

        // only store the state change if we are modifying it for ourselves.
        if (selectedUUID.equals(this.getPubnub().getConfiguration().getUuid())) {
            StateOperation stateOperation = StateOperation.builder()
                    .state(state)
                    .channels(channels)
                    .channelGroups(channelGroups)
                    .build();
            subscriptionManager.adaptStateBuilder(stateOperation);
        }

        if (channelGroups.size() > 0) {
            params.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        stringifiedState = this.getPubnub().getMapper().toJson(state);

        stringifiedState = PubNubUtil.urlEncode(stringifiedState);
        params.put("state", stringifiedState);

        params.putAll(encodeParams(params));

        String channelCSV = channels.size() > 0 ? PubNubUtil.joinString(channels, ",") : ",";

        return this.getRetrofit().getExtendedPresenceService().setState(
                this.getPubnub().getConfiguration().getSubscribeKey(), channelCSV, selectedUUID, params);
    }

    @Override
    protected PNSetStateResult createResponse(Response<Envelope<JsonElement>> input) throws PubNubException {

        if (input.body() == null || input.body().getPayload() == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR).build();
        }

        PNSetStateResult.PNSetStateResultBuilder pnSetStateResult = PNSetStateResult.builder()
                .state(input.body().getPayload());

        return pnSetStateResult.build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNSetStateOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

}
