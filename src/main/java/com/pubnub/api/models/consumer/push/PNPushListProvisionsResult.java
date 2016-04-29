package com.pubnub.api.models.consumer.push;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PNPushListProvisionsResult {

    private List<String> channels;

}
