package com.pubnub.api.endpoints.presence;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.presence.PNGetStateResult;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.endpoints.Endpoint;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class GetState extends Endpoint<Envelope<Object>, PNGetStateResult> {

    @Setter private List<String> channels;
    @Setter private List<String> channelGroups;
    @Setter private String uuid;

    public GetState(PubNub pubnub) {
        super(pubnub);
        channels = new ArrayList<>();
        channelGroups = new ArrayList<>();
    }

    @Override
    protected boolean validateParams() {

        if (channels.size() == 0 && channelGroups.size() == 0) {
            return false;
        }

        return true;
    }

    @Override
    protected Call<Envelope<Object>> doWork(Map<String, String> params) throws PubNubException {
        PresenceService service = this.createRetrofit().create(PresenceService.class);

        if (channelGroups.size() > 0) {
            params.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        String channelCSV = channels.size() > 0 ? PubNubUtil.joinString(channels, ",") : ",";

        String selectedUUID = uuid != null ? uuid : pubnub.getConfiguration().getUuid();

        return service.getState(pubnub.getConfiguration().getSubscribeKey(), channelCSV, selectedUUID, params);
    }

    @Override
    protected PNGetStateResult createResponse(final Response<Envelope<Object>> input) throws PubNubException {
        Map<String, Object> stateMappings;

        if (channels.size() == 1 && channelGroups.size() == 0) {
            stateMappings = new HashMap<>();
            stateMappings.put(channels.get(0), input.body().getPayload());
        } else {
            stateMappings = (Map<String, Object>) input.body().getPayload();
        }

        return PNGetStateResult.builder().stateByUUID(stateMappings).build();
    }

    protected int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return pubnub.getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNGetState;
    }

}
