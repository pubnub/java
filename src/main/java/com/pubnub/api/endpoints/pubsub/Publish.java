package com.pubnub.api.endpoints.pubsub;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.publishKT.ConfigurationNeededForEndpoint;
import com.pubnub.api.endpoints.publishKT.ConfigurationNeededForPublish;
import com.pubnub.api.endpoints.publishKT.JsonMapperManagerForEndpoint;
import com.pubnub.api.endpoints.publishKT.PNInstanceIdProvider;
import com.pubnub.api.endpoints.publishKT.PublishServiceExternal;
import com.pubnub.api.endpoints.publishKT.SequenceManagerExternal;
import com.pubnub.api.endpoints.publishKT.TelemetryManagerExternal;
import com.pubnub.api.endpoints.publishKT.ToJsonMapper;
import com.pubnub.api.endpoints.publishKT.TokenRetriever;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Accessors(chain = true, fluent = true)
public class Publish implements RemoteAction<PNPublishResult> {
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

    private PublishServiceExternal publishServiceExternal;
    private ConfigurationNeededForPublish configurationNeededForPublish;
    private ToJsonMapper toJsonMapper;
    private SequenceManagerExternal sequenceManagerExternal;
    private TelemetryManagerExternal telemetryManagerExternal;
    private ConfigurationNeededForEndpoint configurationNeededForEndpoint;
    private PNInstanceIdProvider pnInstanceIdProvider;
    private TokenRetriever tokenRetriever;
    private JsonMapperManagerForEndpoint jsonMapperManagerForEndpoint;

    public Publish(PubNub pubnub,
                   PublishServiceExternal publishServiceExternal,
                   ConfigurationNeededForPublish configurationNeededForPublish,
                   ToJsonMapper toJsonMapper,
                   SequenceManagerExternal sequenceManagerExternal,
                   TelemetryManagerExternal telemetryManagerExternal,
                   ConfigurationNeededForEndpoint configurationNeededForEndpoint,
                   PNInstanceIdProvider pnInstanceIdProvider,
                   TokenRetriever tokenRetriever,
                   JsonMapperManagerForEndpoint jsonMapperManagerForEndpoint) {

        this.replicate = true;
        this.pubnub = pubnub;
        this.publishServiceExternal = publishServiceExternal;
        this.configurationNeededForPublish = configurationNeededForPublish;
        this.toJsonMapper = toJsonMapper;
        this.sequenceManagerExternal = sequenceManagerExternal;
        this.telemetryManagerExternal = telemetryManagerExternal;
        this.configurationNeededForEndpoint = configurationNeededForEndpoint;
        this.pnInstanceIdProvider = pnInstanceIdProvider;
        this.tokenRetriever = tokenRetriever;
        this.jsonMapperManagerForEndpoint = jsonMapperManagerForEndpoint;
    }


    @Override
    public @Nullable PNPublishResult sync() throws PubNubException {
        //dodatkowo queryParam z Endpointa jest wypiasny w dokumentacji, ale nie da się go użyć w Publishu bo nie jest częścią buildera(nie zwraca publish)
        Boolean usePostKT = usePOST != null ? usePOST : false;

        com.pubnub.api.endpoints.publishKT.Publish publishKT = new com.pubnub.api.endpoints.publishKT.Publish(
                message, channel, meta, shouldStore, usePostKT, replicate, ttl, publishServiceExternal, configurationNeededForPublish,
                toJsonMapper, sequenceManagerExternal, telemetryManagerExternal, configurationNeededForEndpoint,
                pnInstanceIdProvider, tokenRetriever, jsonMapperManagerForEndpoint);


        com.pubnub.api.endpoints.publishKT.PNPublishResult pnPublishResultKT = publishKT.sync();

        return PNPublishResult.builder().timetoken(pnPublishResultKT.getTimetoken()).build(); //zwrot z Kotlina
    }

    @Override
    public void async(@NotNull PNCallback<PNPublishResult> callback) {

        Boolean usePostKT = usePOST != null ? usePOST : false;

        com.pubnub.api.endpoints.publishKT.Publish publishKT = new com.pubnub.api.endpoints.publishKT.Publish(
                message, channel, meta, shouldStore, usePostKT, replicate, ttl, publishServiceExternal, configurationNeededForPublish,
                toJsonMapper, sequenceManagerExternal, telemetryManagerExternal, configurationNeededForEndpoint,
                pnInstanceIdProvider, tokenRetriever, jsonMapperManagerForEndpoint);

        publishKT.async((pnPublishResultKT, statusKT) -> {
            PNPublishResult pnPublishResult = PNPublishResult.builder().timetoken(pnPublishResultKT.getTimetoken()).build();
            //ToDO here we should convert statusKT to pnStatus in java
            PNStatus pnStatus = PNStatus.builder().category(PNStatusCategory.PNAcknowledgmentCategory).build();
            callback.onResponse(pnPublishResult, pnStatus);
            return null;
        });
    }

    @Override
    public void retry() {
        //Todo
    }

    @Override
    public void silentCancel() {
        //Todo
    }
}
