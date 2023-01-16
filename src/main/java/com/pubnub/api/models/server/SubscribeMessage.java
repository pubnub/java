package com.pubnub.api.models.server;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.pubnub.api.enums.PNMessageType;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
public class SubscribeMessage {
    private static Set<PNMessageType> pnMessageTypesSupportingEncryption = new HashSet<>(Arrays.asList(PNMessageType.MESSAGE01, PNMessageType.MESSAGE02, PNMessageType.FILE));

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

    public PNMessageType getPnMessageType() {
        return PNMessageType.valueByPnMessageType(pnMessageType);
    }

    public boolean supportsEncryption() {
        return pnMessageTypesSupportingEncryption.contains(getPnMessageType());
    }
}
