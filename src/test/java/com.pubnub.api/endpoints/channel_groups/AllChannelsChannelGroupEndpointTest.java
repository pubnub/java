package com.pubnub.api.endpoints.channel_groups;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsAllChannelsResult;
import com.pubnub.api.endpoints.TestHarness;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertThat;

public class AllChannelsChannelGroupEndpointTest extends TestHarness {
    private AllChannelsChannelGroup partialAllChannelsChannelGroup;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void beforeEach() throws IOException {
        partialAllChannelsChannelGroup = this.createPubNubInstance(8080).listChannelsForChannelGroup();
    }

    @org.junit.Test
    public void testSyncSuccess() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}, \"service\": \"ChannelGroups\"}")));

        PNChannelGroupsAllChannelsResult response = partialAllChannelsChannelGroup.channelGroup("groupA").sync();
        assertThat(response.getChannels(), org.hamcrest.Matchers.contains("a", "b"));
    }

}
