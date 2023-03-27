package com.pubnub.api.models.consumer.pubsub;

import com.google.gson.JsonElement;
import com.pubnub.api.SpaceId;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class PNSignalResult extends MessageResult {
    public PNSignalResult(BasePubSubResult basePubSubResult, JsonElement message, String type, SpaceId spaceId) {
        super(basePubSubResult, message, type, spaceId);
    }
}