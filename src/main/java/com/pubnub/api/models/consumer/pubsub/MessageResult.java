package com.pubnub.api.models.consumer.pubsub;

import com.google.gson.JsonElement;
import com.pubnub.api.MessageType;
import com.pubnub.api.SpaceId;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class MessageResult extends BasePubSubResult {

    private final JsonElement message;
    private final MessageType messageType;
    private final SpaceId spaceId;

    public MessageResult(BasePubSubResult basePubSubResult, JsonElement message, MessageType messageType, SpaceId spaceId) {
        super(basePubSubResult);
        this.message = message;
        this.messageType = messageType;
        this.spaceId = spaceId;
    }
}

