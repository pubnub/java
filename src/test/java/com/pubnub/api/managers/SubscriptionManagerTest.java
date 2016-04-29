package com.pubnub.api.managers;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.awaitility.Awaitility;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.PubNub;
import com.pubnub.api.enums.PNHeartbeatNotificationOptions;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.endpoints.TestHarness;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class SubscriptionManagerTest extends TestHarness {

    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
    }

    @After
    public void afterEach() {
        pubnub.stop();
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Test
    public void testSubscribeBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                if (requests.size() == 1){
                    atomic.addAndGet(1);
                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        });


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribeChannelGroupBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                if (requests.size() == 1){
                    assertEquals("cg1,cg2", requests.get(0).queryParameter("channel-group").firstValue());
                    atomic.addAndGet(1);
                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        });


        pubnub.subscribe().channelGroups(Arrays.asList("cg1", "cg2")).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribeChannelGroupWithPresenceBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                if (requests.size() == 1){
                    String[] channelGroups = requests.get(0).queryParameter("channel-group").firstValue().split(",");
                    Arrays.sort(channelGroups);
                    assertEquals("cg1,cg1-pnpres,cg2,cg2-pnpres", joinArray(channelGroups));
                    atomic.addAndGet(1);
                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        });


        pubnub.subscribe().channelGroups(Arrays.asList("cg1", "cg2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribeWithTimeTokenBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                if (requests.size() == 1) {
                    assertEquals("1337", requests.get(0).queryParameter("tt").firstValue());
                    atomic.addAndGet(1);
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        });


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withTimetoken(1337L).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribeWithFilterExpressionBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        pubnub.getConfiguration().setFilterExpression("much=filtering");
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch1/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                if (requests.size() == 1) {
                    assertEquals("much%3Dfiltering", requests.get(0).queryParameter("filter-expr").firstValue());
                    atomic.addAndGet(1);
                }
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        });


        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribePresenceBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                if (requests.size() == 1) {
                    Assert.assertEquals("{text=Enter Message Here}", message.getMessage().toString());
                    atomic.addAndGet(1);
                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        });

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribePresencePayloadBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14614512228786519\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"p\":{\"t\":\"14614512228418349\",\"r\":2},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel-pnpres\",\"d\":{\"action\": \"join\", \"timestamp\": 1461451222, \"uuid\": \"4a6d5df7-e301-4e73-a7b7-6af9ab484eb0\", \"occupancy\": 1},\"b\":\"coolChannel-pnpres\"}]}\n")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                if (atomic.get() == 0) {
                    assertEquals("join", presence.getEvent());
                    assertEquals("4a6d5df7-e301-4e73-a7b7-6af9ab484eb0", presence.getUuid());
                    Assert.assertTrue(presence.getOccupancy().equals(1));
                    Assert.assertTrue(presence.getTimestamp().equals(1461451222L));
                    atomic.incrementAndGet();
                }
            }
        });

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testSubscribeRegionBuilder() {
        final AtomicInteger atomic = new AtomicInteger(0);
        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":8},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe.*")));

                if (requests.size() == 2){
                    assertEquals("8", requests.get(1).queryParameter("tr").firstValue());
                    atomic.addAndGet(1);
                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        });

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @Test
    public void testRemoveListener() {

        final AtomicInteger atomic = new AtomicInteger(0);

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                atomic.addAndGet(1);
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                atomic.addAndGet(1);
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                atomic.addAndGet(1);
            }
        };

        pubnub.addListener(sub1);
        pubnub.removeListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(0));

    }

    @Test
    public void testUnsubscribe() {

        final AtomicBoolean statusRecieved = new AtomicBoolean();
        final AtomicBoolean messageRecieved = new AtomicBoolean();

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/leave"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", \"action\": \"leave\"}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getAffectedChannels().size() == 1 && status.getOperation() == PNOperationType.PNUnsubscribeOperation){
                    if (status.getAffectedChannels().get(0).equals("ch1")) {
                        statusRecieved.set(true);
                    }
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres/0.*")));

                if (!requests.isEmpty()) {
                    messageRecieved.set(true);
                }

            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();

        pubnub.unsubscribe().channels(Arrays.asList("ch1")).execute();

        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(messageRecieved, org.hamcrest.core.IsEqual.equalTo(true));
        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusRecieved, org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void testAllHeartbeats() {

        final AtomicBoolean statusRecieved = new AtomicBoolean();
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.All);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", \"action\": \"leave\"}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getOperation() == PNOperationType.PNHeartbeatOperation && !status.isError()) {
                    statusRecieved.set(true);
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();


        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusRecieved, org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void testSuccessOnFailureVerbosityHeartbeats() {

        final AtomicBoolean statusRecieved = new AtomicBoolean();
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.Failures);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));


        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getOperation() == PNOperationType.PNHeartbeatOperation) {
                    statusRecieved.set(true);
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();


        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusRecieved, org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void testFailedHeartbeats() {

        final AtomicBoolean statusRecieved = new AtomicBoolean();
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.All);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getOperation() == PNOperationType.PNHeartbeatOperation && status.isError()) {
                    statusRecieved.set(true);
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();


        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusRecieved, org.hamcrest.core.IsEqual.equalTo(true));
    }

    @Test
    public void testSilencedHeartbeats() {

        final AtomicBoolean statusRecieved = new AtomicBoolean();
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.None);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getOperation() == PNOperationType.PNHeartbeatOperation) {
                    statusRecieved.set(true);
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();


        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusRecieved, org.hamcrest.core.IsEqual.equalTo(false));
    }

    @Test
    public void testFailedNoneHeartbeats() {

        final AtomicBoolean statusRecieved = new AtomicBoolean();
        pubnub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.None);

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/ch2,ch2-pnpres,ch1,ch1-pnpres/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch2,ch1/heartbeat"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\", \"action\": \"leave\"}")));

        SubscribeCallback sub1 = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getOperation() == PNOperationType.PNHeartbeatOperation) {
                    statusRecieved.set(true);
                }
            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
            }
        };

        pubnub.addListener(sub1);

        pubnub.subscribe().channels(Arrays.asList("ch1", "ch2")).withPresence().execute();


        Awaitility.await().atMost(2, TimeUnit.SECONDS).untilAtomic(statusRecieved, org.hamcrest.core.IsEqual.equalTo(false));
    }


    private String joinArray(String[] arr) {
        StringBuilder builder = new StringBuilder();
        for(String s : arr) {
            if (builder.length() != 0) {
                builder.append(",");
            }
            builder.append(s);
        }
        return builder.toString();
    }
}
