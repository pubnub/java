package com.pubnub.api.endpoints.channel_groups;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsListAllResult;
import com.pubnub.api.endpoints.Endpoint;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class ListAllChannelGroup extends Endpoint<Envelope<Object>, PNChannelGroupsListAllResult> {

    public ListAllChannelGroup(PubNub pubnub) {
        super(pubnub);
    }

    @Override
    protected boolean validateParams() {
        return true;
    }

    @Override
    protected Call<Envelope<Object>> doWork(Map<String, String> params) {
        ChannelGroupService service = this.createRetrofit().create(ChannelGroupService.class);

        return service.ListAllChannelGroup(pubnub.getConfiguration().getSubscribeKey(), params);
    }

    @Override
    protected PNChannelGroupsListAllResult createResponse(Response<Envelope<Object>> input) throws PubNubException {
        Map<String, Object> stateMappings;

        if (input.body() == null || input.body().getPayload() == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).build();
        }
        
        stateMappings = (Map<String, Object>) input.body().getPayload();
        List<String> groups = (ArrayList<String>) stateMappings.get("groups");

        return PNChannelGroupsListAllResult.builder()
                .groups(groups)
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
        return PNOperationType.PNChannelGroupsOperation;
    }

}
