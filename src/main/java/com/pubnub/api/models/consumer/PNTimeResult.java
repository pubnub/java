package com.pubnub.api.models.consumer;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PNTimeResult {
    private Long timetoken;
}
