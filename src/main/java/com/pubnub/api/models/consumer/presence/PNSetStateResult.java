package com.pubnub.api.models.consumer.presence;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class PNSetStateResult {

    Map<String, Object> state;

}
