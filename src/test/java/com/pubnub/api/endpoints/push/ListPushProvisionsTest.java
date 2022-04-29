package com.pubnub.api.endpoints.push;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.push.PNPushListProvisionsResult;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Assert;
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
import static org.junit.Assert.assertFalse;

public class ListPushProvisionsTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private ListPushProvisions instance;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        instance = pubnub.auditPushChannelProvisions();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void testAppleSuccessSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        instance.deviceId("niceDevice").pushType(PNPushType.APNS).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

        assertEquals("apns", requests.get(0).queryParameter("type").firstValue());
        assertFalse(requests.get(0).queryParameter("environment").isPresent());
        assertFalse(requests.get(0).queryParameter("news").isPresent());
    }

    @Test
    public void testGoogleSuccessSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        instance.deviceId("niceDevice").pushType(PNPushType.GCM).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

        assertEquals("gcm", requests.get(0).queryParameter("type").firstValue());
        assertFalse(requests.get(0).queryParameter("environment").isPresent());
        assertFalse(requests.get(0).queryParameter("news").isPresent());
    }

    @Test
    public void testFirebaseSuccessSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        instance.deviceId("niceDevice").pushType(PNPushType.FCM).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

        assertEquals("gcm", requests.get(0).queryParameter("type").firstValue());
        assertFalse(requests.get(0).queryParameter("environment").isPresent());
        assertFalse(requests.get(0).queryParameter("news").isPresent());
    }

    @Test
    public void testMicrosoftSuccessSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        instance.deviceId("niceDevice").pushType(PNPushType.MPNS).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

        assertEquals("mpns", requests.get(0).queryParameter("type").firstValue());
        assertFalse(requests.get(0).queryParameter("environment").isPresent());
        assertFalse(requests.get(0).queryParameter("news").isPresent());
    }

    @Test
    public void testApns2SuccessSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        instance.deviceId("niceDevice").pushType(PNPushType.APNS2).topic("news").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

        assertEquals("development", requests.get(0).queryParameter("environment").firstValue());
        assertEquals("news", requests.get(0).queryParameter("topic").firstValue());
        assertFalse(requests.get(0).queryParameter("type").isPresent());
    }

    @Test
    public void testIsAuthRequiredSuccess() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        pubnub.getConfiguration().setAuthKey("myKey");
        instance.deviceId("niceDevice").pushType(PNPushType.APNS).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @Test
    public void testOperationTypeSuccess() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        final AtomicInteger atomic = new AtomicInteger(0);

        instance.deviceId("niceDevice").pushType(PNPushType.APNS).async(new PNCallback<PNPushListProvisionsResult>() {
            @Override
            public void onResponse(PNPushListProvisionsResult result, @NotNull PNStatus status) {
                if (status != null
                        && status.getOperation() == PNOperationType.PNPushNotificationEnabledChannelsOperation) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test(expected = PubNubException.class)
    public void testNullSubscribeKey() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        pubnub.getConfiguration().setSubscribeKey(null);
        instance.deviceId("niceDevice").pushType(PNPushType.APNS).sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptySubscribeKey() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        pubnub.getConfiguration().setSubscribeKey("");
        instance.deviceId("niceDevice").pushType(PNPushType.MPNS).sync();
    }

    @Test(expected = PubNubException.class)
    public void testNullPushType() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        instance.deviceId("niceDevice").sync();
    }

    @Test(expected = PubNubException.class)
    public void testNullDeviceId() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        instance.pushType(PNPushType.MPNS).sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptyDeviceIdRemoveAll() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/push/sub-key/mySubscribeKey/devices/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        instance.deviceId("").pushType(PNPushType.MPNS).sync();
    }

    @Test
    public void testApns2NoTopic() {
        try {
            instance.deviceId("niceDevice").pushType(PNPushType.APNS2).sync();
        } catch (PubNubException e) {
            Assert.assertEquals(e.getPubnubError(), PubNubErrorBuilder.PNERROBJ_PUSH_TOPIC_MISSING);
        }
    }

    @Test
    public void testApns2DefaultEnvironment() throws PubNubException {
        stubFor(get(urlPathEqualTo("/v2/push/sub-key/mySubscribeKey/devices-apns2/niceDevice"))
                .willReturn(aResponse().withBody("[\"ch1\", \"ch2\", \"ch3\"]")));

        instance.deviceId("niceDevice").pushType(PNPushType.APNS2).topic("news").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

        assertEquals("development", requests.get(0).queryParameter("environment").firstValue());
    }

    @Test
    public void testPushTypeNames() {
        String expectedName = "gcm";
        Assert.assertEquals(PNPushType.GCM.toString(), PNPushType.FCM.toString());
        Assert.assertEquals(PNPushType.GCM + "", PNPushType.FCM + "");
        Assert.assertEquals(expectedName, PNPushType.GCM + "");
        Assert.assertEquals(expectedName, PNPushType.FCM + "");
        Assert.assertEquals(expectedName, PNPushType.GCM.toString());
        Assert.assertEquals(expectedName, PNPushType.FCM.toString());

        Assert.assertEquals("mpns", PNPushType.MPNS.toString());
        Assert.assertEquals("apns", PNPushType.APNS.toString());
        Assert.assertEquals("apns2", PNPushType.APNS2.toString());
    }
}
