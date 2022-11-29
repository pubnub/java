package com.pubnub.api.integration;

import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.integration.util.BaseIntegrationTest;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.hamcrest.core.IsEqual;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pubnub.api.integration.util.Utils.randomChannel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class HeartbeatIntegrationTest extends BaseIntegrationTest {

    private String expectedChannel;

    @Override
    protected void onBefore() {
        expectedChannel = randomChannel();
    }

    @Override
    protected void onAfter() {

    }

    @Test
    public void testStateWithHeartbeat() {
        final AtomicInteger hits = new AtomicInteger();
        final JsonObject expectedStatePayload = generatePayload();
        final PubNub observer = getPubNub();

        pubNub.getConfiguration().setPresenceTimeoutWithCustomInterval(20, 4);

        observer.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pn, @NotNull PNStatus status) {
                if (status.getOperation() == PNOperationType.PNSubscribeOperation) {
                    assert status.getAffectedChannels() != null;
                    if (status.getAffectedChannels().contains(expectedChannel)) {

                        pubNub.subscribe()
                                .channels(Collections.singletonList(expectedChannel))
                                .withPresence()
                                .execute();
                    }
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {

            }

            @Override
            public void presence(@NotNull PubNub p, @NotNull PNPresenceEventResult presence) {
                if (presence.getUuid().equals(pubNub.getConfiguration().getUserId().getValue())
                        && presence.getChannel().equals(expectedChannel)) {
                    switch (presence.getEvent()) {
                        case "state-change":
                            assertEquals(expectedStatePayload, presence.getState());
                            hits.incrementAndGet();
                            pubNub.disconnect();
                            break;
                        case "join":
                            if (presence.getState() == null) {
                                hits.incrementAndGet();

                                final AtomicBoolean stateSet = new AtomicBoolean();

                                pubNub.setPresenceState()
                                        .state(expectedStatePayload)
                                        .channels(Collections.singletonList(expectedChannel))
                                        .async((result, status) -> {
                                            assertFalse(status.isError());
                                            assert result != null;
                                            assertEquals(expectedStatePayload, result.getState());
                                            hits.incrementAndGet();
                                            stateSet.set(true);
                                        });

                                Awaitility.await().atMost(Durations.FIVE_SECONDS).untilTrue(stateSet);

                            } else {
                                assertEquals(expectedStatePayload, presence.getState());
                                hits.incrementAndGet();
                            }
                            break;
                        case "timeout":
                            pubNub.reconnect();
                            break;
                    }
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

        observer.subscribe()
                .channels(Collections.singletonList(expectedChannel))
                .withPresence()
                .execute();

        Awaitility.await()
                .atMost(Duration.ofSeconds(40))
                .untilAtomic(hits, IsEqual.equalTo(4));
    }

}
