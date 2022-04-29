package com.pubnub.api.endpoints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.presence.Heartbeat;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;

public class HeartbeatEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private Heartbeat partialHeartbeat;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        RetrofitManager retrofitManager = new RetrofitManager(pubnub);
        partialHeartbeat = new Heartbeat(pubnub, null, retrofitManager, new TokenManager());
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void testSuccessOneChannel() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.channels(Arrays.asList("ch1")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        assertEquals("myUUID", request.queryParameter("uuid").firstValue());
        assertEquals("123", request.queryParameter("heartbeat").firstValue());

    }

    @Test
    public void testSuccessManyChannels() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1,ch2/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.channels(Arrays.asList("ch1", "ch2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        assertEquals("myUUID", request.queryParameter("uuid").firstValue());
        assertEquals("123", request.queryParameter("heartbeat").firstValue());
    }

    @Test
    public void testSuccessOneChannelGroup() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.channelGroups(Arrays.asList("cg1")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        assertEquals("myUUID", request.queryParameter("uuid").firstValue());
        assertEquals("cg1", request.queryParameter("channel-group").firstValue());
        assertEquals("123", request.queryParameter("heartbeat").firstValue());
    }

    @Test
    public void testSuccessManyChannelGroups() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.channelGroups(Arrays.asList("cg1", "cg2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        assertEquals("myUUID", request.queryParameter("uuid").firstValue());
        assertEquals("cg1,cg2", request.queryParameter("channel-group").firstValue());
        assertEquals("123", request.queryParameter("heartbeat").firstValue());

    }

    @Test(expected = PubNubException.class)
    public void testMissingChannelAndGroupSync() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialHeartbeat.sync();
    }

    @Test
    public void testIsAuthRequiredSuccessSync() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setAuthKey("myKey");
        partialHeartbeat.channels(Arrays.asList("ch1")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @Test(expected = PubNubException.class)
    public void testNullSubKeySync() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setSubscribeKey(null);
        partialHeartbeat.channels(Arrays.asList("ch1")).sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptySubKeySync() throws PubNubException, InterruptedException {
        pubnub.getConfiguration().setPresenceTimeout(123);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setSubscribeKey("");
        partialHeartbeat.channels(Arrays.asList("ch1")).sync();
    }

}
