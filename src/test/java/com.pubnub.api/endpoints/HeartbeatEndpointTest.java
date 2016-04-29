package com.pubnub.api.endpoints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.presence.Heartbeat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class HeartbeatEndpointTest extends TestHarness {

    private Heartbeat partialHeartbeat;
    private PubNub pubnub;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
        partialHeartbeat = new Heartbeat(pubnub);
    }

    @org.junit.Test
    public void testSuccessOneChannel() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.channels(Arrays.asList("ch1")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        Assert.assertEquals("myUUID", request.queryParameter("uuid").firstValue());
        Assert.assertEquals("123", request.queryParameter("heartbeat").firstValue());

    }

    @org.junit.Test
    public void testSuccessManyChannels() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1,ch2/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.channels(Arrays.asList("ch1", "ch2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        Assert.assertEquals("myUUID", request.queryParameter("uuid").firstValue());
        Assert.assertEquals("123", request.queryParameter("heartbeat").firstValue());
    }

    @org.junit.Test
    public void testSuccessOneChannelGroup() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.channelGroups(Arrays.asList("cg1")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        Assert.assertEquals("myUUID", request.queryParameter("uuid").firstValue());
        Assert.assertEquals("cg1", request.queryParameter("channel-group").firstValue());
        Assert.assertEquals("123", request.queryParameter("heartbeat").firstValue());
    }

    @org.junit.Test
    public void testSuccessManyChannelGroups() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.channelGroups(Arrays.asList("cg1", "cg2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        Assert.assertEquals("myUUID", request.queryParameter("uuid").firstValue());
        Assert.assertEquals("cg1,cg2", request.queryParameter("channel-group").firstValue());
        Assert.assertEquals("123", request.queryParameter("heartbeat").firstValue());

    }

    @org.junit.Test
    public void testSuccessIncludeState() throws PubNubException, InterruptedException {
        Map<String, String> state = new HashMap<>();
        state.put("CH1", "this-is-channel1");
        state.put("CH2", "this-is-channel2");

        pubnub.getConfiguration().setPresenceTimeout(123);


        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1,ch2/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.channels(Arrays.asList("ch1", "ch2")).state(state).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        Assert.assertEquals("myUUID", request.queryParameter("uuid").firstValue());
        Assert.assertEquals("123", request.queryParameter("heartbeat").firstValue());
        Assert.assertEquals("%7B%22CH2%22%3A%22this-is-channel2%22%2C%22CH1%22%3A%22this-is-channel1%22%7D", request.queryParameter("state").firstValue());

    }
}
