package com.pubnub.api.endpoints.presence;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.awaitility.Awaitility;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.endpoints.TestHarness;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;


public class LeaveTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Leave instance;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
        instance = new Leave(pubnub);
    }

    @Test
    public void subscribeChannelSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", \"action\": \"leave\"}")));

        instance.channels(Arrays.asList("coolChannel")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

    }

    @Test
    public void subscribeChannelsSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel,coolChannel2/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", \"action\": \"leave\"}")));

        instance.channels(Arrays.asList("coolChannel", "coolChannel2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }


    @Test
    public void subscribeChannelsWithGroupSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel,coolChannel2/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", \"action\": \"leave\"}")));

        instance.channels(Arrays.asList("coolChannel", "coolChannel2")).channelGroups(Arrays.asList("cg1")) .sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1", requests.get(0).queryParameter("channel-group").firstValue());
    }

    @Test
    public void subscribeChannelsWithGroupASync() throws PubNubException {

        final AtomicBoolean statusArrived = new AtomicBoolean();

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/coolChannel,coolChannel2/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", \"action\": \"leave\"}")));

        instance.channels(Arrays.asList("coolChannel", "coolChannel2")).channelGroups(Arrays.asList("cg1")) .async(new PNCallback<Boolean>() {
            @Override
            public void onResponse(Boolean result, PNStatus status) {
                assertEquals(status.getAffectedChannels().get(0), "coolChannel");
                assertEquals(status.getAffectedChannels().get(1), "coolChannel2");
                assertEquals(status.getAffectedChannelGroups().get(0), "cg1");
                statusArrived.set(true);
            }
        });


        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusArrived, org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void subscribeGroupsSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", \"action\": \"leave\"}")));

        instance.channelGroups(Arrays.asList("cg1", "cg2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1,cg2", requests.get(0).queryParameter("channel-group").firstValue());
    }

    @Test
    public void subscribeGroupSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", \"action\": \"leave\"}")));

        instance.channelGroups(Arrays.asList("cg1")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1", requests.get(0).queryParameter("channel-group").firstValue());
    }


}
