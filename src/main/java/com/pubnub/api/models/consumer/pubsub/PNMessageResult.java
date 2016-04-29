package com.pubnub.api.models.consumer.pubsub;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PNMessageResult {

    private Object message;

    private String subscribedChannel;
    private String actualChannel;
    private Long timetoken;
    private Object userMetadata;

}
