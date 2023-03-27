package com.pubnub.api.integration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
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
import org.json.JSONException;
import org.json.JSONObject;
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
    public void testPublishSignalMessageAsync() {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedChannel = randomChannel();
        final String expectedPayload = RandomGenerator.newValue(5);

        pubNub.signal()
                .message(expectedPayload)
                .channel(expectedChannel)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assertEquals(PNOperationType.PNSignalOperation, status.getOperation());
                    assertEquals(status.getUuid(), pubNub.getConfiguration().getUserId().getValue());
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
    public void testReceiveSignalMessage() {
        final AtomicBoolean success = new AtomicBoolean();

        final String expectedChannel = randomChannel();
        final String expectedPayload = RandomGenerator.newValue(5);
        final String expectedType = "test";
        final SpaceId expectedSpaceId = new SpaceId("1-to-1_chat");

        final PubNub observerClient = getPubNub();

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
                                .type(expectedType)
                                .spaceId(expectedSpaceId)
                                .async((result, status1) -> {
                                    assertFalse(status1.isError());
                                    assertEquals(PNOperationType.PNSignalOperation, status1.getOperation());
                                    assertEquals(status1.getUuid(), pubNub.getConfiguration().getUserId().getValue());
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
                assertEquals(pubNub.getConfiguration().getUserId().getValue(), signal.getPublisher());
                assertEquals(expectedChannel, signal.getChannel());
                assertEquals(expectedPayload, new Gson().fromJson(signal.getMessage(), String.class));
                assertEquals(expectedType, signal.getType());
                assertEquals(expectedSpaceId.getValue(), signal.getSpaceId().getValue());
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

    @Test
    public void should_receive_signal_when_signal_is_sent() throws PubNubException {
        final String expectedType = "test";
        final SpaceId expectedSpaceId = new SpaceId("1-to-1_chat");
        final JSONObject payload = generatePayloadJSON();
        final AtomicBoolean success = new AtomicBoolean();
        String channel = randomChannel();

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {
                final JsonElement receivedMessage = pnSignalResult.getMessage();
                assertEquals(expectedType, pnSignalResult.getType());
                assertEquals(expectedSpaceId.getValue(), pnSignalResult.getSpaceId().getValue());
                try {
                    final JSONObject receivedObject = new JSONObject(receivedMessage.toString());
                    assertEquals(payload.toString(), receivedObject.toString());
                    success.set(true);
                } catch (JSONException e) {
                    throw new RuntimeException("Can't convert message to JSON");
                }
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
                .channels(Collections.singletonList(channel))
                .execute();

        pause(3);

        pubNub.signal()
                .channel(channel)
                .message(payload)
                .type(expectedType)
                .spaceId(expectedSpaceId)
                .sync();
    }
}
