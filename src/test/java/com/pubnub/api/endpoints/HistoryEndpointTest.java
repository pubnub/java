package com.pubnub.api.endpoints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class HistoryEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT), false);

    private History partialHistory;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        partialHistory = pubnub.history();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void testSyncDisabled() {
        String payload = "[[\"Use of the history API requires the Storage & Playback add-on which is not enabled for " +
                "this subscribe key. Login to your PubNub Dashboard Account and ADD the Storage & Playback add-on. " +
                "Contact support@pubnub.com if you require further assistance.\"],0,0]";

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(payload)));

        try {
            partialHistory.channel("niceChannel").sync();
        } catch (PubNubException ex) {
            assertEquals("History is disabled", ex.getErrormsg());
        }
    }

    @Test
    public void testSyncWithTokensDisabled() {
        String payload = "[\"Use of the history API requires the Storage & Playback which is not enabled for this " +
                "subscribe key.Login to your PubNub Dashboard Account and enable Storage & Playback.Contact support " +
                "@pubnub.com if you require further assistance.\",0,0]";

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(payload)));

        try {
            partialHistory.channel("niceChannel").includeTimetoken(true).sync();
        } catch (PubNubException ex) {
            assertEquals("History is disabled", ex.getErrormsg());
        }
    }

    @Test
    public void testSyncSuccess() throws IOException, PubNubException {
        List<Object> testArray = new ArrayList<>();
        List<Object> historyItems = new ArrayList<>();

        Map<String, Object> historyEnvelope1 = new HashMap<>();
        Map<String, Object> historyItem1 = new HashMap<>();
        historyItem1.put("a", 11);
        historyItem1.put("b", 22);
        historyEnvelope1.put("timetoken", 1111);
        historyEnvelope1.put("message", historyItem1);

        Map<String, Object> historyEnvelope2 = new HashMap<>();
        Map<String, Object> historyItem2 = new HashMap<>();
        historyItem2.put("a", 33);
        historyItem2.put("b", 44);
        historyEnvelope2.put("timetoken", 2222);
        historyEnvelope2.put("message", historyItem2);

        historyItems.add(historyEnvelope1);
        historyItems.add(historyEnvelope2);

        testArray.add(historyItems);
        testArray.add(1234);
        testArray.add(4321);

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.getMapper().toJson(testArray))));

        PNHistoryResult response = partialHistory.channel("niceChannel").includeTimetoken(true).sync();

        assert response != null;

        assertEquals(1234L, (long) response.getStartTimetoken());
        assertEquals(4321L, (long) response.getEndTimetoken());

        assertEquals(response.getMessages().size(), 2);

        assertEquals(1111L, (long) response.getMessages().get(0).getTimetoken());
        assertEquals((response.getMessages().get(0).getEntry()).getAsJsonObject().get("a").getAsInt(), 11);
        assertEquals((response.getMessages().get(0).getEntry()).getAsJsonObject().get("b").getAsInt(), 22);

        assertEquals(2222L, (long) response.getMessages().get(1).getTimetoken());
        assertEquals((response.getMessages().get(1).getEntry()).getAsJsonObject().get("a").getAsInt(), 33);
        assertEquals((response.getMessages().get(1).getEntry()).getAsJsonObject().get("b").getAsInt(), 44);
    }

    @Test
    public void testSyncAuthSuccess() throws PubNubException {

        pubnub.getConfiguration().setAuthKey("authKey");

        List<Object> testArray = new ArrayList<>();
        List<Object> historyItems = new ArrayList<>();

        Map<String, Object> historyEnvelope1 = new HashMap<>();
        Map<String, Object> historyItem1 = new HashMap<>();
        historyItem1.put("a", 11);
        historyItem1.put("b", 22);
        historyEnvelope1.put("timetoken", 1111);
        historyEnvelope1.put("message", historyItem1);

        Map<String, Object> historyEnvelope2 = new HashMap<>();
        Map<String, Object> historyItem2 = new HashMap<>();
        historyItem2.put("a", 33);
        historyItem2.put("b", 44);
        historyEnvelope2.put("timetoken", 2222);
        historyEnvelope2.put("message", historyItem2);

        historyItems.add(historyEnvelope1);
        historyItems.add(historyEnvelope2);

        testArray.add(historyItems);
        testArray.add(1234);
        testArray.add(4321);

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.getMapper().toJson(testArray))));

        partialHistory.channel("niceChannel").includeTimetoken(true).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals("authKey", requests.get(0).queryParameter("auth").firstValue());
        assertEquals(1, requests.size());
    }


    @Test
    public void testSyncEncryptedSuccess() throws IOException, PubNubException {
        pubnub.getConfiguration().setCipherKey("testCipher");

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody("[[\"EGwV+Ti43wh2TprPIq7o0KMuW5j6B3yWy352ucWIOmU=\\n\"," +
                        "\"EGwV+Ti43wh2TprPIq7o0KMuW5j6B3yWy352ucWIOmU=\\n\"," +
                        "\"EGwV+Ti43wh2TprPIq7o0KMuW5j6B3yWy352ucWIOmU=\\n\"],14606134331557853,14606134485013970]")));

        PNHistoryResult response = partialHistory.channel("niceChannel").includeTimetoken(false).sync();

        assert response != null;

        assertEquals(14606134331557853L, (long) response.getStartTimetoken());
        assertEquals(14606134485013970L, (long) response.getEndTimetoken());

        assertEquals(response.getMessages().size(), 3);

        assertNull(response.getMessages().get(0).getTimetoken());
        assertEquals("m1", (response.getMessages().get(0).getEntry()).getAsJsonArray().get(0).getAsString());
        assertEquals("m2", (response.getMessages().get(0).getEntry()).getAsJsonArray().get(1).getAsString());
        assertEquals("m3", (response.getMessages().get(0).getEntry()).getAsJsonArray().get(2).getAsString());

        assertEquals("m1", (response.getMessages().get(1).getEntry()).getAsJsonArray().get(0).getAsString());
        assertEquals("m2", (response.getMessages().get(1).getEntry()).getAsJsonArray().get(1).getAsString());
        assertEquals("m3", (response.getMessages().get(1).getEntry()).getAsJsonArray().get(2).getAsString());

        assertEquals("m1", (response.getMessages().get(2).getEntry()).getAsJsonArray().get(0).getAsString());
        assertEquals("m2", (response.getMessages().get(2).getEntry()).getAsJsonArray().get(1).getAsString());
        assertEquals("m3", (response.getMessages().get(2).getEntry()).getAsJsonArray().get(2).getAsString());

    }

    @Test
    public void testSyncEncryptedWithPNOtherSuccess() throws PubNubException {
        pubnub.getConfiguration().setCipherKey("hello");

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody("[[{\"pn_other\":\"6QoqmS9CnB3W9+I4mhmL7w==\"}],14606134331557852," +
                        "14606134485013970]")));

        PNHistoryResult response = partialHistory.channel("niceChannel").includeTimetoken(false).sync();

        assert response != null;

        assertEquals(14606134331557852L, (long) response.getStartTimetoken());
        assertEquals(14606134485013970L, (long) response.getEndTimetoken());

        assertEquals(response.getMessages().size(), 1);

        assertNull(response.getMessages().get(0).getTimetoken());
        assertEquals("hey",
                response.getMessages().get(0).getEntry().getAsJsonObject().get("pn_other").getAsJsonObject().get(
                        "text").getAsString());

    }

    @Test
    public void testSyncSuccessWithoutTimeToken() throws PubNubException {
        List<Object> testArray = new ArrayList<>();
        List<Object> historyItems = new ArrayList<>();

        Map<String, Object> historyItem1 = new HashMap<>();
        historyItem1.put("a", 11);
        historyItem1.put("b", 22);

        Map<String, Object> historyItem2 = new HashMap<>();
        historyItem2.put("a", 33);
        historyItem2.put("b", 44);

        historyItems.add(historyItem1);
        historyItems.add(historyItem2);

        testArray.add(historyItems);
        testArray.add(1234);
        testArray.add(4321);

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.getMapper().toJson(testArray))));

        PNHistoryResult response = partialHistory.channel("niceChannel").sync();

        assert response != null;

        assertTrue(response.getStartTimetoken().equals(1234L));
        assertTrue(response.getEndTimetoken().equals(4321L));

        assertEquals(response.getMessages().size(), 2);

        assertNull(response.getMessages().get(0).getTimetoken());
        assertEquals(response.getMessages().get(0).getEntry().getAsJsonObject().get("a").getAsInt(), 11);
        assertEquals(response.getMessages().get(0).getEntry().getAsJsonObject().get("b").getAsInt(), 22);

        assertNull(response.getMessages().get(1).getTimetoken());
        assertEquals(response.getMessages().get(1).getEntry().getAsJsonObject().get("a").getAsInt(), 33);
        assertEquals(response.getMessages().get(1).getEntry().getAsJsonObject().get("b").getAsInt(), 44);
    }


    @Test(expected = PubNubException.class)
    public void testMissinChannel() throws IOException, PubNubException {
        List<Object> testArray = new ArrayList<>();
        List<Object> historyItems = new ArrayList<>();

        Map<String, Object> historyEnvelope1 = new HashMap<>();
        Map<String, Object> historyItem1 = new HashMap<>();
        historyItem1.put("a", 11);
        historyItem1.put("b", 22);
        historyEnvelope1.put("timetoken", 1111);
        historyEnvelope1.put("message", historyItem1);

        Map<String, Object> historyEnvelope2 = new HashMap<>();
        Map<String, Object> historyItem2 = new HashMap<>();
        historyItem2.put("a", 33);
        historyItem2.put("b", 44);
        historyEnvelope2.put("timetoken", 2222);
        historyEnvelope2.put("message", historyItem2);

        historyItems.add(historyEnvelope1);
        historyItems.add(historyEnvelope2);

        testArray.add(historyItems);
        testArray.add(1234);
        testArray.add(4321);

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.getMapper().toJson(testArray))));

        partialHistory.includeTimetoken(true).sync();
    }

    @Test(expected = PubNubException.class)
    public void testChannelIsEmpty() throws PubNubException {
        List<Object> testArray = new ArrayList<>();
        List<Object> historyItems = new ArrayList<>();

        Map<String, Object> historyEnvelope1 = new HashMap<>();
        Map<String, Object> historyItem1 = new HashMap<>();
        historyItem1.put("a", 11);
        historyItem1.put("b", 22);
        historyEnvelope1.put("timetoken", 1111);
        historyEnvelope1.put("message", historyItem1);

        Map<String, Object> historyEnvelope2 = new HashMap<>();
        Map<String, Object> historyItem2 = new HashMap<>();
        historyItem2.put("a", 33);
        historyItem2.put("b", 44);
        historyEnvelope2.put("timetoken", 2222);
        historyEnvelope2.put("message", historyItem2);

        historyItems.add(historyEnvelope1);
        historyItems.add(historyEnvelope2);

        testArray.add(historyItems);
        testArray.add(1234);
        testArray.add(4321);

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.getMapper().toJson(testArray))));

        partialHistory.channel("").includeTimetoken(true).sync();
    }

    @Test
    public void testOperationTypeSuccessAsync() throws PubNubException {

        List<Object> testArray = new ArrayList<>();
        List<Object> historyItems = new ArrayList<>();

        Map<String, Object> historyEnvelope1 = new HashMap<>();
        Map<String, Object> historyItem1 = new HashMap<>();
        historyItem1.put("a", 11);
        historyItem1.put("b", 22);
        historyEnvelope1.put("timetoken", 1111);
        historyEnvelope1.put("message", historyItem1);

        Map<String, Object> historyEnvelope2 = new HashMap<>();
        Map<String, Object> historyItem2 = new HashMap<>();
        historyItem2.put("a", 33);
        historyItem2.put("b", 44);
        historyEnvelope2.put("timetoken", 2222);
        historyEnvelope2.put("message", historyItem2);

        historyItems.add(historyEnvelope1);
        historyItems.add(historyEnvelope2);

        testArray.add(historyItems);
        testArray.add(1234);
        testArray.add(4321);

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.getMapper().toJson(testArray))));

        final AtomicInteger atomic = new AtomicInteger(0);
        partialHistory.channel("niceChannel").includeTimetoken(true).async(new PNCallback<PNHistoryResult>() {
            @Override
            public void onResponse(PNHistoryResult result, @NotNull PNStatus status) {
                if (status != null && status.getOperation() == PNOperationType.PNHistoryOperation) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test
    public void testSyncCountReverseStartEndSuccess() throws IOException, PubNubException {
        List<Object> testArray = new ArrayList<>();
        List<Object> historyItems = new ArrayList<>();

        Map<String, Object> historyEnvelope1 = new HashMap<>();
        Map<String, Object> historyItem1 = new HashMap<>();
        historyItem1.put("a", 11);
        historyItem1.put("b", 22);
        historyEnvelope1.put("timetoken", 1111);
        historyEnvelope1.put("message", historyItem1);

        Map<String, Object> historyEnvelope2 = new HashMap<>();
        Map<String, Object> historyItem2 = new HashMap<>();
        historyItem2.put("a", 33);
        historyItem2.put("b", 44);
        historyEnvelope2.put("timetoken", 2222);
        historyEnvelope2.put("message", historyItem2);

        historyItems.add(historyEnvelope1);
        historyItems.add(historyEnvelope2);

        testArray.add(historyItems);
        testArray.add(1234);
        testArray.add(4321);

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.getMapper().toJson(testArray))));

        PNHistoryResult response =
                partialHistory.channel("niceChannel").count(5).reverse(true).start(1L).end(2L).includeTimetoken(true).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/history/sub-key/mySubscribeKey" +
                "/channel/niceChannel.*")));
        assertTrue(requests.get(0).queryParameter("reverse").firstValue().equals("true"));
        assertTrue(Integer.valueOf(requests.get(0).queryParameter("count").firstValue()).equals(5));
        assertTrue(Integer.valueOf(requests.get(0).queryParameter("start").firstValue()).equals(1));
        assertTrue(Integer.valueOf(requests.get(0).queryParameter("end").firstValue()).equals(2));
        assertTrue(requests.get(0).queryParameter("include_token").firstValue().equals("true"));


        assertTrue(response.getStartTimetoken().equals(1234L));
        assertTrue(response.getEndTimetoken().equals(4321L));

        assertEquals(response.getMessages().size(), 2);

        assertTrue(response.getMessages().get(0).getTimetoken().equals(1111L));
        assertEquals((response.getMessages().get(0).getEntry()).getAsJsonObject().get("a").getAsInt(), 11);
        assertEquals((response.getMessages().get(0).getEntry()).getAsJsonObject().get("b").getAsInt(), 22);

        assertTrue(response.getMessages().get(1).getTimetoken().equals(2222L));
        assertEquals((response.getMessages().get(1).getEntry()).getAsJsonObject().get("a").getAsInt(), 33);
        assertEquals((response.getMessages().get(1).getEntry()).getAsJsonObject().get("b").getAsInt(), 44);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testSyncProcessMessageError() throws IOException, PubNubException {
        List<Object> testArray = new ArrayList<>();
        List<Object> historyItems = new ArrayList<>();

        Map<String, Object> historyEnvelope1 = new HashMap<>();
        Map<String, Object> historyItem1 = new HashMap<>();
        historyItem1.put("a", 11);
        historyItem1.put("b", 22);
        historyEnvelope1.put("timetoken", 1111);
        historyEnvelope1.put("message", historyItem1);

        Map<String, Object> historyEnvelope2 = new HashMap<>();
        Map<String, Object> historyItem2 = new HashMap<>();
        historyItem2.put("a", 33);
        historyItem2.put("b", 44);
        historyEnvelope2.put("timetoken", 2222);
        historyEnvelope2.put("message", historyItem2);

        historyItems.add(historyEnvelope1);
        historyItems.add(historyEnvelope2);

        testArray.add(historyItems);
        testArray.add(1234);
        testArray.add(4321);

        stubFor(get(urlPathEqualTo("/v2/history/sub-key/mySubscribeKey/channel/niceChannel"))
                .willReturn(aResponse().withBody(pubnub.getMapper().toJson(testArray))));

        pubnub.getConfiguration().setCipherKey("Test");
        partialHistory.channel("niceChannel").count(5).reverse(true).start(1L).end(2L).includeTimetoken(true).sync();
    }


}
