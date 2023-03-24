package com.pubnub.api.models.consumer.pubsub;

import com.google.gson.JsonElement;
import com.pubnub.api.SpaceId;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class PNMessageResult extends MessageResult {
    public PNMessageResult(BasePubSubResult basePubSubResult, JsonElement message, String type, SpaceId spaceId) {
        super(basePubSubResult, message, type, spaceId);
    }
}

