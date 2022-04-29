package com.pubnub.api.managers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNHeartbeatNotificationOptions;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.presence.PNSetStateResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.CheckedOutputStream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class SubscriptionManagerTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT), false);

    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void testGetSubscribedChannels() {
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        List<String> channels = pubnub.getSubscribedChannels();

        assertTrue(channels.contains("ch1"));
        assertTrue(channels.contains("ch2"));
    }

    @Test
    public void testGetSubscribedEmptyChannel() {

        final AtomicInteger gotMessages = new AtomicInteger();

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.subscribe().channels(Arrays.asList("")).execute();

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                gotMessages.addAndGet(1);
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                gotMessages.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                gotMessages.addAndGet(1);
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAtomic(gotMessages, org.hamcrest.core.IsEqual.equalTo(0));

    }

    @Test
    public void testGetSubscribedEmptyChannelGroup() {

        final AtomicInteger gotMessages = new AtomicInteger();

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.subscribe().channelGroups(Arrays.asList("")).execute();

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                gotMessages.addAndGet(1);
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                gotMessages.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                gotMessages.addAndGet(1);
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAtomic(gotMessages, org.hamcrest.core.IsEqual.equalTo(0));

    }

    @Test
    public void testGetSubscribedChannelGroups() {
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.subscribe().channelGroups(Arrays.asList("cg1", "cg2")).execute();

        List<String> groups = pubnub.getSubscribedChannelGroups();

        assertTrue(groups.contains("cg1"));
        assertTrue(groups.contains("cg2"));
    }

    @Test
    public void testPubNubUnsubscribeAll() {

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2"))
                .channelGroups(Arrays.asList("cg1", "cg2"))
                .withPresence()
                .execute();

        List<String> channels = pubnub.getSubscribedChannels();
        assertTrue(channels.contains("ch1"));
        assertTrue(channels.contains("ch2"));

        List<String> groups = pubnub.getSubscribedChannelGroups();
        assertTrue(groups.contains("cg1"));
        assertTrue(groups.contains("cg2"));

        pubnub.unsubscribeAll();

        channels = pubnub.getSubscribedChannels();
        assertEquals(0, channels.size());

        groups = pubnub.getSubscribedChannelGroups();
        assertEquals(0, groups.size());
    }

    @Test
    public void testSubscribeBuilder() {
        final AtomicInteger gotStatus = new AtomicInteger();
        final AtomicBoolean gotMessage = new AtomicBoolean();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Publisher-A\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChannel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {

                if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    gotStatus.addAndGet(1);
                }

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() > 0);
                assertEquals("Message", pubnub.getMapper().elementToString(message.getMessage(), "text"));
                assertEquals("coolChannel", message.getChannel());
                assertEquals(null, message.getSubscription());
                assertEquals("Publisher-A", message.getPublisher());
                gotMessage.set(true);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAtomic(gotMessage, org.hamcrest.core.IsEqual.equalTo(true));
        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAtomic(gotStatus, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribeDuplicateDisabledBuilder() {
        final AtomicInteger gotMessages = new AtomicInteger();

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Publisher-A\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChannel\"},{\"a\":\"4\",\"f\":0," +
                        "\"i\":\"Publisher-A\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChannel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                gotMessages.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAtomic(gotMessages, org.hamcrest.core.IsEqual.equalTo(2));
    }

    @Test
    public void testSubscribeDuplicateBuilder() {
        this.pubnub.getConfiguration().setDedupOnSubscribe(true);
        final AtomicInteger gotMessages = new AtomicInteger();

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Publisher-A\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChannel\"},{\"a\":\"4\",\"f\":0," +
                        "\"i\":\"Publisher-A\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChannel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                gotMessages.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAtomic(gotMessages, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test
    public void testSubscribeDuplicateWithLimitBuilder() {
        this.pubnub.getConfiguration().setDedupOnSubscribe(true);
        this.pubnub.getConfiguration().setMaximumMessagesCacheSize(1);

        final AtomicInteger gotMessages = new AtomicInteger();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Publisher-A\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message1\"},\"b\":\"coolChannel\"},{\"a\":\"4\",\"f\":0," +
                        "\"i\":\"Publisher-A\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message2\"},\"b\":\"coolChannel\"},{\"a\":\"4\",\"f\":0," +
                        "\"i\":\"Publisher-A\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message1\"},\"b\":\"coolChannel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                gotMessages.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAtomic(gotMessages, org.hamcrest.core.IsEqual.equalTo(3));
    }

    @Test
    public void testQueueNotificationsBuilderNoThresholdSpecified() {
        pubnub.getConfiguration().setRequestMessageCountThreshold(null);
        final AtomicBoolean gotStatus = new AtomicBoolean();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChannel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getCategory() == PNStatusCategory.PNRequestMessageCountExceededCategory) {
                    gotStatus.set(true);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotStatus, org.hamcrest.core.IsEqual.equalTo(false));
    }

    @Test
    public void testQueueNotificationsBuilderBelowThreshold() {
        pubnub.getConfiguration().setRequestMessageCountThreshold(10);
        final AtomicBoolean gotStatus = new AtomicBoolean();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChannel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getCategory() == PNStatusCategory.PNRequestMessageCountExceededCategory) {
                    gotStatus.set(true);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotStatus, org.hamcrest.core.IsEqual.equalTo(false));
    }

    @Test
    public void testQueueNotificationsBuilderThresholdMatched() {
        pubnub.getConfiguration().setRequestMessageCountThreshold(1);
        final AtomicBoolean gotStatus = new AtomicBoolean();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"o\":{\"t\":\"14737141991877032\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChannel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getCategory() == PNStatusCategory.PNRequestMessageCountExceededCategory) {
                    gotStatus.set(true);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotStatus, org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void testQueueNotificationsBuilderThresholdExceeded() {
        pubnub.getConfiguration().setRequestMessageCountThreshold(1);
        final AtomicBoolean gotStatus = new AtomicBoolean();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"m\":[{\"a\":\"4\",\"b\":\"coolChannel\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"f\":0,\"i\":\"Client-g5d4g\"," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"o\":{\"r\":2," +
                        "\"t\":\"14737141991877032\"},\"p\":{\"r\":1,\"t\":\"14607577960925503\"}},{\"a\":\"5\"," +
                        "\"b\":\"coolChannel2\",\"c\":\"coolChannel2\",\"d\":{\"text\":\"Message2\"},\"f\":0," +
                        "\"i\":\"Client-g5d4g\",\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4g\",\"o\":{\"r\":2," +
                        "\"t\":\"14737141991877033\"},\"p\":{\"r\":1,\"t\":\"14607577960925504\"}}],\"t\":{\"r\":1," +
                        "\"t\":\"14607577960932487\"}}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getCategory() == PNStatusCategory.PNRequestMessageCountExceededCategory) {
                    gotStatus.set(true);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotStatus, org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void testSubscribeBuilderWithAccessManager403Error() {
        final AtomicInteger gotStatus = new AtomicInteger();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withStatus(403).withBody("{\"message\":\"Forbidden\"," +
                        "\"payload\":{\"channels\":[\"ch1\", \"ch2\"], \"channel-groups\":[\":cg1\", \":cg2\"]}," +
                        "\"error\":true,\"service\":\"Access Manager\",\"status\":403}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {

                if (status.getCategory() == PNStatusCategory.PNAccessDeniedCategory) {

                    assert status.getAffectedChannels() != null;

                    assertEquals(PNStatusCategory.PNAccessDeniedCategory, status.getCategory());
                    assertEquals(Arrays.asList("ch1", "ch2"), status.getAffectedChannels());
                    assertEquals(Arrays.asList("cg1", "cg2"), status.getAffectedChannelGroups());
                    gotStatus.addAndGet(1);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotStatus, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test
    public void testNamingSubscribeChannelGroupBuilder() {
        final AtomicBoolean gotStatus = new AtomicBoolean(false);
        final AtomicBoolean gotMessage = new AtomicBoolean(false);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChannelGroup\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    assert status.getAffectedChannels() != null;
                    assertEquals(2, status.getAffectedChannels().size());
                    gotStatus.set(true);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() > 0);
                assertEquals("Message", pubnub.getMapper().elementToString(message.getMessage(), "text"));
                assertEquals("coolChannel", message.getChannel());
                assertEquals("coolChannelGroup", message.getSubscription());
                gotMessage.set(true);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(4, TimeUnit.SECONDS).untilTrue(gotMessage);
        Awaitility.await().atMost(4, TimeUnit.SECONDS).untilTrue(gotStatus);

    }

    @Test
    public void testPresenceSubscribeBuilder() {
        final AtomicInteger gotStatus = new AtomicInteger();
        final AtomicBoolean gotMessage = new AtomicBoolean();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14614512228786519\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"p\":{\"t\":\"14614512228418349\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel-pnpres\"," +
                        "\"d\":{\"action\": \"join\", \"timestamp\": 1461451222, \"uuid\": " +
                        "\"4a6d5df7-e301-4e73-a7b7-6af9ab484eb0\", \"occupancy\": 1},\"b\":\"coolChannel-pnpres\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {

                if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    gotStatus.addAndGet(1);
                }

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() >= 1);
                assertEquals("coolChannel", presence.getChannel());
                assertEquals(null, presence.getSubscription());
                gotMessage.set(true);
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotMessage, org.hamcrest.core.IsEqual.equalTo(true));
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotStatus, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testPresenceChannelGroupSubscribeBuilder() {
        final AtomicInteger gotStatus = new AtomicInteger();
        final AtomicBoolean gotMessage = new AtomicBoolean();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14614512228786519\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"p\":{\"t\":\"14614512228418349\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel-pnpres\"," +
                        "\"d\":{\"action\": \"join\", \"timestamp\": 1461451222, \"uuid\": " +
                        "\"4a6d5df7-e301-4e73-a7b7-6af9ab484eb0\", \"occupancy\": 1}," +
                        "\"b\":\"coolChannelGroup-pnpres\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {

                if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    gotStatus.addAndGet(1);
                }

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() >= 1);
                assertEquals("coolChannel", presence.getChannel());
                assertEquals("coolChannelGroup", presence.getSubscription());
                gotMessage.set(true);
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotMessage, org.hamcrest.core.IsEqual.equalTo(true));
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotStatus, org.hamcrest.core.IsEqual.equalTo(1));

    }


    @Test
    public void testSubscribeSlidingBuilder() {
        final AtomicBoolean gotMessage1 = new AtomicBoolean();
        final AtomicBoolean gotMessage2 = new AtomicBoolean();
        final AtomicBoolean gotMessage3 = new AtomicBoolean();

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"3\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0," +
                        "\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("3"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"10\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0," +
                        "\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message3\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("tt", matching("10"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"20\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0," +
                        "\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Message10\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() >= 1);

                if (message.getMessage().getAsJsonObject().get("text").getAsString().equals("Message")) {
                    gotMessage1.set(true);
                } else if (message.getMessage().getAsJsonObject().get("text").getAsString().equals("Message3")) {
                    gotMessage2.set(true);
                } else if (message.getMessage().getAsJsonObject().get("text").getAsString().equals("Message10")) {
                    gotMessage3.set(true);
                }
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotMessage1,
                org.hamcrest.core.IsEqual.equalTo(true));
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotMessage2,
                org.hamcrest.core.IsEqual.equalTo(true));
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(gotMessage3,
                org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void testSubscribeBuilderNumber() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\": 10," +
                        "\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() >= 1);
                assertEquals(10, message.getMessage().getAsInt());
                atomic.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.Matchers.greaterThan(0));

    }

    @Test
    public void testSubscribeBuilderWithMetadata() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody(" {\"t\":{\"t\":\"14858178301085322\",\"r\":7},\"m\":[{\"a\":\"4\"," +
                        "\"f\":512,\"i\":\"02a7b822-220c-49b0-90c4-d9cbecc0fd85\",\"s\":1," +
                        "\"p\":{\"t\":\"14858178301075219\",\"r\":7},\"k\":\"demo-36\",\"c\":\"chTest\"," +
                        "\"u\":{\"status_update\":{\"lat\":55.752023906250656,\"lon\":37.61749036080494," +
                        "\"driver_id\":4722}},\"d\":{\"City\":\"Goiania\",\"Name\":\"Marcelo\"}}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() >= 1);
                assertEquals("{\"status_update\":{\"lat\":55.752023906250656,\"lon\":37.61749036080494," +
                        "\"driver_id\":4722}}", message.getUserMetadata().toString());
                atomic.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.Matchers.greaterThan(0));

    }

    @Test
    public void testSubscribeBuilderWithState() throws PubNubException {
        final String expectedPayload = PubNubUtil.urlDecode("%7B%22ch1%22%3A%5B%22p1%22%2C%22p2%22%5D%2C%22cg2%22%3A" +
                "%5B%22p1%22%2C%22p2%22%5D%7D");
        final Map<String, Object> expectedMap = pubnub.getMapper().fromJson(expectedPayload, Map.class);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.getConfiguration().setPresenceTimeout(20);
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.ALL);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).channelGroups(Arrays.asList("cg1", "cg2")).execute();
        pubnub.setPresenceState().channels(Arrays.asList("ch1")).channelGroups(Arrays.asList("cg2"))
                .state(Arrays.asList("p1", "p2"))
                .async((result, status) -> {
                });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> findAll(getRequestedFor(urlMatching(
                        "/v2/subscribe/" + pubnub.getConfiguration().getSubscribeKey() + "/ch2,ch1/.*"))).stream().anyMatch(req -> {
                    String stateString = PubNubUtil.urlDecode(req.queryParameter("state").firstValue());
                    Map<String, Object> actualMap = null;
                    try {
                        actualMap = pubnub.getMapper().fromJson(stateString, Map.class);
                    } catch (PubNubException e) {
                        e.printStackTrace();
                    }
                    return actualMap != null && actualMap.equals(expectedMap);
                }));
        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .until(() -> findAll(getRequestedFor(urlMatching(
                            "/v2/presence/sub-key/" + pubnub.getConfiguration().getSubscribeKey() + "/channel/ch2," +
                                    "ch1/heartbeat.*"))).stream().anyMatch(req -> !req.getQueryParams().containsKey("state")));

    }

    @Test
    public void testSubscribeChannelGroupBuilder() {
        final AtomicBoolean atomic = new AtomicBoolean(false);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                for (LoggedRequest request : requests) {
                    QueryParameter channelGroupQuery = request.queryParameter("channel-group");
                    if (channelGroupQuery != null && channelGroupQuery.firstValue().equals("cg1,cg2")) {
                        atomic.set(true);
                    }
                }

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channelGroups(Arrays.asList("cg1", "cg2")).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(atomic);
    }

    @Test
    public void testSubscribeChannelGroupWithPresenceBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                for (LoggedRequest request : requests) {
                    String[] channelGroups = request.queryParameter("channel-group").firstValue().split(",");
                    Arrays.sort(channelGroups);
                    if ("cg1,cg1-pnpres,cg2,cg2-pnpres".equals(joinArray(channelGroups))) {
                        atomic.addAndGet(1);
                    }

                }

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channelGroups(Arrays.asList("cg1", "cg2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.Matchers.greaterThan(0));

    }

    @Test
    public void testSubscribeWithFilterExpressionBuilder() {
        final AtomicBoolean atomic = new AtomicBoolean(false);

        pubnub.getConfiguration().setFilterExpression("much=filtering");
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("filter-expr", matching("much=filtering"))
                .withQueryParam("tt", matching("0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() > 0);
                atomic.set(true);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilTrue(atomic);
    }

    @Test
    public void testSubscribeWithEncryption() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14718972508742569\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":512,\"i\":\"ff374d0b-b866-40db-9ced-42d205bb808b\",\"p\":{\"t\":\"14718972508739738\"," +
                        "\"r\":1},\"k\":\"demo-36\",\"c\":\"max_ch1\",\"d\":\"6QoqmS9CnB3W9+I4mhmL7w==\"}]}")));

        pubnub.getConfiguration().setCipherKey("hello");

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() > 0);
                assertEquals("hey", pubnub.getMapper().elementToString(message.getMessage(), "text"));
                atomic.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.Matchers.greaterThan(0));

    }

    @Test
    public void testSubscribeWithEncryptionPNOther() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14718972508742569\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":512,\"i\":\"ff374d0b-b866-40db-9ced-42d205bb808b\",\"p\":{\"t\":\"14718972508739738\"," +
                        "\"r\":1},\"k\":\"demo-36\",\"c\":\"max_ch1\"," +
                        "\"d\":{\"pn_other\":\"6QoqmS9CnB3W9+I4mhmL7w==\"}}]}")));

        pubnub.getConfiguration().setCipherKey("hello");

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() > 0);
                assertEquals("hey", message.getMessage().getAsJsonObject().get("pn_other").getAsJsonObject().get(
                        "text").getAsString());
                atomic.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.Matchers.greaterThan(0));

    }

    @Test
    public void testSubscribePresenceBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));
                assertTrue(requests.size() >= 1);
                assertEquals("{\"text\":\"Enter Message Here\"}", message.getMessage().toString());
                atomic.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.Matchers.greaterThan(0));

    }

    @Test
    public void testSubscribePresencePayloadHereNowRefreshDeltaBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14901247588021627\",\"r\":2},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"p\":{\"t\":\"14901247587675704\",\"r\":1},\"k\":\"demo-36\"," +
                        "\"c\":\"moon-interval-deltas-pnpres\",\"d\":{\"action\": \"interval\", \"timestamp\": " +
                        "1490124758, \"occupancy\": 2, \"here_now_refresh\": true, \"join\": " +
                        "[\"2220E216-5A30-49AD-A89C-1E0B5AE26AD7\", \"4262AE3F-3202-4487-BEE0-1A0D91307DEB\"]}," +
                        "\"b\":\"moon-interval-deltas-pnpres\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                if (atomic.get() == 0) {
                    assertEquals(true, presence.getHereNowRefresh());
                    assertTrue(presence.getOccupancy().equals(2));
                    atomic.incrementAndGet();
                }
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }


    @Test
    public void testSubscribePresencePayloadJoinDeltaBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14901247588021627\",\"r\":2},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"p\":{\"t\":\"14901247587675704\",\"r\":1},\"k\":\"demo-36\"," +
                        "\"c\":\"moon-interval-deltas-pnpres\",\"d\":{\"action\": \"interval\", \"timestamp\": " +
                        "1490124758, \"occupancy\": 2, \"join\": [\"2220E216-5A30-49AD-A89C-1E0B5AE26AD7\", " +
                        "\"4262AE3F-3202-4487-BEE0-1A0D91307DEB\"]},\"b\":\"moon-interval-deltas-pnpres\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                if (atomic.get() == 0) {
                    List<String> joinList = new ArrayList<>();
                    joinList.add("2220E216-5A30-49AD-A89C-1E0B5AE26AD7");
                    joinList.add("4262AE3F-3202-4487-BEE0-1A0D91307DEB");

                    assertEquals("interval", presence.getEvent());
                    assertEquals(joinList, presence.getJoin());
                    assertTrue(presence.getOccupancy().equals(2));
                    atomic.incrementAndGet();
                }
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribePresencePayloadLeaveDeltaBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14901247588021627\",\"r\":2},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"p\":{\"t\":\"14901247587675704\",\"r\":1},\"k\":\"demo-36\"," +
                        "\"c\":\"moon-interval-deltas-pnpres\",\"d\":{\"action\": \"interval\", \"timestamp\": " +
                        "1490124758, \"occupancy\": 2, \"leave\": [\"2220E216-5A30-49AD-A89C-1E0B5AE26AD7\", " +
                        "\"4262AE3F-3202-4487-BEE0-1A0D91307DEB\"]},\"b\":\"moon-interval-deltas-pnpres\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                if (atomic.get() == 0) {
                    List<String> leaveList = new ArrayList<>();

                    leaveList.add("2220E216-5A30-49AD-A89C-1E0B5AE26AD7");
                    leaveList.add("4262AE3F-3202-4487-BEE0-1A0D91307DEB");

                    assertEquals("interval", presence.getEvent());
                    assertEquals(leaveList, presence.getLeave());
                    assertTrue(presence.getOccupancy().equals(2));
                    atomic.incrementAndGet();
                }
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribePresencePayloadTimeoutDeltaBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14901247588021627\",\"r\":2},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"p\":{\"t\":\"14901247587675704\",\"r\":1},\"k\":\"demo-36\"," +
                        "\"c\":\"moon-interval-deltas-pnpres\",\"d\":{\"action\": \"interval\", \"timestamp\": " +
                        "1490124758, \"occupancy\": 2, \"timeout\": [\"2220E216-5A30-49AD-A89C-1E0B5AE26AD7\", " +
                        "\"4262AE3F-3202-4487-BEE0-1A0D91307DEB\"]},\"b\":\"moon-interval-deltas-pnpres\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                if (atomic.get() == 0) {
                    List<String> timeoutList = new ArrayList<>();
                    timeoutList.add("2220E216-5A30-49AD-A89C-1E0B5AE26AD7");
                    timeoutList.add("4262AE3F-3202-4487-BEE0-1A0D91307DEB");

                    assertEquals("interval", presence.getEvent());
                    assertEquals(timeoutList, presence.getTimeout());
                    assertTrue(presence.getOccupancy().equals(2));
                    atomic.incrementAndGet();
                }
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribePresencePayloadBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14614512228786519\",\"r\":1},\"m\":" +
                        "[{\"a\":\"4\",\"f\":0,\"p\":{\"t\":\"14614512228418349\",\"r\":2}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":" +
                        "\"coolChannel-pnpres\",\"d\":{\"action\": \"join\", \"timestamp\": 1461451222, " +
                        "\"uuid\": \"4a6d5df7-e301-4e73-a7b7-6af9ab484eb0\", " +
                        "\"occupancy\": 1},\"b\":\"coolChannel-pnpres\"}]}\n")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                if (atomic.get() == 0) {
                    assertEquals("join", presence.getEvent());
                    assertEquals("4a6d5df7-e301-4e73-a7b7-6af9ab484eb0", presence.getUuid());
                    assertTrue(presence.getOccupancy().equals(1));
                    assertTrue(presence.getTimestamp().equals(1461451222L));
                    atomic.incrementAndGet();
                }
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribePresenceStateCallback() {
        final AtomicBoolean atomic = new AtomicBoolean();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch10,ch10-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14637536741734954\",\"r\":1},\"m\":" +
                        "[{\"a\":\"4\",\"f\":512,\"p\":{\"t\":\"14637536740940378\",\"r\":1}," +
                        "\"k\":\"demo-36\",\"c\":\"ch10-pnpres\",\"d\":" +
                        "{\"action\": \"join\", \"timestamp\": 1463753674, \"uuid\": " +
                        "\"24c9bb19-1fcd-4c40-a6f1-522a8a1329ef\", \"occupancy\": 3},\"b\":\"ch10-pnpres\"}" +
                        ",{\"a\":\"4\",\"f\":512,\"p\":{\"t\":\"14637536741726901\",\"r\":1},\"k\":\"" +
                        "demo-36\",\"c\":\"ch10-pnpres\",\"d\":{\"action\": \"state-change\", " +
                        "\"timestamp\": 1463753674, \"data\": {\"state\": \"cool\"}, " +
                        "\"uuid\": \"24c9bb19-1fcd-4c40-a6f1-522a8a1329ef\", " +
                        "\"occupancy\": 3},\"b\":\"ch10-pnpres\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                if (presence.getEvent().equals("state-change")) {
                    if (presence.getState().getAsJsonObject().has("state") &&
                            presence.getState().getAsJsonObject().get("state").getAsString().equals("cool")) {
                        atomic.set(true);
                    }
                }
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch10")).withPresence().execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(atomic,
                org.hamcrest.core.IsEqual.equalTo(true));

    }

    @Test
    public void testSubscribeRegionBuilder() {
        final AtomicBoolean atomic = new AtomicBoolean();
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":8},\"m\":" +
                        "[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}" +
                        ",\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":" +
                        "{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                if (requests.size() > 1) {
                    assertEquals("8", requests.get(1).queryParameter("tr").firstValue());
                    atomic.set(true);
                }
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(true));

    }

    @Test
    public void testRemoveListener() {

        final AtomicInteger atomic = new AtomicInteger(0);

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                atomic.addAndGet(1);
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                atomic.addAndGet(1);
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                atomic.addAndGet(1);
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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
        };

        pubnub.addListener(sub1);
        pubnub.removeListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(0));

    }

    @Test
    public void testUnsubscribe() throws InterruptedException {
        final CountDownLatch statusReceived = new CountDownLatch(1);
        final CountDownLatch messageReceived = new CountDownLatch(1);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":" +
                        "[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":" +
                        "{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":" +
                        "[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":" +
                        "{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"," +
                        " \"action\": \"leave\"}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {

                if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    pubnub.unsubscribe().channels(Arrays.asList("ch1")).execute();
                }

                List<String> affectedChannels = status.getAffectedChannels();

                assert affectedChannels != null;

                if (affectedChannels.size() == 1 && status.getOperation() == PNOperationType.PNUnsubscribeOperation) {
                    if (affectedChannels.get(0).equals("ch1")) {
                        statusReceived.countDown();
                    }
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(
                        urlMatching("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres/0.*")));

                if (!requests.isEmpty()) {
                    messageReceived.countDown();
                }

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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
        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        assertTrue(statusReceived.await(2, TimeUnit.SECONDS));
        assertTrue(messageReceived.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void testAllHeartbeats() throws InterruptedException {

        pubnub.getConfiguration().setPresenceTimeout(20);
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.ALL);
        final CountDownLatch statusReceived = new CountDownLatch(1);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":" +
                        "[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":" +
                        "{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"," +
                        " \"action\": \"leave\"}")));

        pubnub.addListener(operationStatusReceivedListener(PNOperationType.PNHeartbeatOperation, statusReceived));

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        assertTrue(statusReceived.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void testAllHeartbeatsViaPresence() throws InterruptedException {

        pubnub.getConfiguration().setPresenceTimeout(20);
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.ALL);
        final CountDownLatch statusReceived = new CountDownLatch(1);


        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"," +
                        " \"action\": \"leave\"}")));

        pubnub.addListener(operationStatusReceivedListener(PNOperationType.PNHeartbeatOperation, statusReceived));

        pubnub.presence().channels(Arrays.asList("ch1", "ch2")).connected(true).execute();

        assertTrue(statusReceived.await(2, TimeUnit.SECONDS));
    }

    @Test
    public void testAllHeartbeatsLeaveViaPresence() throws InterruptedException {

        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.ALL);

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1,ch2/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"," +
                        " \"action\": \"leave\"}")));
        final CountDownLatch statusReceived = new CountDownLatch(1);

        pubnub.addListener(operationStatusReceivedListener(PNOperationType.PNUnsubscribeOperation, statusReceived));

        pubnub.presence().channels(Arrays.asList("ch1", "ch2")).connected(false).execute();

        assertTrue(statusReceived.await(2, TimeUnit.SECONDS));
    }

    SubscribeCallback operationStatusReceivedListener(PNOperationType operationType, CountDownLatch statusReceived) {
        return new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getOperation() == operationType && !status.isError()) {
                    statusReceived.countDown();
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

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
        };
    }

    @Test
    public void testSuccessOnFailureVerbosityHeartbeats() {

        final AtomicBoolean statusRecieved = new AtomicBoolean();
        pubnub.getConfiguration().setPresenceTimeout(20);
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.FAILURES);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":" +
                        "[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":" +
                        "{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));


        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getOperation() == PNOperationType.PNHeartbeatOperation) {
                    statusRecieved.set(true);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

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


        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();


        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusRecieved,
                org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void testFailedHeartbeats() {

        final AtomicBoolean statusRecieved = new AtomicBoolean();
        pubnub.getConfiguration().setPresenceTimeout(20);
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.ALL);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":" +
                        "[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":" +
                        "{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getOperation() == PNOperationType.PNHeartbeatOperation && status.isError()) {
                    statusRecieved.set(true);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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
        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();


        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusRecieved,
                org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void testSilencedHeartbeats() {

        final AtomicBoolean statusRecieved = new AtomicBoolean();
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.NONE);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":" +
                        "[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}" +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":" +
                        "{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getOperation() == PNOperationType.PNHeartbeatOperation) {
                    statusRecieved.set(true);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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
        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();


        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusRecieved,
                org.hamcrest.core.IsEqual.equalTo(false));
    }

    @Test
    public void testFailedNoneHeartbeats() {
        final AtomicBoolean statusRecieved = new AtomicBoolean(false);

        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.NONE);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", " +
                        "\"action\": \"leave\"}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getOperation() != PNOperationType.PNHeartbeatOperation) {
                    statusRecieved.set(true);
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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
        };

        pubnub.addListener(sub1);
        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(4, TimeUnit.SECONDS).untilTrue(statusRecieved);
    }

    @Test
    public void testHeartbeatsDisabled() {
        final AtomicBoolean subscribeSuccess = new AtomicBoolean();
        final AtomicBoolean heartbeatFail = new AtomicBoolean(false);

        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.ALL);

        assertEquals(PNHeartbeatNotificationOptions.ALL, pubnub.getConfiguration().getHeartbeatNotificationOptions());
        assertEquals(300, pubnub.getConfiguration().getPresenceTimeout());
        assertEquals(0, pubnub.getConfiguration().getHeartbeatInterval());

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch1,ch1-pnpres/0"))
                .willReturn(aResponse()
                        .withBody("{\"t\":{\"t\":null,\"r\":12},\"m\":[]}")
                        .withStatus(200)));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"status\": 200, \"message\": \"OK\", \"service\":\"Presence\"}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (!status.isError()) {
                    if (status.getOperation() == PNOperationType.PNSubscribeOperation) {
                        subscribeSuccess.set(true);
                    }
                    if (status.getOperation() == PNOperationType.PNHeartbeatOperation) {
                        heartbeatFail.set(true);
                    }
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe()
                .channels(Arrays.asList("ch1"))
                .withPresence()
                .execute();

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> subscribeSuccess.get() && !heartbeatFail.get());
    }

    @Test
    public void testHeartbeatsEnabled() {
        final AtomicBoolean subscribeSuccess = new AtomicBoolean();
        final AtomicBoolean heartbeatSuccess = new AtomicBoolean();

        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.ALL);

        assertEquals(PNHeartbeatNotificationOptions.ALL, pubnub.getConfiguration().getHeartbeatNotificationOptions());
        assertEquals(300, pubnub.getConfiguration().getPresenceTimeout());
        assertEquals(0, pubnub.getConfiguration().getHeartbeatInterval());

        pubnub.getConfiguration().setPresenceTimeout(20);

        assertEquals(20, pubnub.getConfiguration().getPresenceTimeout());
        assertEquals(9, pubnub.getConfiguration().getHeartbeatInterval());

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch1,ch1-pnpres/0"))
                .willReturn(aResponse()
                        .withBody("{\"t\":{\"t\":null,\"r\":12},\"m\":[]}")
                        .withStatus(200)));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/heartbeat"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("{\"status\": 200, \"message\": \"OK\", \"service\":\"Presence\"}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (!status.isError()) {
                    if (status.getOperation() == PNOperationType.PNSubscribeOperation) {
                        subscribeSuccess.set(true);
                    }
                    if (status.getOperation() == PNOperationType.PNHeartbeatOperation) {
                        heartbeatSuccess.set(true);
                    }
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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

        pubnub.subscribe()
                .channels(Collections.singletonList("ch1"))
                .withPresence()
                .execute();

        Awaitility.await()
                .atMost(5, TimeUnit.SECONDS)
                .until(() -> subscribeSuccess.get() && heartbeatSuccess.get());
    }

    @Test
    public void testMinimumPresenceValueNoInterval() {
        pubnub.getConfiguration().setPresenceTimeout(10);
        assertEquals(20, pubnub.getConfiguration().getPresenceTimeout());
        assertEquals(9, pubnub.getConfiguration().getHeartbeatInterval());
    }

    @Test
    public void testMinimumPresenceValueWithInterval() {
        pubnub.getConfiguration().setPresenceTimeoutWithCustomInterval(10, 50);
        assertEquals(20, pubnub.getConfiguration().getPresenceTimeout());
        assertEquals(50, pubnub.getConfiguration().getHeartbeatInterval());
    }

    @Test
    public void testUnsubscribeAll() {
        final AtomicBoolean statusRecieved = new AtomicBoolean(false);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1,ch2-pnpres,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\"," +
                        "\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1}," +
                        "\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\"," +
                        "\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", " +
                        "\"action\": \"leave\"}")));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", " +
                        "\"action\": \"leave\"}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {

                if (status.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    pubnub.unsubscribe().channels(Arrays.asList("ch1")).execute();
                }

                assert status.getAffectedChannels() != null;

                List<String> affectedChannels = status.getAffectedChannels();

                if (affectedChannels != null && affectedChannels.size() == 1 &&
                        status.getOperation() == PNOperationType.PNUnsubscribeOperation) {
                    if (affectedChannels.get(0).equals("ch1")) {
                        pubnub.unsubscribe().channels(Arrays.asList("ch2")).execute();
                    }
                }


                if (affectedChannels != null && affectedChannels.size() == 1 &&
                        status.getOperation() == PNOperationType.PNUnsubscribeOperation) {
                    if (affectedChannels.get(0).equals("ch2")) {
                        statusRecieved.set(true);
                    }
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult signal) {

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
        };

        pubnub.addListener(sub1);
        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(4, TimeUnit.SECONDS).untilTrue(statusRecieved);
    }


    private String joinArray(String[] arr) {
        StringBuilder builder = new StringBuilder();
        for (String s : arr) {
            if (builder.length() != 0) {
                builder.append(",");
            }
            builder.append(s);
        }
        return builder.toString();
    }
}
