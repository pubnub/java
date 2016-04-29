package com.pubnub.api.endpoints.channel_groups;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsAllChannelsResult;
import com.pubnub.api.endpoints.Endpoint;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class AllChannelsChannelGroup extends Endpoint<Envelope<Object>, PNChannelGroupsAllChannelsResult> {
    @Setter private String channelGroup;

    public AllChannelsChannelGroup(PubNub pubnub) {
        super(pubnub);
    }

    @Override
    protected boolean validateParams() {
        return true;
    }

    @Override
    protected Call<Envelope<Object>> doWork(Map<String, String> params) {
        ChannelGroupService service = this.createRetrofit().create(ChannelGroupService.class);

        return service.AllChannelsChannelGroup(pubnub.getConfiguration().getSubscribeKey(), channelGroup, params);
    }

    @Override
    protected PNChannelGroupsAllChannelsResult createResponse(Response<Envelope<Object>> input) throws PubNubException {
        Map<String, Object> stateMappings;

        if (input.body() == null || input.body().getPayload() == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).build();
        }

        stateMappings = (Map<String, Object>) input.body().getPayload();
        List<String> channels = (ArrayList<String>) stateMappings.get("channels");

        return PNChannelGroupsAllChannelsResult.builder()
                .channels(channels)
                .build();
    }

    protected int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return pubnub.getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNChannelsForGroupOperation;
    }

}
