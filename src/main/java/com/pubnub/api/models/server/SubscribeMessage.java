package com.pubnub.api.models.server;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.pubnub.api.enums.PNMessageType;
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

    @SerializedName("f")
    private String flags;

    @SerializedName("i")
    private String issuingClientId;

    @SerializedName("k")
    private String subscribeKey;

    @SerializedName("o")
    private OriginationMetaData originationMetadata;

    @SerializedName("p")
    private PublishMetaData publishMetaData;

    @SerializedName("u")
    private JsonElement userMetadata;

    @SerializedName("e")
    private Integer pnMessageType;

    @SerializedName("mt")
    private String userMessageType;

    @SerializedName("si")
    private String spaceId;

    public boolean supportsEncryption() {
        //should be "==" instead of "equals" for PNMessageType.MESSAGE01.getEValueFromServer() because its value is null
        return pnMessageType == PNMessageType.MESSAGE01.getEValueFromServer() || pnMessageType.equals(PNMessageType.MESSAGE02.getEValueFromServer()) || pnMessageType.equals(PNMessageType.FILE.getEValueFromServer());
    }

}
