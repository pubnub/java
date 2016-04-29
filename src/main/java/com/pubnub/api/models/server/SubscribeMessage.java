package com.pubnub.api.models.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class SubscribeMessage {

    @JsonProperty("a")
    private String shard;

    @JsonProperty("b")
    private String subscriptionMatch; // will contain channel group; or channel if user subscribed to channel

    @JsonProperty("c")
    private String channel;

    @JsonProperty("d")
    private Object payload;

    // TODO: figure me out
    //@JsonProperty("ear")
    //private String payload;

    @JsonProperty("f")
    private String flags;

    @JsonProperty("i")
    private String issuingClientId;

    @JsonProperty("k")
    private String subscribeKey;

    //@JsonProperty("s")
    //private String sequenceNumber;

    //@JsonProperty("o")
    //private String originationTimetoken;

    //@JsonProperty("p")
    //private String publishTimetoken;

    //@JsonProperty("r")
    //private Object replicationMap;

    //@JsonProperty("u")
    //private String userMetadata;

    //@JsonProperty("w")
    //private String waypointList;
}
