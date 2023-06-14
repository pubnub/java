package com.pubnub.api.integration;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNHeartbeatNotificationOptions;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.integration.util.BaseIntegrationTest;
import com.pubnub.api.integration.util.RandomGenerator;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowOccupantData;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.hamcrest.core.IsEqual;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PresenceIntegrationTests extends BaseIntegrationTest {

    private static final String STATE_CHANGE_EVENT = "state-change";

    @Test
    public void testWhereNow() {
        final AtomicBoolean success = new AtomicBoolean();

        final int expectedChannelsCount = 4;

        final List<String> expectedChannels = new ArrayList<>();
        for (int i = 0; i < expectedChannelsCount; i++) {
            expectedChannels.add(RandomGenerator.get());
        }

        subscribeToChannel(pubNub, expectedChannels);

        pause(TIMEOUT_MEDIUM);

        pubNub.whereNow()
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assert result != null;
                    assertEquals(expectedChannelsCount, result.getChannels().size());
                    for (String channel : result.getChannels()) {
                        assertTrue(expectedChannels.contains(channel));
                    }
                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testGlobalHereNow() {
        final AtomicBoolean success = new AtomicBoolean();

        final int expectedChannelsCount = 2;
        final int expectedClientsCount = 3;

        final List<String> expectedChannels = new ArrayList<>(expectedChannelsCount);
        for (int i = 0; i < expectedChannelsCount; i++) {
            expectedChannels.add(RandomGenerator.get());
        }

        final List<PubNub> clients = new ArrayList<PubNub>(expectedClientsCount) {{
            add(pubNub);
        }};

        for (int i = 1; i <= expectedClientsCount - 1; i++) {
            clients.add(getPubNub());
        }

        for (PubNub client : clients) {
            subscribeToChannel(client, expectedChannels);
        }

        assertEquals(expectedClientsCount, clients.size());

        for (PubNub client : clients) {
            assertEquals(expectedChannelsCount, client.getSubscribedChannels().size());
        }

        pause(TIMEOUT_MEDIUM);

        pubNub.hereNow()
                .includeUUIDs(true)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assert result != null;
                    assertTrue(result.getTotalOccupancy() >= expectedClientsCount);
                    assertTrue(result.getTotalChannels() >= expectedChannelsCount);
                    assertTrue(result.getChannels().size() >= expectedChannelsCount);

                    final List<String> channelsResult = new ArrayList<String>() {{
                        addAll(result.getChannels().keySet());
                    }};

                    assertTrue(channelsResult.containsAll(expectedChannels));

                    for (Map.Entry<String, PNHereNowChannelData> entry : result.getChannels().entrySet()) {
                        if (expectedChannels.contains(entry.getKey())) {
                            assertTrue(entry.getValue().getOccupancy() >= expectedClientsCount);
                            assertTrue(entry.getValue().getOccupants().size() >= expectedClientsCount);
                            final List<PNHereNowOccupantData> occupants = entry.getValue().getOccupants();

                            final List<String> resultUuidList = new ArrayList<>();
                            for (PNHereNowOccupantData occupant : occupants) {
                                resultUuidList.add(occupant.getUuid());
                            }

                            final List<String> expectedUuidList = new ArrayList<>();
                            for (PubNub client : clients) {
                                expectedUuidList.add(client.getConfiguration().getUserId().getValue());
                            }

                            Collections.sort(expectedUuidList);
                            Collections.sort(resultUuidList);

                            assertEquals(expectedUuidList, resultUuidList);
                        }

                    }

                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testHereNow() {
        final AtomicBoolean success = new AtomicBoolean();

        final int expectedChannelsCount = 3;
        final int expectedClientsCount = 4;

        final List<String> expectedChannels = new ArrayList<>(expectedChannelsCount);
        for (int i = 0; i < expectedChannelsCount; i++) {
            expectedChannels.add(RandomGenerator.get());
        }

        final List<PubNub> clients = new ArrayList<PubNub>() {{
            add(pubNub);
        }};
        for (int i = 1; i < expectedClientsCount; i++) {
            clients.add(getPubNub());
        }

        for (PubNub client : clients) {
            subscribeToChannel(client, expectedChannels);
        }

        assertEquals(expectedChannelsCount, expectedChannels.size());
        assertEquals(expectedClientsCount, clients.size());

        pause(TIMEOUT_MEDIUM);

        pubNub.hereNow()
                .channels(expectedChannels)
                .includeUUIDs(true)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assert result != null;
                    assertEquals(expectedChannelsCount, result.getTotalChannels());
                    assertEquals(expectedChannelsCount, result.getChannels().size());
                    assertEquals(expectedChannelsCount * expectedClientsCount, result.getTotalOccupancy());

                    for (Map.Entry<String, PNHereNowChannelData> entry : result.getChannels().entrySet()) {
                        assertTrue(expectedChannels.contains(entry.getKey()));
                        assertTrue(expectedChannels.contains(entry.getValue().getChannelName()));
                        assertEquals(expectedClientsCount, entry.getValue().getOccupancy());
                        assertEquals(expectedClientsCount, entry.getValue().getOccupants().size());
                        for (PNHereNowOccupantData occupant : entry.getValue().getOccupants()) {
                            final String uuid = occupant.getUuid();
                            boolean contains = false;
                            for (PubNub client : clients) {
                                if (client.getConfiguration().getUserId().getValue().equals(uuid)) {
                                    contains = true;
                                    break;
                                }
                            }
                            assertTrue(contains);
                        }
                    }

                    success.set(true);
                });

        listen(success);
    }

    //this test not passes right now because server doesn't emit "state-change" presence event for setState(withHeartbeat)
    //once server change is on Prod modify it by replacing Thread.sleep(10000) with Awaitility.await()
    @Ignore
    @Test
    public void should_setState_withHeartbeat() throws InterruptedException {
//        enableHeartbeatLoop(2);
        boolean WITH_HEARTBEAT_TRUE = true;
        final AtomicInteger hits = new AtomicInteger();
        final int expectedHits = 2;

        final JsonObject expectedStatePayload = generatePayload();
        final String expectedChannel = RandomGenerator.get();

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                System.out.println("---" + status.getCategory());
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                System.out.println("---" + presence.getEvent());
                if (presence.getEvent().equals(STATE_CHANGE_EVENT)
                        && presence.getChannel().equals(expectedChannel)
                        && presence.getUuid().equals(pubNub.getConfiguration().getUserId().getValue())) {
                    assertEquals(expectedStatePayload, presence.getState());
                    hits.incrementAndGet();
                }
            }

            @Override
            public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {

            }

            @Override
            public void uuid(@NotNull final PubNub pubnub, @NotNull final PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull final PubNub pubnub, @NotNull final PNChannelMetadataResult pnChannelMetadataResult) {

            }

            @Override
            public void membership(@NotNull final PubNub pubnub, @NotNull final PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnActionResult) {

            }
        });

        subscribeToChannel(pubNub, expectedChannel);

        pubNub.setPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .state(expectedStatePayload)
                .withHeartbeat(WITH_HEARTBEAT_TRUE)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assert result != null;
                    assertEquals(expectedStatePayload, result.getState());
                });

//        Awaitility.await().atMost(Durations.FIVE_SECONDS).untilAtomic(hits, IsEqual.equalTo(1));
        Thread.sleep(1000);

        pubNub.getPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assert result != null;
                    assertEquals(expectedStatePayload.get("text"), result.getStateByUUID().get(expectedChannel).getAsJsonObject().get("text"));
                    assertEquals(expectedStatePayload.get("info"), result.getStateByUUID().get(expectedChannel).getAsJsonObject().get("info"));
                    assertEquals(expectedStatePayload.get("uncd"), result.getStateByUUID().get(expectedChannel).getAsJsonObject().get("uncd"));
                    hits.incrementAndGet();
                });

//        Awaitility.await().atMost(Durations.FIVE_SECONDS).untilAtomic(hits, IsEqual.equalTo(expectedHits));
        Thread.sleep(1000);
    }

    @Test
    public void testPresenceState() {
        final AtomicInteger hits = new AtomicInteger();
        final int expectedHits = 2;

        final JsonObject expectedStatePayload = generatePayload();
        final String expectedChannel = RandomGenerator.get();

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult presence) {
                if (presence.getEvent().equals(STATE_CHANGE_EVENT)
                        && presence.getChannel().equals(expectedChannel)
                        && presence.getUuid().equals(pubNub.getConfiguration().getUserId().getValue())) {
                    assertEquals(expectedStatePayload, presence.getState());
                    hits.incrementAndGet();
                }
            }

            @Override
            public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {

            }

            @Override
            public void uuid(@NotNull final PubNub pubnub, @NotNull final PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull final PubNub pubnub, @NotNull final PNChannelMetadataResult pnChannelMetadataResult) {

            }

            @Override
            public void membership(@NotNull final PubNub pubnub, @NotNull final PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnActionResult) {

            }
        });

        subscribeToChannel(pubNub, expectedChannel);

        pubNub.setPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .state(expectedStatePayload)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assert result != null;
                    assertEquals(expectedStatePayload, result.getState());
                });

        Awaitility.await().atMost(Durations.FIVE_SECONDS).untilAtomic(hits, IsEqual.equalTo(1));

        pubNub.getPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assert result != null;
                    assertEquals(expectedStatePayload, result.getStateByUUID().get(expectedChannel));
                    hits.incrementAndGet();
                });

        Awaitility.await().atMost(Durations.FIVE_SECONDS).untilAtomic(hits, IsEqual.equalTo(expectedHits));

    }

    @Test
    public void testHeartbeatsDisabled() {
        final AtomicInteger heartbeatCallsCount = new AtomicInteger(0);
        final AtomicBoolean subscribeSuccess = new AtomicBoolean();
        final String expectedChannel = RandomGenerator.get();

        pubNub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.ALL);

        assertEquals(PNHeartbeatNotificationOptions.ALL, pubNub.getConfiguration().getHeartbeatNotificationOptions());
        assertEquals(300, pubNub.getConfiguration().getPresenceTimeout());
        assertEquals(0, pubNub.getConfiguration().getHeartbeatInterval());

        pubNub.getConfiguration().setPresenceTimeoutWithCustomInterval(20, 0);
        assertEquals(20, pubNub.getConfiguration().getPresenceTimeout());
        assertEquals(0, pubNub.getConfiguration().getHeartbeatInterval());

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (!status.isError()) {
                    assert status.getAffectedChannels() != null;
                    if (status.getAffectedChannels().contains(expectedChannel)) {
                        if (status.getOperation() == PNOperationType.PNSubscribeOperation) {
                            subscribeSuccess.set(true);
                        }
                        if (status.getOperation() == PNOperationType.PNHeartbeatOperation) {
                            heartbeatCallsCount.incrementAndGet();
                        }
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
            public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {

            }

            @Override
            public void uuid(@NotNull final PubNub pubnub, @NotNull final PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull final PubNub pubnub, @NotNull final PNChannelMetadataResult pnChannelMetadataResult) {

            }

            @Override
            public void membership(@NotNull PubNub pubNub, @NotNull PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnActionResult) {

            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList(expectedChannel))
                .withPresence()
                .execute();

        Awaitility.await()
                .atMost(22, TimeUnit.SECONDS)
                .pollDelay(21, TimeUnit.SECONDS)
                .until(() -> subscribeSuccess.get() && heartbeatCallsCount.get() == 0);
    }

    @Test
    public void testHeartbeatsEnabled() {
        final AtomicInteger heartbeatCallsCount = new AtomicInteger(0);
        final AtomicBoolean subscribeSuccess = new AtomicBoolean();
        final String expectedChannel = RandomGenerator.get();

        pubNub.getConfiguration().setHeartbeatNotificationOptions(PNHeartbeatNotificationOptions.ALL);

        assertEquals(PNHeartbeatNotificationOptions.ALL, pubNub.getConfiguration().getHeartbeatNotificationOptions());
        assertEquals(300, pubNub.getConfiguration().getPresenceTimeout());
        assertEquals(0, pubNub.getConfiguration().getHeartbeatInterval());

        pubNub.getConfiguration().setPresenceTimeout(20);
        assertEquals(20, pubNub.getConfiguration().getPresenceTimeout());
        assertEquals(9, pubNub.getConfiguration().getHeartbeatInterval());

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (!status.isError()) {
                    assert status.getAffectedChannels() != null;
                    if (status.getAffectedChannels().contains(expectedChannel)) {
                        if (status.getOperation() == PNOperationType.PNSubscribeOperation) {
                            subscribeSuccess.set(true);
                        }
                        if (status.getOperation() == PNOperationType.PNHeartbeatOperation) {
                            heartbeatCallsCount.incrementAndGet();
                        }
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
            public void signal(@NotNull PubNub pubNub, @NotNull PNSignalResult pnSignalResult) {

            }

            @Override
            public void uuid(@NotNull final PubNub pubnub, @NotNull final PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull final PubNub pubnub, @NotNull final PNChannelMetadataResult pnChannelMetadataResult) {

            }

            @Override
            public void membership(@NotNull PubNub pubNub, @NotNull PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnActionResult) {

            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList(expectedChannel))
                .withPresence()
                .execute();

        Awaitility.await()
                .atMost(20, TimeUnit.SECONDS)
                .until(() -> subscribeSuccess.get() && heartbeatCallsCount.get() > 2);
    }

    private void enableHeartbeatLoop(int interval) {
        pubNub.getConfiguration().setPresenceTimeoutWithCustomInterval(20, interval);
    }
}
