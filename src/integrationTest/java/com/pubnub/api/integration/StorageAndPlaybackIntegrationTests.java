package com.pubnub.api.integration;

import com.pubnub.api.PubNubException;
import com.pubnub.api.integration.util.RandomGenerator;
import com.pubnub.api.integration.util.BaseIntegrationTest;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class StorageAndPlaybackIntegrationTests extends BaseIntegrationTest {

    @Override
    protected void onBefore() {

    }

    @Override
    protected void onAfter() {

    }

    @Test
    public void testHistoryMessages() throws PubNubException {
        final AtomicBoolean success = new AtomicBoolean();
        final String messageText = RandomGenerator.newValue(10);
        final String channel = RandomGenerator.newValue(10);

        subscribeToChannel(pubNub, channel);
        publishMessage(pubNub, channel, messageText);
        pause(1);

        String userIdValue = pubNub.getConfiguration().getUserId().getValue();
        pubNub.history()
                .channel(channel)
                .async((result, status) -> {
                    assertNotNull(result);
                    final String message = result.getMessages().get(0).getEntry().toString();
                    assertFalse(status.isError());
                    assertTrue(message.contains(userIdValue));
                    assertTrue(message.contains(messageText));
                    success.set(true);
                });

        success.set(true);
    }

    @Test
    public void testHistoryMessagesWithTimeToken() {
        final AtomicBoolean success = new AtomicBoolean();
        final String channel = RandomGenerator.newValue(10);

        pubNub.subscribe()
                .channels(Collections.singletonList(channel))
                .execute();

        publishMessages(channel, 3);
        pause(1);

        pubNub.history()
                .channel(channel)
                .includeTimetoken(true)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assertNotNull(result);
                    int timeTokenCounter = 0;
                    for (int i = 0; i < result.getMessages().size(); i++) {
                        if (result.getMessages().get(i).getTimetoken() != null) {
                            timeTokenCounter++;
                        }
                    }

                    assertEquals(3, timeTokenCounter);
                    success.set(true);
                });

        success.set(true);
    }

    @Test
    public void testLoadingHistoryMessagesWithLimit() {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedChannel = RandomGenerator.newValue(10);

        subscribeToChannel(pubNub, expectedChannel);
        publishMessages(expectedChannel, 20);
        pause(1);

        pubNub.history()
                .channel(expectedChannel)
                .count(10)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assertNotNull(result);
                    final int numberOfMessages = result.getMessages().size();
                    assertEquals(10, numberOfMessages);
                    success.set(true);
                });

        success.set(true);
    }

    @Test
    public void testLoadingHistoryWithSpecificTimeInterval() {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedChannel = RandomGenerator.newValue(10);

        subscribeToChannel(pubNub, expectedChannel);

        final Long before = System.currentTimeMillis() * 10_000;
        pause(5);
        publishMessages(expectedChannel, 3);
        pause(5);
        final Long now = System.currentTimeMillis() * 10_000;

        pubNub.history()
                .channel(expectedChannel)
                .includeTimetoken(true)
                .start(now)
                .end(before)
                .count(10)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assertNotNull(result);
                    assertEquals(3, result.getMessages().size());
                    success.set(true);
                });
        success.set(true);
    }

    @Test
    public void testReverseHistoryPaging() {
        final AtomicBoolean success = new AtomicBoolean();
        final String channel = RandomGenerator.newValue(10);
        final String message_1 = RandomGenerator.newValue(20);
        final String message_2 = RandomGenerator.newValue(20);

        subscribeToChannel(pubNub, channel);
        publishMessage(pubNub, channel, message_1);
        publishMessage(pubNub, channel, message_2);
        pause(1);

        pubNub.history()
                .channel(channel)
                .count(10)
                .reverse(true)
                .async((result, status) -> {
                    assertFalse(status.isError());
                    assertNotNull(result);
                    if (!result.getMessages().isEmpty()) {
                        final String message = result.getMessages().get(0).getEntry().toString();
                        assertTrue(message.contains(message_1));
                    } else {
                        fail("Messages are empty");
                    }
                    success.set(true);
                });

        success.set(true);
    }

    private void publishMessages(String channel, int counter) {
        for (int i = 0; i < counter; i++) {
            publishMessage(pubNub, channel, RandomGenerator.newValue(10));
        }
    }
}
