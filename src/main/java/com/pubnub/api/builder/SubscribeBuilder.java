package com.pubnub.api.builder;

import com.pubnub.api.builder.dto.SubscribeOperation;
import com.pubnub.api.managers.SubscriptionManager;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Accessors(chain = true, fluent =  true)
public class SubscribeBuilder extends PubSubBuilder  {

    /**
     * Allow users to specify if they would also like to include the presence channels for those subscriptions.
     */
    @Setter(AccessLevel.NONE)
    private boolean presenceEnabled;

    /**
     * Allow users to subscribe with a custom timetoken.
     */
    @Setter(AccessLevel.NONE)
    private Long timetoken;

    public SubscribeBuilder(final SubscriptionManager subscriptionManager) {
        super(subscriptionManager);
    }

    public SubscribeBuilder withPresence() {
        this.presenceEnabled = true;
        return this;
    }

    public SubscribeBuilder withTimetoken(final Long timetoken) {
        this.timetoken = timetoken;
        return this;
    }

    public void execute() {
        SubscribeOperation subscribeOperation = SubscribeOperation.builder()
                .channels(channelSubscriptions)
                .channelGroups(channelGroupSubscriptions)
                .timetoken(timetoken)
                .presenceEnabled(presenceEnabled)
                .build();

        this.subscriptionManager.adaptSubscribeBuilder(subscribeOperation);
    }

    public SubscribeBuilder channels(final List<String> channels) {
        return (SubscribeBuilder) super.channels(channels);
    }

    public SubscribeBuilder channelGroups(final List<String> channelGroups) {
        return (SubscribeBuilder) super.channelGroups(channelGroups);
    }

}
