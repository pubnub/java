package com.pubnub.api.endpoints.presence;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.endpoints.Endpoint;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class Leave extends Endpoint<Envelope, Boolean> {

    public Leave(PubNub pubnub) {
        super(pubnub);
        channels = new ArrayList<>();
        channelGroups = new ArrayList<>();
    }

    @Setter private List<String> channels;
    @Setter private List<String> channelGroups;

    @Override
    protected boolean validateParams() {
        return true;
    }

    @Override
    protected Call<Envelope> doWork(Map<String, String> params) {
        String channelCSV;
        PresenceService service = this.createRetrofit().create(PresenceService.class);

        if (channelGroups.size() > 0) {
            params.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        if (channels.size() > 0) {
            channelCSV = PubNubUtil.joinString(channels, ",");
        } else {
            channelCSV = ",";
        }

        return service.leave(this.pubnub.getConfiguration().getSubscribeKey(), channelCSV, params);
    }

    @Override
    protected Boolean createResponse(Response<Envelope> input) throws PubNubException {
        return true;
    }

    protected int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return pubnub.getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNUnsubscribeOperation;
    }

    @Override
    protected List<String> getAffectedChannels() {
        return channels;
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return channelGroups;
    }

}
