package com.pubnub.api.endpoints.pubsub;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import okhttp3.HttpUrl;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING;
import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_MESSAGE_MISSING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SignalTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private PubNub pubNub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubNub = this.createPubNubInstance();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubNub.destroy();
        pubNub = null;
        wireMockRule.stop();
    }

    @Test
    public void testSignalGetSuccessSync() throws PubNubException {
        stubFor(get(urlMatching("/signal/myPublishKey/mySubscribeKey/0/coolChannel.*"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"1000\"]")));

        Map<String, Object> payload = new HashMap<>();
        payload.put("text", "hello");

        pubNub.signal()
                .channel("coolChannel")
                .message(payload)
                .sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        assertEquals("myUUID", request.queryParameter("uuid").firstValue());

        HttpUrl httpUrl = HttpUrl.parse(request.getAbsoluteUrl());
        String decodedSignalPayload = null;
        if (httpUrl != null) {
            decodedSignalPayload = httpUrl.pathSegments().get(httpUrl.pathSize() - 1);
        }
        assertEquals(pubNub.getMapper().toJson(payload), decodedSignalPayload);
    }

    @Test
    public void testSignalGetSuccessAsync() {

        String payload = UUID.randomUUID().toString();

        stubFor(get(urlMatching("/signal/myPublishKey/mySubscribeKey/0/coolChannel.*"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"1000\"]")));

        final AtomicBoolean success = new AtomicBoolean();

        pubNub.signal()
                .channel("coolChannel")
                .message(payload)
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, @NotNull PNStatus status) {
                        assertFalse(status.isError());
                        assertEquals(PNOperationType.PNSignalOperation, status.getOperation());
                        assertEquals("1000", result.getTimetoken().toString());
                        success.set(true);
                    }
                });

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilTrue(success);

    }

    @Test
    public void testSignalSuccessReceive() {
        final String expectedCoolChannel = "coolChannel";
        final String expectedMessage = "hello";
        final String issuingClient = "uuid";
        final String expectedUserMessageTypeValue = "myMessageType";
        final String expectedSpaceIdValue = "1-to-1_chat";
        final String subscribeResponseSignalJson = getSubscribeResponseForSignalWithMessageTypeAndSpaceId(expectedCoolChannel, expectedMessage, issuingClient, expectedUserMessageTypeValue, expectedSpaceIdValue);

        stubFor(get(urlMatching("/v2/subscribe/mySubscribeKey/coolChannel/0.*"))
                .willReturn(aResponse().withBody(subscribeResponseSignalJson)));

        AtomicBoolean success = new AtomicBoolean();

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                throw new RuntimeException("Should never receive a message");
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {
                assertEquals(expectedCoolChannel, signal.getChannel());
                assertEquals(expectedMessage, signal.getMessage().getAsString());
                assertEquals(issuingClient, signal.getPublisher());
                assertEquals(expectedUserMessageTypeValue, signal.getType());
                assertEquals(expectedSpaceIdValue, signal.getSpaceId().getValue());
                success.set(true);
            }

            @Override
            public void uuid(@NotNull PubNub pubnub, @NotNull PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull PubNub pubnub, @NotNull PNChannelMetadataResult pnChannelMetadataResult) {

            }

            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }

            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList("coolChannel"))
                .execute();


        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .untilTrue(success);
    }

    @Test
    public void testSignalFailNoChannel() {
        try {
            pubNub.signal()
                    .message(UUID.randomUUID().toString())
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERROBJ_CHANNEL_MISSING.getMessage(), e.getPubnubError().getMessage());
        }
    }

    @Test
    public void testSignalFailNoMessage() {
        try {
            pubNub.signal()
                    .channel(UUID.randomUUID().toString())
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERROBJ_MESSAGE_MISSING.getMessage(), e.getPubnubError().getMessage());
        }
    }

    @Test
    public void testSignalTelemetryParam() throws PubNubException {
        stubFor(get(urlMatching("/signal/myPublishKey/mySubscribeKey/0/coolChannel.*"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"1000\"]")));

        stubFor(get(urlMatching("/time/0.*"))
                .willReturn(aResponse().withBody("[1000]")));

        pubNub.signal()
                .channel("coolChannel")
                .message(UUID.randomUUID().toString())
                .sync();

        pubNub.time()
                .sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/time/0.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        assertTrue(request.queryParameter("l_sig").isPresent());
    }

    @Test
    public void testSignalHttpMethod() throws PubNubException {
        stubFor(get(urlMatching("/signal/myPublishKey/mySubscribeKey/0/coolChannel.*"))
                .willReturn(aResponse().withBody("[1,\"Sent\",\"1000\"]")));

        pubNub.signal()
                .channel("coolChannel")
                .message(UUID.randomUUID().toString())
                .sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/signal.*")));
        assertEquals(1, requests.size());
        LoggedRequest request = requests.get(0);
        assertEquals(RequestMethod.GET, request.getMethod());
    }

    @NotNull
    private String getSubscribeResponseForSignalWithMessageTypeAndSpaceId(String channel, String message, String issuingClient, String userMessageTypeValue, String spaceIdValue) {
        String valueIndicatingThatMessageIsOfTypeSignal = "1";
        return "{\"m\":[{\"c\":\"" + channel + "\",\"f\":\"0\",\"i\":\"" + issuingClient + "\"," +
                "\"d\":\"" + message + "\",\"e\":" + valueIndicatingThatMessageIsOfTypeSignal + ",\"p\":{\"t\":1000,\"r\":1},\"k\":\"mySubscribeKey\"," +
                "\"b\":\"" + channel + "\",\"mt\":\"" + userMessageTypeValue + "\",\"si\":\"" + spaceIdValue + "\"}],\"t\":{\"r\":\"56\",\"t\":1000}}";
    }
}
