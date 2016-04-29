package com.pubnub.api.models.consumer.pubsub;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PNPresenceEventResult {

    private String event;

    private String uuid;
    private Long timestamp;
    private Integer occupancy;

    private String subscribedChannel;
    private String actualChannel;
    private Long timetoken;
    private Object userMetadata;

}
