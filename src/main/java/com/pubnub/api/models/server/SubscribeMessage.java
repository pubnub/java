package com.pubnub.api.models.server;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.pubnub.api.enums.MessageType;
import lombok.Builder;
import lombok.Data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Builder
@Data
public class SubscribeMessage {
    private static Set<MessageType> messageTypesSupportingEncryption = new HashSet<>(Arrays.asList(MessageType.MESSAGE01, MessageType.MESSAGE02, MessageType.FILE));

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
    private Integer messageType;

    @SerializedName("mt")
    private String type;

    @SerializedName("si")
    private String spaceId;

    public MessageType getMessageType() {
        return MessageType.valueByPnMessageType(messageType);
    }

    public boolean supportsEncryption() {
        return messageTypesSupportingEncryption.contains(getMessageType());
    }
}
