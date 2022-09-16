package com.pubnub.api.models.server;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.pubnub.api.workers.SubscribeMessageProcessor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SubscribeMessage {

    @SerializedName("a")
    private String shard;

    @SerializedName("b")
    private String subscriptionMatch;

    @SerializedName("c")
    private String channel;

    @SerializedName("d")
    private JsonElement payload;

    // TODO: figure me out
    //@SerializedName("ear")
    //private String payload;

    @SerializedName("f")
    private String flags;

    @SerializedName("i")
    private String issuingClientId;

    @SerializedName("k")
    private String subscribeKey;

    //@SerializedName("s")
    //private String sequenceNumber;

    @SerializedName("o")
    private OriginationMetaData originationMetadata;

    @SerializedName("p")
    private PublishMetaData publishMetaData;

    //@SerializedName("r")
    //private Object replicationMap;

    @SerializedName("u")
    private JsonElement userMetadata;

    //@SerializedName("w")
    //private String waypointList;

    @SerializedName("e")
    private Integer type;

    public boolean supportsEncryption() {
        return type == null || type == SubscribeMessageProcessor.TYPE_MESSAGE || type == SubscribeMessageProcessor.TYPE_FILES;
    }

}
