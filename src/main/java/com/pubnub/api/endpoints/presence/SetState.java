package com.pubnub.api.endpoints.presence;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import org.jetbrains.annotations.Nullable;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Accessors(chain = true, fluent = true)
public class SetState extends Endpoint<Envelope, PNSetStateResult> {

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
    @Setter
    private boolean withHeartbeat;


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
        //Heartbeat endpoint accepts state being not json and not returns error but state is not being stored in this case
        String stringifiedState = this.getPubnub().getMapper().toJson(state);
        if (!isValidJson(stringifiedState)) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_STATE_MUST_BE_JSON_OBJECT).build();
        }
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (channels.size() == 0 && channelGroups.size() == 0) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_AND_GROUP_MISSING).build();
        }
    }

    @Override
    protected Call<Envelope> doWork(Map<String, String> params) throws PubNubException {
        String selectedUUID = uuid != null ? uuid : this.getPubnub().getConfiguration().getUserId().getValue();
        String stringifiedState;

        // only store the state change if we are modifying it for ourselves.
        if (selectedUUID.equals(this.getPubnub().getConfiguration().getUserId().getValue())) {
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

        String channelCSV = channels.size() > 0 ? PubNubUtil.joinString(channels, ",") : ",";

        if (withHeartbeat) {
            params.put("heartbeat", String.valueOf(this.getPubnub().getConfiguration().getPresenceTimeout()));
            String encodedStateForChannelsAndGroups = getStateParamValue();
            params.put("state", encodedStateForChannelsAndGroups);
            params.putAll(encodeParams(params));
            return this.getRetrofit().getPresenceService().heartbeat(this.getPubnub().getConfiguration().getSubscribeKey(), channelCSV, params);
        } else {
            stringifiedState = this.getPubnub().getMapper().toJson(state);
            String encodedState = PubNubUtil.urlEncode(stringifiedState);
            params.put("state", encodedState);
            params.putAll(encodeParams(params));
            return this.getRetrofit().getExtendedPresenceService().setState(
                    this.getPubnub().getConfiguration().getSubscribeKey(), channelCSV, selectedUUID, params);
        }
    }

    @Override
    protected PNSetStateResult createResponse(Response<Envelope> input) throws PubNubException {
        if (withHeartbeat) {
            //heartbeat endpoint doesn't return state as presenceData endpoint, so we just return state provided by user
            String stateAsString = this.getPubnub().getMapper().toJson(state);
            JsonObject jsonObject = JsonParser.parseString(stateAsString).getAsJsonObject();

            PNSetStateResult.PNSetStateResultBuilder pnSetStateResult = PNSetStateResult.builder().state(jsonObject);
            return pnSetStateResult.build();
        } else {
            if (input.body() == null || input.body().getPayload() == null) {
                throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR).build();
            }
            JsonElement jsonElement = JsonParser.parseString(new Gson().toJson(input.body().getPayload()));
            PNSetStateResult.PNSetStateResultBuilder pnSetStateResult = PNSetStateResult.builder().state(jsonElement);

            return pnSetStateResult.build();
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNSetStateOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

    @Nullable
    private String getStateParamValue() throws PubNubException {
        Map<String, Object> stateParamValue = new HashMap<>();
        for (String channel : channels) {
            stateParamValue.put(channel, state);
        }
        for (String channelGroup : channelGroups) {
            stateParamValue.put(channelGroup, state);
        }

        String stringifiedStatePerChannel = this.getPubnub().getMapper().toJson(stateParamValue);
        String encodedStatePerChannel = PubNubUtil.urlEncode(stringifiedStatePerChannel);
        return encodedStatePerChannel;
    }
}
