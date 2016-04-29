package com.pubnub.api.models.consumer.presence;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PNWhereNowResult {
    private List<String> channels;
}
