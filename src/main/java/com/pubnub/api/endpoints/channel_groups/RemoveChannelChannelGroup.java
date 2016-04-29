package com.pubnub.api.endpoints.channel_groups;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsRemoveChannelResult;
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
public class RemoveChannelChannelGroup extends Endpoint<Envelope, PNChannelGroupsRemoveChannelResult> {
    @Setter private String channelGroup;
    @Setter private List<String> channels;


    public RemoveChannelChannelGroup(PubNub pubnub) {
        super(pubnub);
        channels = new ArrayList<>();
    }

    @Override
    protected boolean validateParams() {
        return true;
    }

    @Override
    protected Call<Envelope> doWork(Map<String, String> params) {
        ChannelGroupService service = this.createRetrofit().create(ChannelGroupService.class);

        if (channels.size() > 0) {
            params.put("remove", PubNubUtil.joinString(channels, ","));
        }

        return service.RemoveChannel(pubnub.getConfiguration().getSubscribeKey(), channelGroup, params);
    }

    @Override
    protected PNChannelGroupsRemoveChannelResult createResponse(Response<Envelope> input) throws PubNubException {
        if (input.body() == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).build();
        }

        return PNChannelGroupsRemoveChannelResult.builder().build();
    }

    protected int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    protected int getRequestTimeout() {
        return pubnub.getConfiguration().getNonSubscribeRequestTimeout();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNRemoveChannelsFromGroupOperation;
    }

}
