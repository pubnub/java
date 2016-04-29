package com.pubnub.api.models.server;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubscribeMetadata {

    @JsonProperty("t")
    private Long timetoken;

    @JsonProperty("r")
    private String region;

}
