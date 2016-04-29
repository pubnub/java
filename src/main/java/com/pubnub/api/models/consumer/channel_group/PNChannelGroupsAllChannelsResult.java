package com.pubnub.api.models.consumer.channel_group;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PNChannelGroupsAllChannelsResult {
    private List<String> channels;
}
