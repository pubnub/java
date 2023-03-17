package com.pubnub.api.endpoints.pubsub;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.managers.PublishSequenceManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNErrorData;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.apikt.PNConfiguration;
import com.pubnub.apikt.UserId;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Accessors(chain = true, fluent = true)
public class Publish implements RemoteAction<PNPublishResult> {  //nie Endpoint a RemoteAction

    @Getter(AccessLevel.PROTECTED)
    private PubNub pubnub;
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

    private PublishSequenceManager publishSequenceManager;

    public Publish(PubNub pubnub,
                   PublishSequenceManager providedPublishSequenceManager,
                   TelemetryManager telemetryManager,
                   RetrofitManager retrofit,
                   TokenManager tokenManager) {
//        super(pubnub, telemetryManager, retrofit, tokenManager);

        this.publishSequenceManager = providedPublishSequenceManager;
        this.replicate = true;
        this.pubnub = pubnub;
    }


    @Override
    public @Nullable PNPublishResult sync() throws PubNubException {
        //dodatkowo queryParam z Endpointa jest wypiasny w dokumentacji, ale nie da się go użyć w Publishu bo nie jest częścią buildera(nie zwraca publish)

        //jak stworzyć Kotlin PubNub z Java PubNub? Chodzi o przekazanie konfiguracji czyli zamiane PNConfiguration w Javie na Kotlina
        PNConfiguration pnConfigurationKT = new PNConfiguration(new com.pubnub.apikt.UserId("pn-" + UUID.randomUUID()));
        pnConfigurationKT.setSubscribeKey(pubnub.getConfiguration().getSubscribeKey());
        pnConfigurationKT.setPublishKey(pubnub.getConfiguration().getPublishKey());
        com.pubnub.apikt.PubNub pubNubKt = new com.pubnub.apikt.PubNub(pnConfigurationKT);

        Boolean usePostKT = usePOST != null ? usePOST : false;

        com.pubnub.apikt.endpoints.pubsub.Publish publishKT = new com.pubnub.apikt.endpoints.pubsub.Publish(
                pubNubKt,
                message,
                channel,
                meta,
                shouldStore,
                usePostKT,
                replicate,
                ttl
        );

        com.pubnub.apikt.models.consumer.PNPublishResult pnPublishResultKT = publishKT.sync();

        //przerobić pnPublishResult w Kotlinie na pnPublishResult w Javie
        return PNPublishResult.builder().timetoken(pnPublishResultKT.getTimetoken()).build() ; //zwrot z Kotlin
    }

    @Override
    public void async(@NotNull PNCallback<PNPublishResult> callback) {
        PNConfiguration pnConfigurationKT = new PNConfiguration(new com.pubnub.apikt.UserId("pn-" + UUID.randomUUID()));
        pnConfigurationKT.setSubscribeKey(pubnub.getConfiguration().getSubscribeKey());
        pnConfigurationKT.setPublishKey(pubnub.getConfiguration().getPublishKey());
        pnConfigurationKT.setUserId(new UserId(pubnub.getConfiguration().getUserId().getValue()));
        com.pubnub.apikt.PubNub pubNubKt = new com.pubnub.apikt.PubNub(pnConfigurationKT);

        Boolean usePostKT = usePOST != null ? usePOST : false;

        com.pubnub.apikt.endpoints.pubsub.Publish publishKT = new com.pubnub.apikt.endpoints.pubsub.Publish(
                pubNubKt,
                message,
                channel,
                meta,
                shouldStore,
                usePostKT,
                replicate,
                ttl
        );

        publishKT.async((pnPublishResultKT, statusKT) -> {
            PNPublishResult pnPublishResult = PNPublishResult.builder().timetoken(pnPublishResultKT.getTimetoken()).build();
            PNStatus pnStatus = PNStatus.builder().category(PNStatusCategory.PNAcknowledgmentCategory).build(); //to jest prowizorka
            callback.onResponse(pnPublishResult, pnStatus);
            return null;
        });

    }

    private PNStatus createStatusResponse(PNStatusCategory category, Exception throwable,
                                          ArrayList<String> errorChannels, ArrayList<String> errorChannelGroups) {
        PNStatus.PNStatusBuilder pnStatus = PNStatus.builder();

        pnStatus.executedEndpoint(this);

//        if (response == null || throwable != null) {
//            pnStatus.error(true);
//        }
        if (throwable != null) {
            PNErrorData pnErrorData = new PNErrorData(throwable.getMessage(), throwable);
            pnStatus.errorData(pnErrorData);
        }

//        if (response != null) {
//            pnStatus.statusCode(response.code());
//            pnStatus.tlsEnabled(response.raw().request().url().isHttps());
//            pnStatus.origin(response.raw().request().url().host());
//            pnStatus.uuid(response.raw().request().url().queryParameter("uuid"));
//            pnStatus.authKey(response.raw().request().url().queryParameter(PubNubUtil.AUTH_QUERY_PARAM_NAME));
//            pnStatus.clientRequest(response.raw().request());
//        }

        pnStatus.operation(getOperationType());
        pnStatus.category(category);

        if (errorChannels != null && !errorChannels.isEmpty()) {
            pnStatus.affectedChannels(errorChannels);
        } else {
            pnStatus.affectedChannels(getAffectedChannels());
        }

        if (errorChannelGroups != null && !errorChannelGroups.isEmpty()) {
            pnStatus.affectedChannelGroups(errorChannelGroups);
        } else {
            pnStatus.affectedChannelGroups(getAffectedChannelGroups());
        }

        return pnStatus.build();
    }


    @Override
    public void retry() {

    }

    @Override
    public void silentCancel() {

    }

//    @Override
    protected List<String> getAffectedChannels() {
        return Collections.singletonList(channel);
    }

//    @Override
    protected List<String> getAffectedChannelGroups() {
        return null;
    }

//    @Override
//    protected void validateParams() throws PubNubException {
//        if (message == null) {
//            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_MESSAGE_MISSING).build();
//        }
//        if (channel == null || channel.isEmpty()) {
//            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING).build();
//        }
//        if (this.getPubnub().getConfiguration().getSubscribeKey() == null || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
//            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
//        }
//        if (this.getPubnub().getConfiguration().getPublishKey() == null || this.getPubnub().getConfiguration().getPublishKey().isEmpty()) {
//            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PUBLISH_KEY_MISSING).build();
//        }
//    }

//    @Override
//    protected Call<List<Object>> doWork(Map<String, String> params) throws PubNubException {
//        MapperManager mapper = this.getPubnub().getMapper();
//
//        String stringifiedMessage = mapper.toJson(message);
//
//        if (meta != null) {
//            String stringifiedMeta = mapper.toJson(meta);
//            stringifiedMeta = PubNubUtil.urlEncode(stringifiedMeta);
//            params.put("meta", stringifiedMeta);
//        }
//
//        if (shouldStore != null) {
//            if (shouldStore) {
//                params.put("store", "1");
//            } else {
//                params.put("store", "0");
//            }
//        }
//
//        if (ttl != null) {
//            params.put("ttl", String.valueOf(ttl));
//        }
//
//        params.put("seqn", String.valueOf(publishSequenceManager.getNextSequence()));
//
//        if (!replicate) {
//            params.put("norep", "true");
//        }
//
//        if (this.getPubnub().getConfiguration().getCipherKey() != null) {
//            Crypto crypto = new Crypto(this.getPubnub().getConfiguration().getCipherKey(), this.getPubnub().getConfiguration().isUseRandomInitializationVector());
//            stringifiedMessage = crypto.encrypt(stringifiedMessage).replace("\n", "");
//        }
//
//        params.putAll(encodeParams(params));
//
//        if (usePOST != null && usePOST) {
//            Object payloadToSend;
//
//            if (this.getPubnub().getConfiguration().getCipherKey() != null) {
//                payloadToSend = stringifiedMessage;
//            } else {
//                payloadToSend = message;
//            }
//
//            return this.getRetrofit().getPublishService().publishWithPost(this.getPubnub().getConfiguration().getPublishKey(),
//                    this.getPubnub().getConfiguration().getSubscribeKey(),
//                    channel, payloadToSend, params);
//        } else {
//
//            if (this.getPubnub().getConfiguration().getCipherKey() != null) {
//                stringifiedMessage = "\"".concat(stringifiedMessage).concat("\"");
//            }
//
//            stringifiedMessage = PubNubUtil.urlEncode(stringifiedMessage);
//
//            return this.getRetrofit().getPublishService().publish(this.getPubnub().getConfiguration().getPublishKey(),
//                    this.getPubnub().getConfiguration().getSubscribeKey(),
//                    channel, stringifiedMessage, params);
//        }
//    }
//
//    @Override
//    protected PNPublishResult createResponse(Response<List<Object>> input) throws PubNubException {
//        PNPublishResult.PNPublishResultBuilder pnPublishResult = PNPublishResult.builder();
//        pnPublishResult.timetoken(Long.valueOf(input.body().get(2).toString()));
//
//        return pnPublishResult.build();
//    }
//
//    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNPublishOperation;
    }
//
//    @Override
//    protected boolean isAuthRequired() {
//        return true;
//    }

}
