package com.pubnub.api.models.consumer.history;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PNHistoryItemResult {

    @Getter private Long timetoken;
    @Getter private Object entry;

}
