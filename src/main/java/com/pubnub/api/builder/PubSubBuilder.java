package com.pubnub.api.builder;


import com.pubnub.api.managers.SubscriptionManager;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class PubSubBuilder {

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected List<String> channelSubscriptions;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected List<String> channelGroupSubscriptions;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected SubscriptionManager subscriptionManager;

    public PubSubBuilder(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
        this.channelSubscriptions = new ArrayList<>();
        this.channelGroupSubscriptions = new ArrayList<>();
    }


    public PubSubBuilder channels(List<String> channel) {
        channelSubscriptions.addAll(channel);
        return this;
    }

    public PubSubBuilder channelGroups(List<String> channelGroup) {
        channelGroupSubscriptions.addAll(channelGroup);
        return this;
    }

    public abstract void execute();

}
