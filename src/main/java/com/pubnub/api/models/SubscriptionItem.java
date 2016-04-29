package com.pubnub.api.models;

import com.pubnub.api.enums.SubscriptionType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SubscriptionItem {

    private String name;
    private SubscriptionType type;
    private boolean withPresence;
    private Object state;

}
