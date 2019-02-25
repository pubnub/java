package com.pubnub.api.endpoints;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.models.consumer.history.PNMessageCountResult;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class MessageCounts extends Endpoint<JsonElement, PNMessageCountResult> {

    /**
     * The channel name you wish to pull history from. May be a single channel, or multiple channels, separated by
     * comma.
     */
    @Setter
    private List<String> channels;

    /**
     * Single timetoken to cover all channels passed in on the request path. This parameter is incompatible with
     * {@link MessageCounts#channelsTimetoken}.
     */
    @Setter
    private Long timetoken;

    /**
     * Comma-delimited list of timetokens, in order of the channels list, in the request path. If list of timetokens
     * is not same length as list of channels, a 400 bad request will result. This parameter is incompatible with
     * {@link MessageCounts#timetoken}.
     */
    @Setter
    private List<Long> channelsTimetoken;

    public MessageCounts(PubNub pubnub, TelemetryManager telemetryManager, RetrofitManager retrofit) {
        super(pubnub, telemetryManager, retrofit);
    }

    @Override
    protected List<String> getAffectedChannels() {
        return channels;
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return null;
    }

    @Override
    protected void validateParams() throws PubNubException {

        if (channels == null || channels.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING).build();
        }
        if ((channelsTimetoken == null || channelsTimetoken.isEmpty()) && (timetoken == null)) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_TIMETOKEN_MISSING).build();
        }
    }

    @Override
    protected Call<JsonElement> doWork(Map<String, String> params) {

        if (channelsTimetoken != null && !channelsTimetoken.isEmpty()) {
            params.put("channelsTimetoken", PubNubUtil.joinLong(channelsTimetoken, ","));
        } else if (timetoken != null) {
            params.put("timetoken", Long.toString(timetoken).toLowerCase());
        }

        return this.getRetrofit()
                .getHistoryService()
                .fetchCount(this.getPubnub().getConfiguration().getSubscribeKey(),
                        PubNubUtil.joinString(channels, ","), params);
    }

    @Override
    protected PNMessageCountResult createResponse(Response<JsonElement> input) throws PubNubException {

        PNMessageCountResult.PNMessageCountResultBuilder messageCountsData = PNMessageCountResult.builder();
        HashMap<String, Long> channelsMap = new HashMap<>();

        MapperManager mapper = getPubnub().getMapper();

        if (input.body() != null) {

            if (mapper.hasField(input.body(), "channels")) {
                for (Iterator<Map.Entry<String, JsonElement>> it = mapper.getObjectIterator(input.body(), "channels")
                     ; it.hasNext(); ) {
                    Map.Entry<String, JsonElement> entry = it.next();
                    channelsMap.put(entry.getKey(), entry.getValue().getAsLong());
                }
                messageCountsData.channels(channelsMap);
            } else {
                throw PubNubException.builder()
                        .pubnubError(PubNubErrorBuilder.PNERROBJ_HTTP_ERROR)
                        .errormsg("History is disabled")
                        .jso(input.body())
                        .build();
            }
        }

        return messageCountsData.build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNMessageCountOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

}
