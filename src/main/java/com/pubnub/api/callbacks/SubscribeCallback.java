package com.pubnub.api.callbacks;

import com.pubnub.api.PubNub;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.PNStatus;

public abstract class SubscribeCallback {
    public abstract void status(PubNub pubnub, PNStatus status);
    public abstract void message(PubNub pubnub, PNMessageResult message);
    public abstract void presence(PubNub pubnub, PNPresenceEventResult presence);
}
