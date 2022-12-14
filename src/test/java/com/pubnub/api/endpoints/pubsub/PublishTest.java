package com.pubnub.api.endpoints.pubsub;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.MessageType;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.pubnub.api.endpoints.pubsub.Publish.MESSAGE_TYPE_QUERY_PARAMETER;
import static com.pubnub.api.endpoints.pubsub.Publish.SPACE_ID_QUERY_PARAMETER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

public class PublishTest extends TestHarness {

    public static final String CHANNEL_NAME = "coolChannel";
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private PubNub pubnub;
    private Publish instance;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        instance = pubnub.publish();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void testFireSuccessSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.fire().channel(CHANNEL_NAME).message("hi").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("true", requests.get(0).queryParameter("norep").firstValue());
        assertEquals("0", requests.get(0).queryParameter("store").firstValue());
    }

    @Test
    public void testNoRepSuccessSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hi").replicate(false).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("true", requests.get(0).queryParameter("norep").firstValue());
    }

    @Test
    public void testRepDefaultSuccessSync() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hirep").replicate(false).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("true", requests.get(0).queryParameter("norep").firstValue());
    }

    @Test
    public void testSuccessSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hi").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
    }

    @Test
    public void testSuccessSequenceSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hi").sync();
        instance.channel(CHANNEL_NAME).message("hi").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(2, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("1", requests.get(0).queryParameter("seqn").firstValue());
        assertEquals("2", requests.get(1).queryParameter("seqn").firstValue());


    }

    @Test
    public void testSuccessPostSync() throws PubNubException, UnsupportedEncodingException {
        stubFor(post(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).usePOST(true).message(Arrays.asList("m1", "m2")).sync();

        List<LoggedRequest> requests = findAll(postRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("[\"m1\",\"m2\"]", new String(requests.get(0).getBody(), "UTF-8"));
    }

    @Test
    public void testSuccessStoreFalseSync() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hi").shouldStore(false).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("0", requests.get(0).queryParameter("store").firstValue());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
    }

    @Test
    public void testSuccessStoreTrueSync() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hi").shouldStore(true).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("1", requests.get(0).queryParameter("store").firstValue());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
    }

    @Test
    public void testSuccessMetaSync() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("meta", matching("%5B%22m1%22%2C%22m2%22%5D"))
                .withQueryParam("meta", equalToJson("[\"m1\",\"m2\"]"))
                .withQueryParam("store", matching("0"))
                .withQueryParam("seqn", matching("1"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hi").meta(Arrays.asList("m1", "m2")).shouldStore(false).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }

    @Test
    public void testSuccessAuthKeySync() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setAuthKey("authKey");
        instance.channel(CHANNEL_NAME).message("hi").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("authKey", requests.get(0).queryParameter("auth").firstValue());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testSuccessIntSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/10"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message(10).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testSuccessArraySync() throws PubNubException {
        stubFor(get(urlEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%5B%22a%22%2C%22b%22%2C%22c%22" +
                "%5D?pnsdk=PubNub-Java-Unified/suchJava&requestid=PubNubRequestId&seqn=1&uuid=myUUID"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message(Arrays.asList("a", "b", "c")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
    }

    @Test
    public void testSuccessArrayEncryptedSync() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22HFP7V6bDwBLrwc1t8Rnrog%3D" +
                "%3D%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setCipherKey("testCipher");
        instance.channel(CHANNEL_NAME).message(Arrays.asList("m1", "m2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
    }

    @Test
    public void testSuccessPostEncryptedSync() throws PubNubException {
        stubFor(post(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setCipherKey("testCipher");

        instance.channel(CHANNEL_NAME).usePOST(true).message(Arrays.asList("m1", "m2")).sync();

        List<LoggedRequest> requests = findAll(postRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertEquals("\"HFP7V6bDwBLrwc1t8Rnrog==\"", new String(requests.get(0).getBody(), Charset.forName("UTF-8")));
    }

    @Test
    public void testSuccessHashMapSync() throws PubNubException {
        Map<String, Object> params = new HashMap<>();
        params.put("a", 10);
        params.put("z", "test");

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%7B%22a%22%3A10%2C%22z%22%3A" +
                "%22test%22%7D"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message(params).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testSuccessPOJOSync() throws PubNubException {
        TestPojo testPojo = new TestPojo("10", "20");

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%7B%22field1%22%3A%2210%22" +
                "%2C%22field2%22%3A%2220%22%7D"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message(testPojo).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testJSONObject() throws PubNubException {
        JSONObject testMessage = new JSONObject();
        testMessage.put("hi", "test");

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%7B%22hi%22%3A%22test%22%7D"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message(testMessage.toMap()).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test
    public void testJSONList() throws PubNubException {
        JSONArray testMessage = new JSONArray();
        testMessage.put("hi");
        testMessage.put("hi2");

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%5B%22hi%22%2C%22hi2%22%5D"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message(testMessage.toList()).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());

    }

    @Test(expected = PubNubException.class)
    public void testMissingChannel() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.message("hi").sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptyChannel() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.message("hi").channel("").sync();
    }

    @Test(expected = PubNubException.class)
    public void testMissingMessage() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).sync();
    }

    @Test
    public void testOperationTypeSuccessAsync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        final AtomicInteger atomic = new AtomicInteger(0);

        instance.async(new PNCallback<PNPublishResult>() {
            @Override
            public void onResponse(PNPublishResult result, @NotNull PNStatus status) {
                if (status != null && status.getOperation() == PNOperationType.PNPublishOperation) {
                    atomic.incrementAndGet();
                }

            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test(expected = PubNubException.class)
    public void testNullSubKeySync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setSubscribeKey(null);
        instance.channel(CHANNEL_NAME).message("hirep").sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptySubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setSubscribeKey("");
        instance.channel(CHANNEL_NAME).message("hirep").sync();
    }

    @Test(expected = PubNubException.class)
    public void testNullPublishKeySync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setPublishKey(null);
        instance.channel(CHANNEL_NAME).message("hirep").sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptyPublishKeySync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        pubnub.getConfiguration().setPublishKey("");
        instance.channel(CHANNEL_NAME).message("hirep").sync();
    }

    @Test(expected = PubNubException.class)
    public void testInvalidMessage() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message(new Object()).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertNull(requests.get(0).queryParameter("norep"));
    }

    @Test(expected = PubNubException.class)
    public void testInvalidMeta() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hi").meta(new Object()).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myUUID", requests.get(0).queryParameter("uuid").firstValue());
        assertNull(requests.get(0).queryParameter("norep"));
    }

    @Test
    public void testTTLShouldStoryDefaultSuccessSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hi").ttl(10).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("10", requests.get(0).queryParameter("ttl").firstValue());
    }

    @Test
    public void testTTLShouldStoreFalseSuccessSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hi%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));

        instance.channel(CHANNEL_NAME).message("hi").shouldStore(false).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("0", requests.get(0).queryParameter("store").firstValue());
        assertFalse(requests.get(0).queryParameter("ttl").isPresent());
    }

    @Test
    public void when_spaceId_is_provided_then_queryParams_should_contains_spaceIdQueryParam() throws PubNubException {
        String spaceIdValue = "mySpace";

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));


        instance.channel(CHANNEL_NAME).message("hirep").spaceId(new SpaceId(spaceIdValue)).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals(spaceIdValue, requests.get(0).queryParameter(SPACE_ID_QUERY_PARAMETER).firstValue());
    }

    @Test
    public void when_spaceId_is_not_provided_then_queryParams_should_not_contain_spaceIdQueryParam() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));


        instance.channel(CHANNEL_NAME).message("hirep").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertThat(requests.get(0).getQueryParams(), not(hasKey(SPACE_ID_QUERY_PARAMETER)));
    }

    @Test
    public void when_messageType_is_provided_then_queryParams_should_contains_messageTypeQueryParam() throws PubNubException {
        String messageTypeValue = "simplyMessage";

        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));


        instance.channel(CHANNEL_NAME).message("hirep").messageType(new MessageType(messageTypeValue)).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals(messageTypeValue, requests.get(0).queryParameter(MESSAGE_TYPE_QUERY_PARAMETER).firstValue());
    }

    @Test
    public void when_messageType_is_not_provided_then_queryParams_should_not_contain_messageTypeQueryParam() throws PubNubException {
        stubFor(get(urlPathEqualTo("/publish/myPublishKey/mySubscribeKey/0/" + CHANNEL_NAME + "/0/%22hirep%22"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"14598111595318003\"]")));


        instance.channel(CHANNEL_NAME).message("hirep").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertThat(requests.get(0).getQueryParams(), not(hasKey(MESSAGE_TYPE_QUERY_PARAMETER)));
    }
}
