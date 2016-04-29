package com.pubnub.api.models.consumer.channel_group;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class PNChannelGroupsListAllResult {
    private List<String> groups;
}
