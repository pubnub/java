package com.pubnub.api.models.server.access_manager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerKeyData;
import lombok.Getter;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class AccessManagerAuditPayload {

    String level;
    @JsonProperty("subscribe_key")
    String subscribeKey;

    String channel;

    @JsonProperty("channel-group")
    String channelGroup;

    @JsonProperty("auths")
    Map<String, PNAccessManagerKeyData> authKeys;


}
