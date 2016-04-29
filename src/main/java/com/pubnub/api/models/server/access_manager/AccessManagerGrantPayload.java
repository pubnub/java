package com.pubnub.api.models.server.access_manager;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerKeyData;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerKeysData;
import lombok.Getter;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class AccessManagerGrantPayload {

    String level;
    int ttl;
    @JsonProperty("subscribe_key")
    String subscribeKey;

    Map<String, PNAccessManagerKeysData> channels;

    @JsonProperty("channel-groups")
    JsonNode channelGroups;

    @JsonProperty("auths")
    Map<String, PNAccessManagerKeyData> authKeys;

    String channel;

}
