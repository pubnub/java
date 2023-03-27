package com.pubnub.api.endpoints.pubsub;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.SpaceId;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNPublishResult;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class Signal extends Endpoint<List<Object>, PNPublishResult> {
    static final String SPACE_ID_QUERY_PARAMETER = "space-id";
    static final String TYPE_QUERY_PARAMETER = "type";

    @Setter
    private Object message;

    @Setter
    private String channel;

    @Setter
    private String type;

    @Setter
    private SpaceId spaceId;

    public Signal(PubNub pubnub,
                  TelemetryManager telemetryManager,
                  RetrofitManager retrofit,
                  TokenManager tokenManager) {
        super(pubnub, telemetryManager, retrofit, tokenManager);
    }

    @Override
    protected List<String> getAffectedChannels() {
        return Collections.singletonList(channel);
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return Collections.emptyList();
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (message == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_MESSAGE_MISSING).build();
        }
        if (channel == null || channel.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub()
                .getConfiguration()
                .getSubscribeKey()
                .isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getPublishKey() == null || this.getPubnub()
                .getConfiguration()
                .getPublishKey()
                .isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PUBLISH_KEY_MISSING).build();
        }
    }

    @Override
    protected Call<List<Object>> doWork(Map<String, String> params) throws PubNubException {
        MapperManager mapper = this.getPubnub().getMapper();
        String stringifiedMessage = mapper.toJson(message);
        stringifiedMessage = PubNubUtil.urlEncode(stringifiedMessage);
        Map<String, String> signalSpecificParams = extendRequestParamMapBySignalSpecificParams(params);

        return this.getRetrofit().getSignalService().signal(this.getPubnub().getConfiguration().getPublishKey(),
                this.getPubnub().getConfiguration().getSubscribeKey(),
                channel, stringifiedMessage, signalSpecificParams);
    }

    @Override
    protected PNPublishResult createResponse(Response<List<Object>> input) throws PubNubException {
        PNPublishResult.PNPublishResultBuilder pnPublishResult = PNPublishResult.builder();
        pnPublishResult.timetoken(Long.valueOf(input.body().get(2).toString()));

        return pnPublishResult.build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNSignalOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

    private Map<String, String> extendRequestParamMapBySignalSpecificParams(Map<String, String> params) {
        if (type != null) {
            params.put(TYPE_QUERY_PARAMETER, type);
        }
        if (spaceId != null) {
            params.put(SPACE_ID_QUERY_PARAMETER, spaceId.getValue());
        }
        params.putAll(encodeAuthParamValue(params));
        return params;
    }
}
