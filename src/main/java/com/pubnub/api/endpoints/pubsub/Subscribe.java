package com.pubnub.api.endpoints.pubsub;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.server.SubscribeEnvelope;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import retrofit2.Call;
import retrofit2.Response;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Supports calling of the subscribe endpoints and deconstructs the response to POJO's.
 */
@Slf4j
@Accessors(chain = true, fluent = true)
public class Subscribe extends Endpoint<SubscribeEnvelope, SubscribeEnvelope> {
    static final int RATE_LIMIT_EXCEEDED = 429;
    static final int URI_TOO_LONG = 414;
    /**
     * List of channels that will be called to subscribe.
     */
    @Setter
    private List<String> channels;
    /**
     * List of channel groups that will be called with subscribe.
     */
    @Setter
    private List<String> channelGroups;

    /**
     * timetoken to subscribe with 0 for initial subscribe.
     */
    @Setter
    private Long timetoken;

    /**
     * filterExpression used as part of PubSub V2 specification to filter on message.
     */
    @Setter
    private String filterExpression;

    /**
     * region is used as part of PubSub V2 to help the server route traffic to best data center.
     */
    @Setter
    private String region;

    @Setter
    private Object state;

    /**
     * Create a new Subscribe instance endpoint.
     *
     * @param pubnub supplied pubnub instance.
     */
    public Subscribe(PubNub pubnub, RetrofitManager retrofit, TokenManager tokenManager) {
        super(pubnub, null, retrofit, tokenManager);
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
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub()
                .getConfiguration()
                .getSubscribeKey()
                .isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (channels.isEmpty() && channelGroups.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_AND_GROUP_MISSING).build();
        }
    }

    @Override
    public void async(@NotNull PNCallback<SubscribeEnvelope> callback) {
        super.async(new PNCallback<SubscribeEnvelope>() {
            @Override
            public void onResponse(@Nullable SubscribeEnvelope result, @NotNull PNStatus status) {
                if (status.isError()) {
                    final PNStatus maybeNewStatus;
                    if (status.getStatusCode() == HttpURLConnection.HTTP_BAD_REQUEST && status.getErrorData()
                            .getInformation()
                            .contains("Filter syntax error")) {
                        maybeNewStatus = status.toBuilder()
                                .category(PNStatusCategory.PNMalformedFilterExpressionCategory)
                                .build();
                    } else if (status.getStatusCode() == URI_TOO_LONG) {
                        maybeNewStatus = status.toBuilder()
                                .category(PNStatusCategory.PNURITooLongCategory)
                                .build();
                    } else if (status.getStatusCode() == RATE_LIMIT_EXCEEDED) {
                        maybeNewStatus = status.toBuilder()
                                .category(PNStatusCategory.PNRateLimitExceededCategory)
                                .build();
                    } else {
                        maybeNewStatus = status;
                    }
                    callback.onResponse(result, maybeNewStatus);
                } else {
                    callback.onResponse(result, status);
                }

            }
        });
    }

    @Override
    protected Call<SubscribeEnvelope> doWork(Map<String, String> params) throws PubNubException {
        MapperManager mapper = this.getPubnub().getMapper();

        String channelCSV;

        if (!channelGroups.isEmpty()) {
            params.put("channel-group", PubNubUtil.joinString(channelGroups, ","));
        }

        if (filterExpression != null && filterExpression.length() > 0) {
            params.put("filter-expr", PubNubUtil.urlEncode(filterExpression));
        }

        if (timetoken != null) {
            params.put("tt", timetoken.toString());
        }

        if (region != null) {
            params.put("tr", region);
        }

        if (!channels.isEmpty()) {
            channelCSV = PubNubUtil.joinString(channels, ",");
        } else {
            channelCSV = ",";
        }

        params.put("heartbeat", String.valueOf(this.getPubnub().getConfiguration().getPresenceTimeout()));

        if (state != null) {
            String stringifiedState = mapper.toJson(state);
            stringifiedState = PubNubUtil.urlEncode(stringifiedState);
            params.put("state", stringifiedState);
        }

        params.putAll(encodeAuthParamValue(params));

        return this.getRetrofit().getSubscribeService()
                .subscribe(this.getPubnub().getConfiguration().getSubscribeKey(), channelCSV, params);
    }

    @Override
    protected SubscribeEnvelope createResponse(Response<SubscribeEnvelope> input) throws PubNubException {

        if (input.body() == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR).build();
        }

        return input.body();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNSubscribeOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

}
