package com.pubnub.api.models.consumer.pubsub;

import com.google.gson.JsonElement;

import com.pubnub.api.PubNubError;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString(callSuper = true)
public class PNMessageResult extends MessageResult {

    @Nullable
    private final PubNubError error;

    public PNMessageResult(BasePubSubResult basePubSubResult, JsonElement message) {
        this(basePubSubResult, message, null);
    }

    public PNMessageResult(BasePubSubResult basePubSubResult, JsonElement message, @Nullable PubNubError error) {
        super(basePubSubResult, message);
        this.error = error;
    }
}

