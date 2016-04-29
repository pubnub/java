package com.pubnub.api.models.consumer.access_manager;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class PNAccessManagerAuditResult {

    private String level;
    private String subscribeKey;

    private String channel;

    private String channelGroup;

    private Map<String, PNAccessManagerKeyData> authKeys;
}
