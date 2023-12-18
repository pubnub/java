package com.pubnub.api.integration;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.integration.util.BaseIntegrationTest;
import com.pubnub.api.integration.util.RandomGenerator;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.history.HistoryMessageType;
import com.pubnub.api.models.consumer.history.PNFetchMessageItem;
import com.pubnub.api.models.consumer.history.PNFetchMessagesResult;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import com.pubnub.api.models.consumer.message_actions.PNAddMessageActionResult;
import com.pubnub.api.models.consumer.message_actions.PNMessageAction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_HISTORY_MESSAGE_ACTIONS_MULTIPLE_CHANNELS;
import static com.pubnub.api.integration.util.Utils.publishMixed;
import static com.pubnub.api.integration.util.Utils.queryParam;
import static com.pubnub.api.integration.util.Utils.random;
import static com.pubnub.api.integration.util.Utils.randomChannel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class HistoryIntegrationTest extends BaseIntegrationTest {

    @Test
    public void testHistorySingleChannel() throws PubNubException {
        final String expectedChannelName = randomChannel();
        final int expectedMessageCount = 10;

        assertEquals(expectedMessageCount,
                publishMixed(pubNub,
                        expectedMessageCount,
                        expectedChannelName).size());

        final PNHistoryResult historyResult = pubNub.history()
                .channel(expectedChannelName)
                .sync();

        assert historyResult != null;
        for (PNHistoryItemResult message : historyResult.getMessages()) {
            assertNotNull(message.getEntry());
            assertNull(message.getMeta());
            assertNull(message.getTimetoken());
        }
    }

    @Test
    public void testHistorySingleChannel_TimeToken() throws PubNubException {
        final String expectedChannelName = random();
        final int expectedMessageCount = 10;

        assertEquals(expectedMessageCount,
                publishMixed(pubNub, expectedMessageCount, expectedChannelName).size());

        final PNHistoryResult historyResult = pubNub.history()
                .channel(expectedChannelName)
                .includeTimetoken(true)
                .sync();

        assert historyResult != null;
        for (PNHistoryItemResult message : historyResult.getMessages()) {
            assertNotNull(message.getEntry());
            assertNotNull(message.getTimetoken());
            assertNull(message.getMeta());
        }
    }

    @Test
    public void testHistorySingleChannel_Meta() throws PubNubException {
        final String expectedChannelName = random();
        final int expectedMessageCount = 10;

        assertEquals(expectedMessageCount,
                publishMixed(pubNub, expectedMessageCount, expectedChannelName).size());

        final PNHistoryResult historyResult = pubNub.history()
                .channel(expectedChannelName)
                .includeMeta(true)
                .sync();

        assert historyResult != null;
        for (PNHistoryItemResult message : historyResult.getMessages()) {
            assertNotNull(message.getEntry());
            assertNull(message.getTimetoken());
            assertNotNull(message.getMeta());
        }
    }

    @Test
    public void testHistorySingleChannel_Meta_Timetoken() throws PubNubException {
        final String expectedChannelName = random();
        final int expectedMessageCount = 10;

        assertEquals(expectedMessageCount,
                publishMixed(pubNub, expectedMessageCount, expectedChannelName).size());

        final PNHistoryResult historyResult = pubNub.history()
                .channel(expectedChannelName)
                .includeMeta(true)
                .includeTimetoken(true)
                .sync();

        assert historyResult != null;
        for (PNHistoryItemResult message : historyResult.getMessages()) {
            assertNotNull(message.getEntry());
            assertNotNull(message.getTimetoken());
            assertNotNull(message.getMeta());
        }
    }

    @Test
    public void testFetchSingleChannel() throws PubNubException {
        final String expectedChannelName = random();

        publishMixed(pubNub, 10, expectedChannelName);

        final PNFetchMessagesResult fetchMessagesResult = pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .maximumPerChannel(25)
                .sync();

        pause(3);

        assert fetchMessagesResult != null;
        for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertNotNull(messageItem.getMessage());
            assertNotNull(messageItem.getTimetoken());
            assertNull(messageItem.getMeta());
            assertNull(messageItem.getActions());
        }

    }

    @Test
    public void testFetchSingleChannel_Meta() throws PubNubException {
        final String expectedChannelName = random();

        publishMixed(pubNub, 10, expectedChannelName);

        pause(3);

        final PNFetchMessagesResult fetchMessagesResult = pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .maximumPerChannel(25)
                .includeMeta(true)
                .sync();

        assert fetchMessagesResult != null;
        for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertNotNull(messageItem.getMessage());
            assertNotNull(messageItem.getTimetoken());
            assertNotNull(messageItem.getMeta());
            assertNull(messageItem.getActions());
        }

    }

    @Test
    public void testFetchSingleChannel_Actions() throws PubNubException {
        final String expectedChannelName = random();

        final List<PNPublishResult> results = publishMixed(pubNub, 120, expectedChannelName);

        pubNub.addMessageAction()
                .channel(expectedChannelName)
                .messageAction(new PNMessageAction()
                        .setType("reaction")
                        .setValue(RandomGenerator.emoji())
                        .setMessageTimetoken(results.get(0).getTimetoken()))
                .sync();

        final PNFetchMessagesResult fetchMessagesResult = pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .maximumPerChannel(25)
                .includeMessageActions(true)
                .includeMeta(false)
                .sync();

        assert fetchMessagesResult != null;
        for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertNotNull(messageItem.getMessage());
            assertNotNull(messageItem.getTimetoken());
            assertNull(messageItem.getMeta());
            if (messageItem.getTimetoken().equals(results.get(0).getTimetoken())) {
                assertNotNull(messageItem.getActions());
            } else {
                assertTrue(messageItem.getActions().isEmpty());
            }
        }
    }

    @Test
    public void testFetchSingleChannel_ActionsMeta() throws PubNubException {
        final String expectedChannelName = random();

        final List<PNPublishResult> results = publishMixed(pubNub, 2, expectedChannelName);

        pubNub.addMessageAction()
                .channel(expectedChannelName)
                .messageAction(new PNMessageAction()
                        .setType("reaction")
                        .setValue(RandomGenerator.emoji())
                        .setMessageTimetoken(results.get(0).getTimetoken()))
                .sync();

        pause(3);

        final PNFetchMessagesResult fetchMessagesResult = pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .maximumPerChannel(25)
                .includeMessageActions(true)
                .includeMeta(true)
                .sync();

        assert fetchMessagesResult != null;
        for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertNotNull(messageItem.getMessage());
            assertNotNull(messageItem.getTimetoken());
            assertNotNull(messageItem.getMeta());
            if (messageItem.getTimetoken().equals(results.get(0).getTimetoken())) {
                assertNotNull(messageItem.getActions());
            } else {
                assertTrue(messageItem.getActions().isEmpty());
            }
        }
    }

    @Test
    public void testFetchMultiChannel() throws PubNubException {
        final String[] expectedChannelNames = new String[]{
                random(),
                random()
        };

        for (String expectedChannelName : expectedChannelNames) {
            publishMixed(pubNub, 10, expectedChannelName);
        }

        final PNFetchMessagesResult fetchMessagesResult = pubNub.fetchMessages()
                .channels(Arrays.asList(expectedChannelNames))
                .maximumPerChannel(25)
                .sync();

        for (String expectedChannelName : expectedChannelNames) {
            assert fetchMessagesResult != null;
            for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannelName)) {
                assertNotNull(messageItem.getMessage());
                assertNotNull(messageItem.getTimetoken());
                assertNull(messageItem.getMeta());
                assertNull(messageItem.getActions());
            }
        }

    }

    @Test
    public void testFetchSingleChannel_NoLimit() throws PubNubException {
        final String expectedChannelName = random();

        assertEquals(10, publishMixed(pubNub, 10, expectedChannelName).size());

        pause(3);

        final PNFetchMessagesResult fetchMessagesResult = pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .sync();

        assert fetchMessagesResult != null;
        assertEquals(10, fetchMessagesResult.getChannels().get(expectedChannelName).size());

        for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertNotNull(messageItem.getMessage());
            assertNotNull(messageItem.getTimetoken());
            assertNull(messageItem.getMeta());
            assertNull(messageItem.getActions());
        }
    }

    @Test
    public void testFetchSingleChannel_OverflowLimit() throws PubNubException {
        final String expectedChannelName = random();

        assertEquals(10, publishMixed(pubNub, 10, expectedChannelName).size());

        pause(3);

        final PNFetchMessagesResult fetchMessagesResult = pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .maximumPerChannel(100)
                .sync();

        assert fetchMessagesResult != null;
        assertEquals(10, fetchMessagesResult.getChannels().get(expectedChannelName).size());

        for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertNotNull(messageItem.getMessage());
            assertNotNull(messageItem.getTimetoken());
            assertNull(messageItem.getMeta());
            assertNull(messageItem.getActions());
        }
    }

    @Test
    public void testHistorySingleChannel_IncludeAll_Crypto() throws PubNubException {
        final String expectedCipherKey = random();
        pubNub.getConfiguration().setCryptoModule(CryptoModule.createLegacyCryptoModule(expectedCipherKey, true));

        final PubNub observer = getPubNub();
        observer.getConfiguration().setCryptoModule(CryptoModule.createLegacyCryptoModule(expectedCipherKey, true));

        final String expectedChannelName = random();
        final int expectedMessageCount = 10;

        assertEquals(expectedMessageCount,
                publishMixed(pubNub, expectedMessageCount, expectedChannelName).size());

        final PNHistoryResult historyResult = observer.history()
                .channel(expectedChannelName)
                .includeTimetoken(true)
                .includeMeta(true)
                .sync();

        assert historyResult != null;
        for (PNHistoryItemResult message : historyResult.getMessages()) {
            assertNotNull(message.getEntry());
            assertNotNull(message.getTimetoken());
            assertNotNull(message.getMeta());
            assertTrue(message.getEntry().toString().contains("_msg"));
        }
    }

    @Test
    public void testReadUnencryptedMessage_FromHistory_WithCrypto() throws PubNubException {
        final String expectedCipherKey = random();

        final PNConfiguration config = getBasicPnConfiguration();
        config.setCryptoModule(CryptoModule.createLegacyCryptoModule(expectedCipherKey, true));;
        final PubNub observer = getPubNub(config);

        final String expectedChannelName = random();
        final int expectedMessageCount = 10;

        assertEquals(expectedMessageCount,
                publishMixed(pubNub, expectedMessageCount, expectedChannelName).size());

        final PNHistoryResult historyResult = observer.history()
                .channel(expectedChannelName)
                .includeTimetoken(true)
                .includeMeta(true)
                .sync();

        assert historyResult != null;
        for (PNHistoryItemResult message : historyResult.getMessages()) {
            assertNotNull(message.getEntry());
            assertNotNull(message.getTimetoken());
            assertNotNull(message.getMeta());
            assertTrue(message.getEntry().toString().contains("_msg"));
            assertEquals(message.getError(), PubNubErrorBuilder.PNERROBJ_PNERR_CRYPTO_IS_CONFIGURED_BUT_MESSAGE_IS_NOT_ENCRYPTED);
        }
    }

    @Test
    public void testReadUnencryptedMessage_FetchMessages_WithCrypto() throws PubNubException {
        final String expectedCipherKey = random();

        final PubNub observer = getPubNub();
        observer.getConfiguration().setCryptoModule(CryptoModule.createLegacyCryptoModule(expectedCipherKey, true));

        final String expectedChannelName = random();
        final int expectedMessageCount = 10;

        assertEquals(expectedMessageCount,
                publishMixed(pubNub, expectedMessageCount, expectedChannelName).size());

        final PNFetchMessagesResult fetchMessagesResult = observer.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .maximumPerChannel(25)
                .includeMeta(true)
                .sync();

        assert fetchMessagesResult != null;
        for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertNotNull(messageItem.getMessage());
            assertNotNull(messageItem.getTimetoken());
            assertNotNull(messageItem.getMeta());
            assertTrue(messageItem.getMessage().toString().contains("_msg"));
            assertEquals(messageItem.getError(), PubNubErrorBuilder.PNERROBJ_PNERR_CRYPTO_IS_CONFIGURED_BUT_MESSAGE_IS_NOT_ENCRYPTED);
        }
    }

    @Test
    public void testFetchSingleChannel_IncludeAll_Crypto() throws PubNubException {
        final String expectedCipherKey = random();
        pubNub.getConfiguration().setCryptoModule(CryptoModule.createLegacyCryptoModule(expectedCipherKey, false));

        final PubNub observer = getPubNub();
        observer.getConfiguration().setCryptoModule(CryptoModule.createLegacyCryptoModule(expectedCipherKey, false));


        final String expectedChannelName = random();
        final int expectedMessageCount = 10;

        assertEquals(expectedMessageCount,
                publishMixed(pubNub, expectedMessageCount, expectedChannelName).size());

        final PNFetchMessagesResult fetchMessagesResult = observer.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .includeMeta(true)
                .sync();

        assert fetchMessagesResult != null;
        for (PNFetchMessageItem message : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertNotNull(message.getMessage());
            assertNotNull(message.getTimetoken());
            assertNotNull(message.getMeta());
            assertNull(message.getActions());
            assertTrue(message.getMessage().toString().contains("_msg"));
        }
    }

    @Test
    public void testFetchSingleChannel_WithActions_IncludeAll_Crypto() throws PubNubException {
        final String expectedCipherKey = random();
        pubNub.getConfiguration().setCipherKey(expectedCipherKey);

        final PubNub observer = getPubNub();
        observer.getConfiguration().setCipherKey(expectedCipherKey);

        assertEquals(pubNub.getConfiguration().getCipherKey(), observer.getConfiguration().getCipherKey()); //todo

        final String expectedChannelName = random();
        final int expectedMessageCount = 10;

        final List<PNPublishResult> mixed = publishMixed(pubNub, expectedMessageCount, expectedChannelName);
        assertEquals(expectedMessageCount, mixed.size());

        final List<Long> messagesWithActions = new ArrayList<>();

        for (int i = 0; i < mixed.size(); i++) {
            if (i % 2 == 0) {
                final PNAddMessageActionResult reaction = pubNub.addMessageAction()
                        .channel(expectedChannelName)
                        .messageAction(new PNMessageAction()
                                .setType("reaction")
                                .setValue(RandomGenerator.emoji())
                                .setMessageTimetoken(mixed.get(i).getTimetoken()))
                        .sync();
                assert reaction != null;
                messagesWithActions.add(reaction.getMessageTimetoken());
            }
        }

        final PNFetchMessagesResult fetchMessagesResult = observer.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .includeMeta(true)
                .includeMessageActions(true)
                .sync();

        assert fetchMessagesResult != null;
        for (PNFetchMessageItem message : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertNotNull(message.getMessage());
            assertNotNull(message.getTimetoken());
            assertNotNull(message.getMeta());
            if (messagesWithActions.contains(message.getTimetoken())) {
                assertNotNull(message.getActions());
            } else {
                assertTrue(message.getActions().isEmpty());
            }
            assertTrue(message.getMessage().toString().contains("_msg"));
        }
    }

    @Test
    public void testFetchMultiChannel_WithMessageActions_Exception() {
        try {
            pubNub.fetchMessages()
                    .channels(Arrays.asList(random(), random()))
                    .includeMessageActions(true)
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERROBJ_HISTORY_MESSAGE_ACTIONS_MULTIPLE_CHANNELS, e.getPubnubError());
        }
    }

    @Test
    public void testFetchSingleChannel_NoActions_Limit_Default() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(random()))
                .async((result, status) -> {
                    assertEquals("100", queryParam(status, "max"));
                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testFetchSingleChannel_NoActions_Limit_Low() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(random()))
                .maximumPerChannel(-1)
                .async((result, status) -> {
                    assertEquals("100", queryParam(status, "max"));
                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testFetchSingleChannel_NoActions_Limit_Valid() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(random()))
                .maximumPerChannel(15)
                .async((result, status) -> {
                    assertEquals("15", queryParam(status, "max"));
                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testFetchSingleChannel_NoActions_Limit_High() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(random()))
                .maximumPerChannel(100)
                .async((result, status) -> {
                    assertEquals("100", queryParam(status, "max"));
                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testFetchSingleChannel_WithActions_Limit_Default() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(random()))
                .includeMessageActions(true)
                .async((result, status) -> {
                    assertEquals("25", queryParam(status, "max"));
                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testFetchSingleChannel_WithActions_Limit_Low() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(random()))
                .includeMessageActions(true)
                .maximumPerChannel(-1)
                .async((result, status) -> {
                    assertEquals("25", queryParam(status, "max"));
                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testFetchSingleChannel_WithActions_Limit_High() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(random()))
                .includeMessageActions(true)
                .maximumPerChannel(200)
                .async((result, status) -> {
                    assertEquals("25", queryParam(status, "max"));
                    success.set(true);
                });

        listen(success);
    }

    @Test
    public void testFetchSingleChannel_WithActions_Limit_Valid() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(random()))
                .includeMessageActions(true)
                .maximumPerChannel(15)
                .async((result, status) -> {
                    assertEquals("15", queryParam(status, "max"));
                    success.set(true);
                });

        listen(success);
    }


    @Test
    public void testEmptyMeta() throws PubNubException {
        final String channel = random();

        // publish a message without any metadata
        pubNub.publish()
                .message(random())
                .channel(channel)
                .shouldStore(true)
                .sync();

        pause(3);

        // /v2/history
        final PNHistoryResult v2HistoryResult = pubNub.history()
                .channel(channel)
                .includeMeta(true)
                .sync();
        assert v2HistoryResult != null;
        assertEquals(1, v2HistoryResult.getMessages().size());
        assertNotNull(v2HistoryResult.getMessages().get(0).getMeta());

        // /v3/history
        final PNFetchMessagesResult v3HistoryResult = pubNub.fetchMessages()
                .channels(Collections.singletonList(channel))
                .includeMeta(true)
                .sync();
        assert v3HistoryResult != null;
        assertEquals(1, v3HistoryResult.getChannels().get(channel).size());
        assertNotNull(v3HistoryResult.getChannels().get(channel).get(0).getMeta());

        // /v3/history-with-actions
        final PNFetchMessagesResult v3HistoryWithActionsResult = pubNub.fetchMessages()
                .channels(Collections.singletonList(channel))
                .includeMeta(true)
                .includeMessageActions(true)
                .sync();
        assert v3HistoryWithActionsResult != null;
        assertEquals(1, v3HistoryWithActionsResult.getChannels().get(channel).size());
        assertNotNull(v3HistoryWithActionsResult.getChannels().get(channel).get(0).getMeta());

        // three responses from three different APIs will return a non-null meta field
    }

    @Test
    public void testFetchSingleChannel_includeMessageTypeIsFalse() throws PubNubException {
        final String expectedChannelName = random();

        publishMixed(pubNub, 10, expectedChannelName);

        final PNFetchMessagesResult fetchMessagesResult = pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .includeMessageType(false)
                .sync();

        pause(3);

        assert fetchMessagesResult != null;
        for (PNFetchMessageItem messageItem : fetchMessagesResult.getChannels().get(expectedChannelName)) {
            assertEquals(null ,messageItem.getMessageType());
        }
    }
}
