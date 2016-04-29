package com.pubnub.api.models.consumer.access_manager;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class PNAccessManagerGrantResult {

    String level;
    int ttl;
    String subscribeKey;

    Map<String, Map<String,PNAccessManagerKeyData>> channels;

    Map<String, Map<String,PNAccessManagerKeyData>> channelGroups;

}
