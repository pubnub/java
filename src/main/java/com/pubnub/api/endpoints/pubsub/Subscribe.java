package com.pubnub.api.endpoints.pubsub;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.server.SubscribeEnvelope;
import com.pubnub.api.endpoints.Endpoint;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Supports calling of the subscribe endpoints and deconstructs the response to POJO's.
 */
@Slf4j
@Accessors(chain = true, fluent = true)
public class Subscribe extends Endpoint<SubscribeEnvelope, SubscribeEnvelope> {

    /**
     * List of channels that will be called to subscribe.
     */
    @Setter private List<String> channels;
    /**
     * List of channel groups that will be called with subscribe.
     */
    @Setter private List<String> channelGroups;

    /**
     * timeToken to subscribe with 0 for initial subscribe.
     */
    @Setter private Long timetoken;

    /**
     * filterExpression used as part of PubSub V2 specification to filter on message.
     */
    @Setter private String filterExpression;

    /**
     * region is used as part of PubSub V2 to help the server route traffic to best data center.
     */
    @Setter private String region;

    /**
     * CreFte a new Subscribe instance endpoint.
     * @param pubnub supplied pubnub instance.
     */
    public Subscribe(final PubNub pubnub) {
        super(pubnub);
        channels = new ArrayList<>();
        channelGroups = new ArrayList<>();
    }

    @Override
    protected final boolean validateParams() {
        return true;
    }

    @Override
    protected final Call<SubscribeEnvelope> doWork(final Map<String, String> params) throws PubNubException {
        PubSubService pubSubService = this.createRetrofit().create(PubSubService.class);
        String channelCSV;

        if (channelGroups.size() > 0) {
            params.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        if (filterExpression != null && filterExpression.length() > 0) {
            params.put("filter-expr",  PubNubUtil.urlEncode(filterExpression));
        }

        if (timetoken != null) {
            params.put("tt", timetoken.toString());
        }

        if (region != null) {
            params.put("tr", region);
        }

        if (channels.size() > 0) {
            channelCSV = PubNubUtil.joinString(channels, ",");
        } else {
            channelCSV = ",";
        }

        return pubSubService.subscribe(this.pubnub.getConfiguration().getSubscribeKey(), channelCSV,  params);
    }

    @Override
    protected final SubscribeEnvelope createResponse(final Response<SubscribeEnvelope> input) throws PubNubException {

        if (input.body() == null) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).build();
        }

        return input.body();
    }

    /**
     * called by the parent class to determine how long to wait until connect timeout.
     * @return timeout in seconds
     */
    protected final int getConnectTimeout() {
        return pubnub.getConfiguration().getConnectTimeout();
    }

    /**
     * called by the parent class to determine how long to wait until request timeout.
     * @return timeout in seconds
     */
    protected final int getRequestTimeout() {
        return pubnub.getConfiguration().getSubscribeTimeout();
    }

    @Override
    protected final PNOperationType getOperationType() {
        return PNOperationType.PNSubscribeOperation;
    }

}
