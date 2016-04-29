package com.pubnub.api.builder.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SubscribeOperation {

    private List<String> channels;
    private List<String> channelGroups;
    private boolean presenceEnabled;
    private Long timetoken;

}
