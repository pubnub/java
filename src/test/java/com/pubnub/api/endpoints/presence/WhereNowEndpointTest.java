package com.pubnub.api.endpoints.presence;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.WhereNowCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNWhereNowResult;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import static org.hamcrest.MatcherAssert.assertThat;

public class WhereNowEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private PubNub pubnub;
    private WhereNow partialWhereNow;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        partialWhereNow = pubnub.whereNow();
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
        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        PNWhereNowResult response = partialWhereNow.sync();

        assert response != null;

        assertThat(response.getChannels(), org.hamcrest.Matchers.contains("a", "b"));
    }

    @Test
    public void testSyncSuccessCustomUUID() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/customUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        PNWhereNowResult response = partialWhereNow.uuid("customUUID").sync();

        assert response != null;

        assertThat(response.getChannels(), org.hamcrest.Matchers.contains("a", "b"));
    }

    @Test(expected = PubNubException.class)
    public void testSyncBrokenWithString() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[zimp]}, \"service\": \"Presence\"}")));

        partialWhereNow.sync();
    }

    @Test(expected = PubNubException.class)
    public void testSyncBrokenWithoutJSON() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "zimp}, \"service\": \"Presence\"}")));

        partialWhereNow.sync();
    }

    @Test(expected = PubNubException.class)
    public void testSyncBrokenWithout200() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}," +
                                " \"service\": \"Presence\"}")));

        partialWhereNow.sync();
    }

    @Test
    public void testAsyncSuccess() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);
        partialWhereNow.async(new WhereNowCallback() {

            @Override
            public void onResponse(@Nullable PNWhereNowResult result, @NotNull PNStatus status) {
                assert result != null;
                assertThat(result.getChannels(), org.hamcrest.Matchers.contains("a", "b"));
                atomic.incrementAndGet();
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test
    public void testAsyncBrokenWithString() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[zimp]}, \"service\": \"Presence\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);
        partialWhereNow.async(new WhereNowCallback() {

            @Override
            public void onResponse(@Nullable PNWhereNowResult result, @NotNull PNStatus status) {
                atomic.incrementAndGet();
            }

        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test
    public void testAsyncBrokenWithoutJSON() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "zimp}, \"service\": \"Presence\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);
        partialWhereNow.async(new WhereNowCallback() {

            @Override
            public void onResponse(@Nullable PNWhereNowResult result, @NotNull PNStatus status) {
                atomic.incrementAndGet();
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testAsyncBrokenWithout200() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}," +
                                " \"service\": \"Presence\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);
        partialWhereNow.async(new WhereNowCallback() {

            @Override
            public void onResponse(PNWhereNowResult result, @NotNull PNStatus status) {
                atomic.incrementAndGet();
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testIsAuthRequiredSuccessSync() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setAuthKey("myKey");
        partialWhereNow.sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @Test(expected = PubNubException.class)
    public void testNullSubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setSubscribeKey(null);
        partialWhereNow.sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptySubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": " +
                        "[\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setSubscribeKey("");
        partialWhereNow.sync();
    }

    @Test(expected = PubNubException.class)
    public void testNullPayloadSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        partialWhereNow.sync();
    }

}
