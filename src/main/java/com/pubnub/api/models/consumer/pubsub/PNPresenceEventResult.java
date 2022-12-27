package com.pubnub.api.models.consumer.pubsub;

import com.google.gson.JsonElement;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@Builder
@ToString
@Data
public class PNPresenceEventResult implements PNEvent {

    private String event;

    private String uuid;
    private Long timestamp;
    private Integer occupancy;
    private JsonElement state;

    @Deprecated
    private String subscribedChannel;
    @Deprecated
    private String actualChannel;

    private String channel;
    private String subscription;

    private Long timetoken;
    private Object userMetadata;
    private List<String> join;
    private List<String> leave;
    private List<String> timeout;
    private Boolean hereNowRefresh;

}
