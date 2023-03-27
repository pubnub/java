package com.pubnub.api.integration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.integration.util.BaseIntegrationTest;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.history.PNFetchMessageItem;
import com.pubnub.api.models.consumer.history.PNFetchMessagesResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pubnub.api.integration.util.Utils.random;
import static com.pubnub.api.integration.util.Utils.randomChannel;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PublishIntegrationTests extends BaseIntegrationTest {

    @Override
    protected void onBefore() {

    }

    @Override
    protected void onAfter() {

    }

    @Test
    public void testPublishMessage() {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedChannel = randomChannel();
        final JsonObject messagePayload = generateMessage(pubNub);

        pubNub.publish()
                .message(messagePayload)
                .channel(expectedChannel)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assertEquals(status.getUuid(), pubNub.getConfiguration().getUserId().getValue());
                    success.set(true);
                });

        Awaitility.await().atMost(Durations.FIVE_SECONDS).untilTrue(success);
    }

    @Test
    public void testPublishMessageWithTypeAndSpaceId() throws PubNubException {
        final String expectedChannel = randomChannel();
        final JsonObject messagePayload = generateMessage(pubNub);
        SpaceId expectedSpaceId = new SpaceId("chatIndeed");
        String expectedType = "userChosenType";

        PNPublishResult pnPublishResult = pubNub.publish()
                .message(messagePayload)
                .channel(expectedChannel)
                .type(expectedType)
                .spaceId(expectedSpaceId)
                .sync();

        assertNotNull(pnPublishResult.getTimetoken());

        final PNFetchMessagesResult fetchMessagesResult = pauseUntilAsserted(() -> {
            PNFetchMessagesResult result = pubNub.fetchMessages()
                    .channels(Collections.singletonList(expectedChannel))
                    .maximumPerChannel(25)
                    .includeMeta(true)
                    .includeMessageType(true)
                    .includeSpaceId(true)
                    .sync();
            assertNotNull(result);
            assertThat(result.getChannels().size(), greaterThan(0));
            return result;
        });

        for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannel)) {
            assertEquals(expectedSpaceId, messageItem.getSpaceId());
            assertEquals(expectedType, messageItem.getType());
        }
    }

    @Test
    public void testPublishMessageHistory() throws PubNubException {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedChannel = randomChannel();
        final JSONObject messagePayload = new JSONObject();

        try {
            messagePayload.put("name", "joe");
            messagePayload.put("age", 48);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final JsonObject whatToExpect = pubNub.getMapper().convertValue(messagePayload, JsonObject.class);

        pubNub.publish()
                .channel(expectedChannel)
                .message(messagePayload)
                .sync();

        pause(2);

        pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannel))
                .maximumPerChannel(1)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assert result != null;
                    assertEquals(1, result.getChannels().size());
                    assertEquals(1, result.getChannels().get(expectedChannel).size());
                    assertEquals(whatToExpect, result.getChannels().get(expectedChannel).get(
                            0).getMessage());
                    success.set(true);
                });

        Awaitility.await().atMost(Durations.TEN_SECONDS).untilTrue(success);
    }

    @Test
    public void testPublishMessageNoHistory() {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedChannel = randomChannel();
        final JsonObject messagePayload = generateMessage(pubNub);

        pubNub.publish()
                .message(messagePayload)
                .channel(expectedChannel)
                .shouldStore(false)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assertEquals(status.getUuid(), pubNub.getConfiguration().getUserId().getValue());
                });

        pause(2);

        pubNub.history()
                .count(1)
                .channel(expectedChannel)
                .async((pnHistoryResult, pnStatus) -> {
                    assertFalse(pnStatus.isError());
                    assert pnHistoryResult != null;
                    assertEquals(0, pnHistoryResult.getMessages().size());
                    success.set(true);
                });

        Awaitility.await().atMost(Durations.TEN_SECONDS).untilTrue(success);
    }

    @Test
    public void testReceiveMessage() {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedChannel = randomChannel();
        final JsonObject messagePayload = generateMessage(pubNub);

        final PubNub observer = getPubNub();

        this.pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getOperation() == PNOperationType.PNSubscribeOperation) {
                    assert status.getAffectedChannels() != null;
                    if (status.getAffectedChannels().contains(expectedChannel)) {
                        observer.publish()
                                .message(messagePayload)
                                .channel(expectedChannel)
                                .async((result, status1) -> assertFalse(status1.isError()));
                    }
                }
            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult message) {
                assertEquals(expectedChannel, message.getChannel());
                assertEquals(observer.getConfiguration().getUserId().getValue(), message.getPublisher());
                assertEquals(messagePayload, message.getMessage());
                success.set(true);
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

        subscribeToChannel(pubNub, expectedChannel);

        Awaitility.await().atMost(Durations.TEN_SECONDS).untilTrue(success);
    }

    @Test
    public void testOrgJsonObject_Get_History() throws PubNubException, JSONException {
        final String channel = random();

        final JSONObject payload = new JSONObject();
        payload.put("name", "John Doe");
        payload.put("city", "San Francisco");

        pubNub.publish()
                .message(payload)
                .channel(channel)
                .usePOST(true)
                .sync();

        final PNHistoryResult historyResult = pauseUntilAsserted(() -> {
            PNHistoryResult result = pubNub.history()
                    .channel(channel)
                    .count(1)
                    .sync();

            assertNotNull(result);
            assertThat(result.getMessages().size(), greaterThan(0));
            return result;
        });

        final JsonElement receivedMessage = historyResult.getMessages().get(0).getEntry();

        final JSONObject receivedObject = new JSONObject(receivedMessage.toString());

        assertEquals(payload.toString(), receivedObject.toString());
    }

    @Test
    public void testOrgJsonObject_Post_History() throws PubNubException, JSONException {
        final String channel = random();

        final JSONObject payload = generatePayloadJSON();

        pubNub.publish()
                .message(payload)
                .channel(channel)
                .usePOST(true)
                .sync();

        final PNHistoryResult historyResult = pauseUntilAsserted(() -> {
            PNHistoryResult result = pubNub.history()
                    .channel(channel)
                    .count(1)
                    .sync();

            assertNotNull(result);
            assertThat(result.getMessages().size(), greaterThan(0));
            return result;
        });
        assert historyResult != null;
        final JsonElement receivedMessage = historyResult.getMessages().get(0).getEntry();

        final JSONObject receivedObject = new JSONObject(receivedMessage.toString());

        assertEquals(payload.toString(), receivedObject.toString());
    }

    @Test
    public void testOrgJsonObject_Get_Receive() throws PubNubException {
        final String channel = random();
        final String expectedType = "test";
        final SpaceId expectedSpaceId = new SpaceId("1-to-1_chat");
        final JSONObject payload = generatePayloadJSON();
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
                final JsonElement receivedMessage = pnMessageResult.getMessage();
                assertEquals(expectedType, pnMessageResult.getType());
                assertEquals(expectedSpaceId.getValue(), pnMessageResult.getSpaceId().getValue());
                try {
                    final JSONObject receivedObject = new JSONObject(receivedMessage.toString());
                    assertEquals(payload.toString(), receivedObject.toString());
                    success.set(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

            }

            @Override
            public void uuid(@NotNull final PubNub pubnub, @NotNull final PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull final PubNub pubnub, @NotNull final PNChannelMetadataResult pnChannelMetadataResult) {

            }


            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList(channel))
                .execute();

        pause(3);

        pubNub.publish()
                .message(payload)
                .channel(channel)
                .type(expectedType)
                .spaceId(expectedSpaceId)
                .sync();

        listen(success);
    }

    @Test
    public void testOrgJsonObject_Post_Receive() throws PubNubException {
        final String channel = random();
        final JSONObject payload = generatePayloadJSON();
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
                final JsonElement receivedMessage = pnMessageResult.getMessage();
                try {
                    final JSONObject receivedObject = new JSONObject(receivedMessage.toString());
                    assertEquals(payload.toString(), receivedObject.toString());
                    success.set(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

            }

            @Override
            public void uuid(@NotNull final PubNub pubnub, @NotNull final PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull final PubNub pubnub, @NotNull final PNChannelMetadataResult pnChannelMetadataResult) {

            }


            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList(channel))
                .execute();

        pause(3);

        pubNub.publish()
                .message(payload)
                .channel(channel)
                .usePOST(true)
                .sync();

        listen(success);
    }

    @Test
    public void testOrgJsonArray_Get_History() throws PubNubException, JSONException {
        final String channel = random();

        final JSONArray payload = new JSONArray();
        for (int i = 0; i < 3; i++) {
            payload.put(generatePayloadJSON());
        }

        pubNub.publish()
                .message(payload)
                .channel(channel)
                .sync();

        final PNHistoryResult historyResult = pauseUntilAsserted(() -> {
            PNHistoryResult result = pubNub.history()
                    .channel(channel)
                    .count(1)
                    .sync();

            assertNotNull(result);
            assertThat(result.getMessages().size(), greaterThan(0));
            return result;
        });

        final JsonElement receivedMessage = historyResult.getMessages().get(0).getEntry();

        final JSONArray receivedArray = new JSONArray(receivedMessage.toString());

        assertEquals(payload.toString(), receivedArray.toString());
    }

    @Test
    public void testOrgJsonArray_Post_History() throws PubNubException, JSONException {
        final String channel = random();

        final JSONArray payload = new JSONArray();
        for (int i = 0; i < 3; i++) {
            payload.put(generatePayloadJSON());
        }

        pubNub.publish()
                .message(payload)
                .channel(channel)
                .usePOST(true)
                .sync();

        final PNHistoryResult historyResult = pauseUntilAsserted(() -> {
            PNHistoryResult result = pubNub.history()
                    .channel(channel)
                    .count(1)
                    .sync();

            assertNotNull(result);
            assertThat(result.getMessages().size(), greaterThan(0));
            return result;
        });

        final JsonElement receivedMessage = historyResult.getMessages().get(0).getEntry();

        final JSONArray receivedArray = new JSONArray(receivedMessage.toString());

        assertEquals(payload.toString(), receivedArray.toString());
    }

    @Test
    public void testOrgJsonArray_Get_Receive() throws PubNubException {
        final String channel = random();

        final JSONArray payload = new JSONArray();
        for (int i = 0; i < 3; i++) {
            payload.put(generatePayloadJSON());
        }

        final AtomicBoolean success = new AtomicBoolean();

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
                final JsonElement receivedMessage = pnMessageResult.getMessage();
                assertNull(pnMessageResult.getType());
                try {
                    final JSONArray receivedArray = new JSONArray(receivedMessage.toString());
                    assertEquals(payload.toString(), receivedArray.toString());
                    success.set(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

            }

            @Override
            public void uuid(@NotNull final PubNub pubnub, @NotNull final PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull final PubNub pubnub, @NotNull final PNChannelMetadataResult pnChannelMetadataResult) {

            }


            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList(channel))
                .execute();

        pause(3);

        pubNub.publish()
                .message(payload)
                .channel(channel)
                .sync();

        listen(success);
    }

    @Test
    public void testOrgJsonArray_Post_Receive() throws PubNubException {
        final String channel = random();

        final JSONArray payload = new JSONArray();
        for (int i = 0; i < 3; i++) {
            payload.put(generatePayloadJSON());
        }

        final AtomicBoolean success = new AtomicBoolean();

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
                final JsonElement receivedMessage = pnMessageResult.getMessage();
                try {
                    final JSONArray receivedArray = new JSONArray(receivedMessage.toString());
                    assertEquals(payload.toString(), receivedArray.toString());
                    success.set(true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

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
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList(channel))
                .execute();

        pause(3);

        pubNub.publish()
                .message(payload)
                .channel(channel)
                .usePOST(true)
                .sync();

        listen(success);
    }

    @Test
    public void testOrgJson_Combo() throws PubNubException, JSONException {
        final String channel = random();

        final JSONObject payload = new JSONObject();
        payload.put("key_1", generatePayloadJSON());
        payload.put("key_2", generatePayloadJSON());
        payload.put("z_1", new JSONObject(payload.toString()));
        payload.put("a_2", new JSONObject(payload.toString()));
        payload.put("d_3", new JSONObject(payload.toString()));

        final JSONArray jsonArray = new JSONArray();
        jsonArray.put(generatePayloadJSON());
        jsonArray.put(generatePayloadJSON());
        jsonArray.put(generatePayloadJSON());

        payload.put("z_array", jsonArray);

        final AtomicInteger count = new AtomicInteger();

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }

            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {
                final JsonElement receivedMessage = pnMessageResult.getMessage();
                try {
                    final JSONObject receivedObject = new JSONObject(receivedMessage.toString());
                    assertEquals(payload.toString(), receivedObject.toString());
                    count.incrementAndGet();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

            }

            @Override
            public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

            }

            @Override
            public void uuid(@NotNull final PubNub pubnub, @NotNull final PNUUIDMetadataResult pnUUIDMetadataResult) {

            }

            @Override
            public void channel(@NotNull final PubNub pubnub, @NotNull final PNChannelMetadataResult pnChannelMetadataResult) {

            }

            @Override
            public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList(channel))
                .execute();

        pause(3);

        pubNub.publish()
                .message(payload)
                .channel(channel)
                .usePOST(true)
                .sync();

        final PNFetchMessagesResult historyResult = pauseUntilAsserted(() -> {
            PNFetchMessagesResult result = pubNub.fetchMessages()
                    .channels(Collections.singletonList(channel))
                    .maximumPerChannel(1)
                    .sync();

            assertNotNull(result);
            assertThat(result.getChannels().size(), greaterThan(0));
            return result;
        });

        final JsonElement receivedMessage = historyResult.getChannels().get(channel).get(0).getMessage();

        final JSONObject receivedObject = new JSONObject(receivedMessage.toString());
        assertEquals(payload.toString(), receivedObject.toString());
        count.incrementAndGet();

        Awaitility.await()
                .atMost(Durations.FIVE_SECONDS)
                .with()
                .untilAtomic(count, IsEqual.equalTo(2));
    }
}
