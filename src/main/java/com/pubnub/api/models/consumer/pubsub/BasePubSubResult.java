package com.pubnub.api.models.consumer.pubsub;

import com.google.gson.JsonElement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
@AllArgsConstructor
public class BasePubSubResult implements PNEvent {

    @Deprecated
    private String subscribedChannel;
    @Deprecated
    private String actualChannel;

    private String channel;
    private String subscription;

    private Long timetoken;

    private JsonElement userMetadata;

    private String publisher;

    protected BasePubSubResult(BasePubSubResult basePubSubResult) {
        this.subscribedChannel = basePubSubResult.subscribedChannel;
        this.actualChannel = basePubSubResult.actualChannel;
        this.channel = basePubSubResult.channel;
        this.subscription = basePubSubResult.subscription;
        this.timetoken = basePubSubResult.timetoken;
        this.userMetadata = basePubSubResult.userMetadata;
        this.publisher = basePubSubResult.publisher;
    }
}
