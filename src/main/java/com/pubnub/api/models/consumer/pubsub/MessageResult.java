package com.pubnub.api.models.consumer.pubsub;

import com.google.gson.JsonElement;
import com.pubnub.api.SpaceId;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class MessageResult extends BasePubSubResult {

    private final JsonElement message;
    private final String type;
    private final SpaceId spaceId;

    public MessageResult(BasePubSubResult basePubSubResult, JsonElement message, String type, SpaceId spaceId) {
        super(basePubSubResult);
        this.message = message;
        this.type = type;
        this.spaceId = spaceId;
    }
}
