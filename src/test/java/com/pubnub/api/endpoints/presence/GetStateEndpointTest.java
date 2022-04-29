package com.pubnub.api.endpoints.presence;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNGetStateResult;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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


public class GetStateEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private PubNub pubnub;
    private GetState partialGetState;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        partialGetState = pubnub.getPresenceState();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void testOneChannelSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\"}, \"service\": \"Presence\"}")));


        PNGetStateResult result = partialGetState.channels(Collections.singletonList("testChannel")).uuid("sampleUUID"
        ).sync();

        assert result != null;

        JsonElement ch1Data = result.getStateByUUID().get("testChannel");
        assertEquals(pubnub.getMapper().elementToInt(ch1Data, "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(ch1Data, "status"), "online");
    }

    @Test
    public void testOneChannelWithoutUUIDSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\"}, \"service\": \"Presence\"}")));


        PNGetStateResult result = partialGetState.channels(Collections.singletonList("testChannel")).sync();

        assert result != null;

        JsonElement ch1Data = result.getStateByUUID().get("testChannel");
        assertEquals(pubnub.getMapper().elementToInt(ch1Data, "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(ch1Data, "status"), "online");
    }


    @Test(expected = PubNubException.class)
    public void testFailedPayloadSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": \"age\" : 20, " +
                        "\"status\" : \"online\"}, \"service\": \"Presence\"}")));

        partialGetState.channels(Collections.singletonList("testChannel")).uuid("sampleUUID").sync();
    }

    @Test
    public void testMultipleChannelSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1,ch2/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"ch1\": { " +
                        "\"age\" : 20, \"status\" : \"online\"}, \"ch2\": { \"age\": 100, \"status\": \"offline\" } " +
                        "}, \"service\": \"Presence\"}")));

        PNGetStateResult result = partialGetState.channels(Arrays.asList("ch1", "ch2")).uuid("sampleUUID").sync();

        assert result != null;

        JsonElement ch1Data = result.getStateByUUID().get("ch1");
        assertEquals(pubnub.getMapper().elementToInt(ch1Data, "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(ch1Data, "status"), "online");
        JsonElement ch2Data = result.getStateByUUID().get("ch2");
        assertEquals(pubnub.getMapper().elementToInt(ch2Data, "age"), 100);
        assertEquals(pubnub.getMapper().elementToString(ch2Data, "status"), "offline");
    }

    @Test
    public void testOneChannelGroupSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"chcg1\": {" +
                        " \"age\" : 20, \"status\" : \"online\"}, \"chcg2\": { \"age\": 100, \"status\": \"offline\" " +
                        "} }, \"service\": \"Presence\"}")));

        PNGetStateResult result =
                partialGetState.channelGroups(Collections.singletonList("cg1")).uuid("sampleUUID").sync();

        assert result != null;

        JsonElement ch1Data = result.getStateByUUID().get("chcg1");
        assertEquals(pubnub.getMapper().elementToInt(ch1Data, "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(ch1Data, "status"), "online");
        JsonElement ch2Data = result.getStateByUUID().get("chcg2");
        assertEquals(pubnub.getMapper().elementToInt(ch2Data, "age"), 100);
        assertEquals(pubnub.getMapper().elementToString(ch2Data, "status"), "offline");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1", requests.get(0).queryParameter("channel-group").firstValue());
    }

    @Test
    public void testManyChannelGroupSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"chcg1\": {" +
                        " \"age\" : 20, \"status\" : \"online\"}, \"chcg2\": { \"age\": 100, \"status\": \"offline\" " +
                        "} }, \"service\": \"Presence\"}")));

        PNGetStateResult result = partialGetState.channelGroups(Arrays.asList("cg1", "cg2")).uuid("sampleUUID").sync();

        assert result != null;

        JsonElement ch1Data = result.getStateByUUID().get("chcg1");
        assertEquals(pubnub.getMapper().elementToInt(ch1Data, "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(ch1Data, "status"), "online");
        JsonElement ch2Data = result.getStateByUUID().get("chcg2");
        assertEquals(pubnub.getMapper().elementToInt(ch2Data, "age"), 100);
        assertEquals(pubnub.getMapper().elementToString(ch2Data, "status"), "offline");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1,cg2", requests.get(0).queryParameter("channel-group").firstValue());
    }

    @Test
    public void testCombinationSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"chcg1\": {" +
                        " \"age\" : 20, \"status\" : \"online\"}, \"chcg2\": { \"age\": 100, \"status\": \"offline\" " +
                        "} }, \"service\": \"Presence\"}")));

        PNGetStateResult result =
                partialGetState.channels(Collections.singletonList("ch1")).channelGroups(Arrays.asList("cg1", "cg2")).uuid("sampleUUID").sync();

        assert result != null;

        JsonElement ch1Data = result.getStateByUUID().get("chcg1");
        assertEquals(pubnub.getMapper().elementToInt(ch1Data, "age"), 20);
        assertEquals(pubnub.getMapper().elementToString(ch1Data, "status"), "online");
        JsonElement ch2Data = result.getStateByUUID().get("chcg2");
        assertEquals(pubnub.getMapper().elementToInt(ch2Data, "age"), 100);
        assertEquals(pubnub.getMapper().elementToString(ch2Data, "status"), "offline");

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1,cg2", requests.get(0).queryParameter("channel-group").firstValue());

    }

    @Test(expected = PubNubException.class)
    public void testMissingChannelAndGroupSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\"}, \"service\": \"Presence\"}")));
        partialGetState.uuid("sampleUUID").sync();
    }

    @Test
    public void testIsAuthRequiredSuccessSync() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\"}, \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setAuthKey("myKey");
        partialGetState.channels(Collections.singletonList("testChannel")).uuid("sampleUUID").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @Test
    public void testOperationTypeSuccessAsync() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\"}, \"service\": \"Presence\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);

        partialGetState.channels(Collections.singletonList("testChannel")).uuid("sampleUUID").async(new PNCallback<PNGetStateResult>() {
            @Override
            public void onResponse(PNGetStateResult result, @NotNull PNStatus status) {
                if (status != null && status.getOperation() == PNOperationType.PNGetState) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test(expected = PubNubException.class)
    public void testNullSubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\"}, \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setSubscribeKey(null);
        partialGetState.channels(Collections.singletonList("testChannel")).uuid("sampleUUID").sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptySubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/sampleUUID"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\"}, \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setSubscribeKey("");
        partialGetState.channels(Collections.singletonList("testChannel")).uuid("sampleUUID").sync();
    }
}
