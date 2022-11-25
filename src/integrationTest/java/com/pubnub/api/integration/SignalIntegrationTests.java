package com.pubnub.api.integration;

import com.google.gson.Gson;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.integration.util.BaseIntegrationTest;
import com.pubnub.api.integration.util.RandomGenerator;
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
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.pubnub.api.integration.util.Utils.random;
import static com.pubnub.api.integration.util.Utils.randomChannel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class SignalIntegrationTests extends BaseIntegrationTest {

    @Override
    protected void onBefore() {
    }

    @Override
    protected void onAfter() {

    }

    @Test
    public void testPublishSignalMessageAsync() throws PubNubException {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedChannel = randomChannel();
        final String expectedPayload = RandomGenerator.newValue(5);
        String userIdValue = pubNub.getConfiguration().getUserId().getValue();

        pubNub.signal()
                .message(expectedPayload)
                .channel(expectedChannel)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assertEquals(PNOperationType.PNSignalOperation, status.getOperation());
                    assertEquals(status.getUuid(), userIdValue);
                    assertNotNull(result);
                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testPublishSignalMessageSync() throws PubNubException {
        final String expectedChannel = randomChannel();
        final String expectedPayload = RandomGenerator.newValue(5);

        final PNPublishResult signalResult = pubNub.signal()
                .message(expectedPayload)
                .channel(expectedChannel)
                .sync();

        assertNotNull(signalResult);
    }

    @Test
    public void testReceiveSignalMessage() throws PubNubException {
        final AtomicBoolean success = new AtomicBoolean();

        final String expectedChannel = randomChannel();
        final String expectedPayload = RandomGenerator.newValue(5);

        final PubNub observerClient = getPubNub();
        String userIdValue= pubNub.getConfiguration().getUserId().getValue();

        observerClient.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getOperation() == PNOperationType.PNSubscribeOperation) {
                    assert status.getAffectedChannels() != null;
                    if (status.getAffectedChannels().contains(expectedChannel)) {
                        pubNub.signal()
                                .message(expectedPayload)
                                .channel(expectedChannel)
                                .async((result, status1) -> {
                                    assertFalse(status1.isError());
                                    assertEquals(PNOperationType.PNSignalOperation, status1.getOperation());
                                    assertEquals(status1.getUuid(), userIdValue);
                                    assertNotNull(result);
                                });
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
                assertEquals(userIdValue, signal.getPublisher());
                assertEquals(expectedChannel, signal.getChannel());
                assertEquals(expectedPayload, new Gson().fromJson(signal.getMessage(), String.class));
                success.set(true);
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

        observerClient.subscribe()
                .channels(Collections.singletonList(expectedChannel))
                .execute();

        Awaitility.await().atMost(Durations.FIVE_SECONDS).untilTrue(success);
    }

    @Test
    public void testPublishSignalMessageSyncWithoutChannel() {
        try {
            pubNub.signal()
                    .message(randomChannel())
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING.getMessage(), e.getPubnubError()
                    .getMessage());
        }
    }

    @Test
    public void testPublishSignalMessageSyncWithoutMessage() {
        try {
            pubNub.signal()
                    .channel(randomChannel())
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PubNubErrorBuilder.PNERROBJ_MESSAGE_MISSING.getMessage(), e.getPubnubError()
                    .getMessage());
        }
    }

    @Test
    public void testPublishSignalMessageSyncWithoutSubKey() {
        try {
            pubNub.getConfiguration().setSubscribeKey(null);

            pubNub.signal()
                    .channel(randomChannel())
                    .message(random())
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING.getMessage(), e.getPubnubError()
                    .getMessage());
        }
    }
}
