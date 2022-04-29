package com.pubnub.api.endpoints.channel_groups;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsAllChannelsResult;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AllChannelsChannelGroupEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private AllChannelsChannelGroup partialAllChannelsChannelGroup;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        partialAllChannelsChannelGroup = pubnub.listChannelsForChannelGroup();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void testSyncSuccess() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"ChannelGroups\"}")));

        PNChannelGroupsAllChannelsResult response = partialAllChannelsChannelGroup.channelGroup("groupA").sync();

        assert response != null;

        assertThat(response.getChannels(), org.hamcrest.Matchers.contains("a", "b"));
    }

    @Test(expected = PubNubException.class)
    public void testSyncMissingGroup() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"ChannelGroups\"}")));

        partialAllChannelsChannelGroup.sync();
    }

    @Test(expected = PubNubException.class)
    public void testSyncEmptyGroup() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"ChannelGroups\"}")));

        partialAllChannelsChannelGroup.channelGroup("").sync();
    }

    @Test(expected = PubNubException.class)
    public void testNullPayload() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": " +
                        "\"ChannelGroups\"}")));

        PNChannelGroupsAllChannelsResult response = partialAllChannelsChannelGroup.channelGroup("groupA").sync();

        assert response != null;

        assertThat(response.getChannels(), org.hamcrest.Matchers.contains("a", "b"));
    }

    @Test
    public void testIsAuthRequiredSuccessSync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"ChannelGroups\"}")));

        pubnub.getConfiguration().setAuthKey("myKey");
        partialAllChannelsChannelGroup.channelGroup("groupA").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @Test
    public void testOperationTypeSuccessAsync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"ChannelGroups\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);

        partialAllChannelsChannelGroup.channelGroup("groupA").async(new PNCallback<PNChannelGroupsAllChannelsResult>() {
            @Override
            public void onResponse(PNChannelGroupsAllChannelsResult result, @NotNull PNStatus status) {
                if (status != null && status.getOperation() == PNOperationType.PNChannelsForGroupOperation) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }


}
