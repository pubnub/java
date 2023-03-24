package com.pubnub.api.endpoints.pubsub;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.SpaceId;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.PublishSequenceManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.vendor.Crypto;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class Publish extends Endpoint<List<Object>, PNPublishResult> {

    static final String SPACE_ID_QUERY_PARAMETER = "space-id";
    static final String TYPE_QUERY_PARAMETER = "type";
    @Setter
    private Object message;
    @Setter
    private String channel;
    @Setter
    private Boolean shouldStore;
    @Setter
    private Boolean usePOST;
    @Setter
    private Object meta;
    @Setter
    private Boolean replicate;
    @Setter
    private Integer ttl;
    @Setter
    private String type;
    @Setter
    private SpaceId spaceId;

    private PublishSequenceManager publishSequenceManager;

    public Publish(PubNub pubnub,
                   PublishSequenceManager providedPublishSequenceManager,
                   TelemetryManager telemetryManager,
                   RetrofitManager retrofit,
                   TokenManager tokenManager) {
        super(pubnub, telemetryManager, retrofit, tokenManager);

        this.publishSequenceManager = providedPublishSequenceManager;
        this.replicate = true;
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
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }
        if (this.getPubnub().getConfiguration().getPublishKey() == null || this.getPubnub().getConfiguration().getPublishKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PUBLISH_KEY_MISSING).build();
        }
    }

    @Override
    protected Call<List<Object>> doWork(Map<String, String> params) throws PubNubException {
        String stringifiedMessage = getEncryptedMessageAsString();
        Map<String, String> publishSpecificParams = extendRequestParamMapByPublishSpecificParams(params);

        if (shouldSendViaPOST()) {
            return sendRequestViaPOST(stringifiedMessage, publishSpecificParams);
        } else {
            return sendRequestViaGET(stringifiedMessage, publishSpecificParams);
        }
    }

    @Override
    protected PNPublishResult createResponse(Response<List<Object>> input) throws PubNubException {
        PNPublishResult.PNPublishResultBuilder pnPublishResult = PNPublishResult.builder();
        pnPublishResult.timetoken(Long.valueOf(input.body().get(2).toString()));

        return pnPublishResult.build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNPublishOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

    private String getEncryptedMessageAsString() throws PubNubException {
        MapperManager mapper = this.getPubnub().getMapper();
        String stringifiedMessage = mapper.toJson(message);

        if (this.getPubnub().getConfiguration().getCipherKey() != null) {
            Crypto crypto = new Crypto(this.getPubnub().getConfiguration().getCipherKey(), this.getPubnub().getConfiguration().isUseRandomInitializationVector());
            stringifiedMessage = crypto.encrypt(stringifiedMessage).replace("\n", "");
        }
        return stringifiedMessage;
    }

    private Map<String, String> extendRequestParamMapByPublishSpecificParams(Map<String, String> params) throws PubNubException {
        MapperManager mapper = this.getPubnub().getMapper();

        if (meta != null) {
            String stringifiedMeta = mapper.toJson(meta);
            stringifiedMeta = PubNubUtil.urlEncode(stringifiedMeta);
            params.put("meta", stringifiedMeta);
        }

        if (shouldStore != null) {
            if (shouldStore) {
                params.put("store", "1");
            } else {
                params.put("store", "0");
            }
        }

        if (ttl != null) {
            params.put("ttl", String.valueOf(ttl));
        }

        params.put("seqn", String.valueOf(publishSequenceManager.getNextSequence()));

        if (Boolean.FALSE.equals(replicate)) {
            params.put("norep", "true");
        }

        if (spaceId != null) {
            params.put(SPACE_ID_QUERY_PARAMETER, spaceId.getValue());
        }

        if (type != null) {
            params.put(TYPE_QUERY_PARAMETER, type);
        }

        params.putAll(encodeAuthParamValue(params));

        return params;
    }

    private boolean shouldSendViaPOST() {
        return Boolean.TRUE.equals(usePOST);
    }

    private Call<List<Object>> sendRequestViaPOST(String stringifiedMessage, Map<String, String> publishSpecificParams) {
        Object payloadToSend;
        if (this.getPubnub().getConfiguration().getCipherKey() != null) {
            payloadToSend = stringifiedMessage;
        } else {
            payloadToSend = message;
        }

        return this.getRetrofit().getPublishService().publishWithPost(this.getPubnub().getConfiguration().getPublishKey(),
                this.getPubnub().getConfiguration().getSubscribeKey(),
                channel, payloadToSend, publishSpecificParams);
    }

    private Call<List<Object>> sendRequestViaGET(String stringifiedMessage, Map<String, String> publishSpecificParams) {
        if (this.getPubnub().getConfiguration().getCipherKey() != null) {
            stringifiedMessage = "\"".concat(stringifiedMessage).concat("\"");
        }

        stringifiedMessage = PubNubUtil.urlEncode(stringifiedMessage);

        return this.getRetrofit().getPublishService().publish(this.getPubnub().getConfiguration().getPublishKey(),
                this.getPubnub().getConfiguration().getSubscribeKey(),
                channel, stringifiedMessage, publishSpecificParams);
    }

}
