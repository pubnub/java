package com.pubnub.api.endpoints.presence;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.models.consumer.presence.PNSetStateResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;


public class SetStateEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private SetState partialSetState;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        partialSetState = pubnub.setPresenceState();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void applyStateForChannelSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result =
                partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();

        assert result != null;

        assertEquals(pubnub.getMapper().elementToInt(result.getState(), "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(result.getState(), "status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }

    @Test
    public void applyStateForSomebodyElseChannelSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/someoneElseUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result =
                partialSetState.channels(Collections.singletonList("testChannel")).state(myState).uuid(
                        "someoneElseUUID").sync();

        assert result != null;

        assertEquals(pubnub.getMapper().elementToInt(result.getState(), "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(result.getState(), "status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }

    @Test
    public void applyStateForChannelsSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel," +
                "testChannel2/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result =
                partialSetState.channels(Arrays.asList("testChannel", "testChannel2")).state(myState).sync();

        assert result != null;

        assertEquals(pubnub.getMapper().elementToInt(result.getState(), "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(result.getState(), "status"), "online");
        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }

    @Test
    public void applyStateForChannelGroupSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result = partialSetState.channelGroups(Collections.singletonList("cg1")).state(myState).sync();

        assert result != null;

        assertEquals(pubnub.getMapper().elementToInt(result.getState(), "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(result.getState(), "status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

    }

    @Test
    public void applyStateForChannelGroupsSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result = partialSetState.channelGroups(Arrays.asList("cg1", "cg2")).state(myState).sync();

        assert result != null;

        assertEquals(pubnub.getMapper().elementToInt(result.getState(), "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(result.getState(), "status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1,cg2", requests.get(0).queryParameter("channel-group").firstValue());

    }

    @Test
    public void applyStateForMixSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result =
                partialSetState.channels(Collections.singletonList("ch1")).channelGroups(Arrays.asList("cg1", "cg2")).state(myState).sync();

        assert result != null;

        assertEquals(pubnub.getMapper().elementToInt(result.getState(), "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(result.getState(), "status"), "online");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

    }

    @Test(expected = PubNubException.class)
    public void applyNon200Sync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22status%22%3A%22oneline%22%2C%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}").withStatus(400)));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        partialSetState.channels(Collections.singletonList("ch1")).channelGroups(Arrays.asList("cg1", "cg2")).state(myState).sync();
    }

    @Test(expected = PubNubException.class)
    public void missingStateSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        partialSetState.channels(Collections.singletonList("testChannel")).sync();
    }

    @Test
    public void testIsAuthRequiredSuccessSync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        pubnub.getConfiguration().setAuthKey("myKey");
        partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @Test(expected = PubNubException.class)
    public void testNullSubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        pubnub.getConfiguration().setSubscribeKey(null);
        partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptySubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        pubnub.getConfiguration().setSubscribeKey("");
        partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();
    }

    @Test(expected = PubNubException.class)
    public void testChannelAndGroupMissingSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        partialSetState.state(myState).sync();
    }

    @Test(expected = PubNubException.class)
    public void testNullPayloadSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();
    }

}
