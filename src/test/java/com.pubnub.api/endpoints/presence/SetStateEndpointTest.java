package com.pubnub.api.endpoints.presence;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.presence.PNSetStateResult;
import com.pubnub.api.endpoints.TestHarness;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;


public class SetStateEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private SetState partialSetState;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);

        partialSetState = pubnub.setPresenceState();
    }

    @Test
    public void applyStateForChannelSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : 20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        PNSetStateResult result = partialSetState.channels(Arrays.asList("testChannel")).state(Arrays.asList("s1", "s2", "s3")).sync();
        assertEquals(result.getState().get("age"), 20);
        assertEquals(result.getState().get("status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("%5B%22s1%22%2C%22s2%22%2C%22s3%22%5D", requests.get(0).queryParameter("state").firstValue());

    }

    @Test
    public void applyStateForChannelsSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel,testChannel2/uuid/myUUID/data"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : 20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        PNSetStateResult result = partialSetState.channels(Arrays.asList("testChannel", "testChannel2")).state(Arrays.asList("s1", "s2", "s3")).sync();
        assertEquals(result.getState().get("age"), 20);
        assertEquals(result.getState().get("status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("%5B%22s1%22%2C%22s2%22%2C%22s3%22%5D", requests.get(0).queryParameter("state").firstValue());
    }

    @Test
    public void applyStateForChannelGroupSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/myUUID/data"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : 20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        PNSetStateResult result = partialSetState.channelGroups(Arrays.asList("cg1")).state(Arrays.asList("s1", "s2", "s3")).sync();

        assertEquals(result.getState().get("age"), 20);
        assertEquals(result.getState().get("status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("%5B%22s1%22%2C%22s2%22%2C%22s3%22%5D", requests.get(0).queryParameter("state").firstValue());
        assertEquals("cg1", requests.get(0).queryParameter("channel-group").firstValue());
    }

    @Test
    public void applyStateForChannelGroupsSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/myUUID/data"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : 20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        PNSetStateResult result = partialSetState.channelGroups(Arrays.asList("cg1", "cg2")).state(Arrays.asList("s1", "s2", "s3")).sync();

        assertEquals(result.getState().get("age"), 20);
        assertEquals(result.getState().get("status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("%5B%22s1%22%2C%22s2%22%2C%22s3%22%5D", requests.get(0).queryParameter("state").firstValue());
        assertEquals("cg1,cg2", requests.get(0).queryParameter("channel-group").firstValue());

    }

    @Test
    public void applyStateForMixSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/myUUID/data"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : 20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        PNSetStateResult result = partialSetState.channels(Arrays.asList("ch1")).channelGroups(Arrays.asList("cg1", "cg2")).state(Arrays.asList("s1", "s2", "s3")).sync();

        assertEquals(result.getState().get("age"), 20);
        assertEquals(result.getState().get("status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("%5B%22s1%22%2C%22s2%22%2C%22s3%22%5D", requests.get(0).queryParameter("state").firstValue());
        assertEquals("cg1,cg2", requests.get(0).queryParameter("channel-group").firstValue());

    }

    @org.junit.Test(expected=PubNubException.class)
    public void applyNon200Sync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/myUUID/data"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : 20, \"status\" : \"online\" }, \"service\": \"Presence\"}").withStatus(400)));


        PNSetStateResult result = partialSetState.channels(Arrays.asList("ch1")).channelGroups(Arrays.asList("cg1", "cg2")).state(Arrays.asList("s1", "s2", "s3")).sync();
    }

}
