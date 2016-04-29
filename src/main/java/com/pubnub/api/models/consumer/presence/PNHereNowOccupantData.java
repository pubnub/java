package com.pubnub.api.models.consumer.presence;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PNHereNowOccupantData {
    String uuid;
    Object state;
}
