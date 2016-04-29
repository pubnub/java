package com.pubnub.api.models.consumer.history;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PNHistoryResult {

    private List<PNHistoryItemResult> messages;
    private Long startTimeToken;
    private Long endTimeToken;

}
