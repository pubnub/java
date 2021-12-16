package com.pubnub.api.endpoints.channel_groups;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsAddChannelResult;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AddChannelChannelGroupEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private AddChannelChannelGroup partialAddChannelChannelGroup;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        partialAddChannelChannelGroup = pubnub.addChannelsToChannelGroup();
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
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {} , " +
                        "\"service\": \"ChannelGroups\"}")));

        PNChannelGroupsAddChannelResult response =
                partialAddChannelChannelGroup.channelGroup("groupA").channels(Arrays.asList("ch1", "ch2")).sync();

        assertNotNull(response);
    }

    @Test(expected = PubNubException.class)
    public void testSyncGroupMissing() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {} , " +
                        "\"service\": \"ChannelGroups\"}")));

        partialAddChannelChannelGroup.channels(Arrays.asList("ch1", "ch2")).sync();
    }

    @Test(expected = PubNubException.class)
    public void testSyncGroupIsEmpty() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {} , " +
                        "\"service\": \"ChannelGroups\"}")));

        partialAddChannelChannelGroup.channelGroup("").channels(Arrays.asList("ch1", "ch2")).sync();
    }

    @Test(expected = PubNubException.class)
    public void testSyncChannelMissing() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {} , " +
                        "\"service\": \"ChannelGroups\"}")));

        partialAddChannelChannelGroup.channelGroup("groupA").sync();
    }

    @Test
    public void testIsAuthRequiredSuccessSync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {} , " +
                        "\"service\": \"ChannelGroups\"}")));

        pubnub.getConfiguration().setAuthKey("myKey");
        partialAddChannelChannelGroup.channelGroup("groupA").channels(Arrays.asList("ch1", "ch2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @Test
    public void testOperationTypeSuccessAsync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {} , " +
                        "\"service\": \"ChannelGroups\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);

        partialAddChannelChannelGroup.channelGroup("groupA").channels(Arrays.asList("ch1", "ch2")).async(new PNCallback<PNChannelGroupsAddChannelResult>() {
            @Override
            public void onResponse(PNChannelGroupsAddChannelResult result, @NotNull PNStatus status) {
                if (status != null && status.getOperation() == PNOperationType.PNAddChannelsToGroupOperation) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test
    public void testErrorBodyForbiden() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v1/channel-registration/sub-key/mySubscribeKey/channel-group/groupA"))
                .willReturn(aResponse().withStatus(403).withBody("{\"status\": 403, \"message\": \"OK\", \"payload\":" +
                        " {} , \"service\": \"ChannelGroups\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);

        partialAddChannelChannelGroup.channelGroup("groupA").channels(Arrays.asList("ch1", "ch2")).async(new PNCallback<PNChannelGroupsAddChannelResult>() {
            @Override
            public void onResponse(PNChannelGroupsAddChannelResult result, @NotNull PNStatus status) {
                if (status != null && status.getOperation() == PNOperationType.PNAddChannelsToGroupOperation) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

}
