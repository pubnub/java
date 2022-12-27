package com.pubnub.api.integration.pam;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.endpoints.access.Grant;
import com.pubnub.api.integration.util.BaseIntegrationTest;
import com.pubnub.api.integration.util.RandomGenerator;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.message_actions.PNAddMessageActionResult;
import com.pubnub.api.models.consumer.message_actions.PNMessageAction;
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
import org.awaitility.pollinterval.FibonacciPollInterval;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.pubnub.api.enums.PNStatusCategory.PNAccessDeniedCategory;
import static com.pubnub.api.enums.PNStatusCategory.PNAcknowledgmentCategory;
import static com.pubnub.api.integration.util.Utils.random;
import static com.pubnub.api.integration.util.Utils.randomChannel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

abstract class AccessManagerIntegrationTest extends BaseIntegrationTest {

    static final boolean addDelays = false;

    static final String LEVEL_APP = "subkey";
    static final String LEVEL_USER = "user";
    static final String LEVEL_CHANNEL = "channel";
    static final String LEVEL_UUID = "uuid";

    static final int READ = 1;
    static final int WRITE = 2;
    static final int MANAGE = 4;
    static final int DELETE = 8;
    static final int GET = 16;
    static final int UPDATE = 32;
    static final int JOIN = 64;

    private String expectedChannel;
    private String authKey;
    private String uuid;

    @Override
    protected void onPrePubNub() {
        log.warn("onPrePubNub");
        expectedChannel = randomChannel();
        authKey = "auth_".concat(random());
        authKey = authKey.toLowerCase();
    }

    @Override
    protected void onBefore() {
        pubNub.getConfiguration().setIncludeInstanceIdentifier(false);
        pubNub.getConfiguration().setIncludeRequestIdentifier(false);
        if (performOnServer()) {
            pubNub = server;
        }
        log.warn("performOnServer: " + performOnServer());

        if (addDelays) {
            pause(3);
        }
    }

    @Override
    protected void onAfter() {
        revokeAllAccess();
    }

    @Override
    protected String provideAuthKey() {
        return authKey;
    }

    @Test
    public void testGetPublishMessageWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(WRITE);
        pubNub.publish()
                .channel(expectedChannel)
                .message(generatePayload())
                .async((result, status) -> {
                    try {
                        requestAccess(WRITE);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testGetPublishMessageWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.publish()
                .channel(expectedChannel)
                .message(generateMap())
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testPostPublishMessageWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(WRITE);
        pubNub.publish()
                .channel(expectedChannel)
                .message(generatePayload())
                .usePOST(true)
                .async((result, status) -> {
                    try {
                        requestAccess(WRITE);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testPostPublishMessageWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.publish()
                .channel(expectedChannel)
                .message(generateMap())
                .usePOST(true)
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testMessageCountsWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(READ, WRITE);
        pubNub.messageCounts()
                .channels(Collections.singletonList(expectedChannel))
                .channelsTimetoken(Collections.singletonList(System.currentTimeMillis()))
                .async((result, status) -> {
                    try {
                        requestAccess(READ, WRITE);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }


    @Test
    public void testMessageCountsWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.messageCounts()
                .channels(Collections.singletonList(expectedChannel))
                .channelsTimetoken(Collections.singletonList(System.currentTimeMillis()))
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testHistoryWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(READ);
        pubNub.history()
                .channel(expectedChannel)
                .async((result, status) -> {
                    try {
                        requestAccess(READ);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testHistoryWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.history()
                .channel(expectedChannel)
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testPublishHistoryWithPermission() {
        final AtomicBoolean publishSuccess = new AtomicBoolean();
        final AtomicBoolean retrieveSuccess = new AtomicBoolean();

        final JsonObject expectedMessagePayload = generatePayload();

        requestAccess(READ);
        pubNub.publish()
                .channel(expectedChannel)
                .message(expectedMessagePayload)
                .shouldStore(true)
                .async((result, status) -> {
                    try {
                        requestAccess(READ);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        publishSuccess.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(publishSuccess);

        revokeAllAccess();

        requestAccess(WRITE);
        pubNub.history()
                .channel(expectedChannel)
                .async((result, status) -> {
                    try {
                        requestAccess(WRITE);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        assertNotNull(result);
                        assertNotNull(result.getMessages());
                        assertEquals(1, result.getMessages().size());
                        assertEquals(expectedMessagePayload, result.getMessages().get(0).getEntry());
                        retrieveSuccess.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(retrieveSuccess);
    }

    @Test
    public void testHereNowWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(READ);
        pubNub.hereNow()
                .channels(Collections.singletonList(expectedChannel))
                .async((result, status) -> {
                    try {
                        requestAccess(READ);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testHereNowWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.hereNow()
                .channels(Collections.singletonList(expectedChannel))
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testSetStateWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();
        final JsonObject expectedStatePayload = generatePayload();

        pubNub.setPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .uuid(pubNub.getConfiguration().getUserId().getValue())
                .state(expectedStatePayload)
                .async((pnSetStateResult, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testSetStateWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();
        final JsonObject expectedStatePayload = generatePayload();

        requestAccess(READ);

        pubNub.setPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .uuid(pubNub.getConfiguration().getUserId().getValue())
                .state(expectedStatePayload)
                .async((result, status) -> {
                    try {
                        requestAccess(READ);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        assertNotNull(result);
                        assertEquals(expectedStatePayload, result.getState());
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testGetSetStateWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.getPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .uuid(pubNub.getConfiguration().getUserId().getValue())
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testGetStateWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(READ);
        pubNub.getPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .uuid(pubNub.getConfiguration().getUserId().getValue())
                .async((result, status) -> {
                    try {
                        requestAccess(READ);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testStateComboWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();
        final JsonObject expectedStatePayload = generatePayload();

        requestAccess(READ);
        pubNub.setPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .uuid(pubNub.getConfiguration().getUserId().getValue())
                .state(expectedStatePayload)
                .async((result, status) -> {
                    try {
                        requestAccess(READ);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        assertNotNull(result);
                        assertEquals(expectedStatePayload, result.getState());
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        pause(2);

        pubNub.getPresenceState()
                .channels(Collections.singletonList(expectedChannel))
                .uuid(pubNub.getConfiguration().getUserId().getValue())
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        assertNotNull(result);
                        assertEquals(expectedStatePayload, result.getStateByUUID().get(expectedChannel));
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testPresenceWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(READ);

        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }
            @Override
            public void status(@NotNull PubNub pubNub, @NotNull PNStatus pnStatus) {

            }

            @Override
            public void message(@NotNull PubNub pubNub, @NotNull PNMessageResult pnMessageResult) {

            }

            @Override
            public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {
                if ((pnPresenceEventResult.getEvent().equals("join"))
                        && (pnPresenceEventResult.getChannel().equals(expectedChannel))) {
                    if (pnPresenceEventResult.getUuid().equals(server.getConfiguration().getUserId().getValue())) {
                        success.set(true);
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

        subscribeToChannel(pubNub, expectedChannel);
        subscribeToChannel(server, expectedChannel);

        observePam(success);
    }

    @Test
    public void testPublishSignalWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedPayload = RandomGenerator.newValue(5);

        requestAccess(WRITE);
        pubNub.signal()
                .channel(expectedChannel)
                .message(expectedPayload)
                .async((result, status) -> {
                    try {
                        requestAccess(WRITE);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testPublishSignalWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();
        final String expectedPayload = RandomGenerator.newValue(5);

        pubNub.signal()
                .channel(expectedChannel)
                .message(expectedPayload)
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testDeleteMessageWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(DELETE);
        pubNub.deleteMessages()
                .channels(Collections.singletonList(expectedChannel))
                .async((result, status) -> {
                    try {
                        requestAccess(DELETE);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testDeleteMessageWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.deleteMessages()
                .channels(Collections.singletonList(expectedChannel))
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }


    @Test
    public void testFetchMessagesWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(READ);
        pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannel))
                .async((result, status) -> {
                    try {
                        requestAccess(READ);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testFetchMessagesWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannel))
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testFetchMessageActionsWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(READ);
        pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannel))
                .includeMessageActions(true)
                .async((result, status) -> {
                    try {
                        requestAccess(READ);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testFetchMessageActionsWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.fetchMessages()
                .channels(Collections.singletonList(expectedChannel))
                .includeMessageActions(true)
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testAddAMessageActionWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(WRITE);
        pubNub.addMessageAction()
                .channel(expectedChannel)
                .messageAction(new PNMessageAction()
                        .setType("reaction")
                        .setValue(RandomGenerator.emoji())
                        .setMessageTimetoken(1L))
                .async((result, status) -> {
                    try {
                        requestAccess(WRITE);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testAddMessageActionWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.addMessageAction()
                .channel(expectedChannel)
                .messageAction(new PNMessageAction()
                        .setType("reaction")
                        .setValue(RandomGenerator.emoji())
                        .setMessageTimetoken(1L))
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testGetMessageActionsWithPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(READ);
        pubNub.getMessageActions()
                .channel(expectedChannel)
                .async((result, status) -> {
                    try {
                        requestAccess(READ);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testGetMessageActionsWithoutPermission() {
        final AtomicBoolean success = new AtomicBoolean();

        pubNub.getMessageActions()
                .channel(expectedChannel)
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testRemoveMessageActionWithPermission() throws PubNubException {
        final AtomicBoolean success = new AtomicBoolean();

        requestAccess(WRITE);
        final PNAddMessageActionResult addMessageActionResult = pubNub.addMessageAction()
                .channel(expectedChannel)
                .messageAction(new PNMessageAction()
                        .setType("reaction")
                        .setValue(RandomGenerator.emoji())
                        .setMessageTimetoken(1L))
                .sync();
        assertNotNull(addMessageActionResult);

        revokeAllAccess();

        requestAccess(DELETE);
        pubNub.removeMessageAction()
                .channel(expectedChannel)
                .messageTimetoken(addMessageActionResult.getMessageTimetoken())
                .actionTimetoken(addMessageActionResult.getActionTimetoken())
                .async((result, status) -> {
                    try {
                        requestAccess(DELETE);
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusSuccess(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    @Test
    public void testRemoveMessageActionWithoutPermission() throws PubNubException {
        requestAccess(WRITE);

        pause(TIMEOUT_MEDIUM);

        PNPublishResult publishResult = pubNub.publish()
                .channel(expectedChannel)
                .message(random())
                .shouldStore(true)
                .sync();

        assertNotNull(publishResult);
        PNAddMessageActionResult addMessageActionResult = pubNub.addMessageAction()
                .channel(expectedChannel)
                .messageAction(new PNMessageAction()
                        .setType("reaction")
                        .setValue(RandomGenerator.emoji())
                        .setMessageTimetoken(publishResult.getTimetoken()))
                .sync();

        revokeAllAccess();

        final AtomicBoolean success = new AtomicBoolean();

        assertNotNull(addMessageActionResult);
        pubNub.removeMessageAction()
                .channel(expectedChannel)
                .messageTimetoken(addMessageActionResult.getMessageTimetoken())
                .actionTimetoken(addMessageActionResult.getActionTimetoken())
                .async((result, status) -> {
                    try {
                        assertAuthKey(status);
                        assertUuid(status);
                        assertStatusError(status);
                        assertCategory(status);
                        success.set(true);
                    } catch (AssertionError | Exception e) {
                        e.printStackTrace();
                        retry(status);
                    }
                });

        observePam(success);
    }

    private void revokeAllAccess() {
        requestAccess();
    }

    private void requestAccess(Integer... bitmasks) {
        int sum = 0;
        for (Integer bitmask : bitmasks) {
            sum += bitmask;
        }
        boolean revokeAllAccess = sum == 0;

        if (performOnServer()) {
            return;
        }

        if (!revokeAllAccess) {
            log.info(String.format("Requesting access for %1$s at %2$s level", sum, getPamLevel()));
        } else {
            log.info("Revoking all access!");
        }

        final List<Integer> bitList = Arrays.asList(bitmasks);

        final Grant grantOperationBuilder = getServer().grant()
                .read(bitList.contains(READ))
                .write(bitList.contains(WRITE))
                .manage(bitList.contains(MANAGE))
                .delete(bitList.contains(DELETE))
                .get(bitList.contains(GET))
                .update(bitList.contains(UPDATE))
                .join(bitList.contains(JOIN))
                .ttl(1);

        if (!revokeAllAccess) {
            switch (getPamLevel()) {
                case LEVEL_USER:
                    grantOperationBuilder.authKeys(Collections.singletonList(authKey));
                    grantOperationBuilder.channels(Arrays.asList(expectedChannel, expectedChannel.concat("-pnpres")));
                    break;
                case LEVEL_CHANNEL:
                    grantOperationBuilder.channels(Arrays.asList(expectedChannel, expectedChannel.concat("-pnpres")));
                    break;
            }
        }

        try {
            final PNAccessManagerGrantResult grantResult = grantOperationBuilder.sync();
            assertNotNull(grantResult);
            log.info(String.format("Access request result: %s", new Gson().toJson(grantResult)));
            if (revokeAllAccess) {
                assertEquals(LEVEL_APP, grantResult.getLevel());
            } else {
                assertEquals(getPamLevel(), grantResult.getLevel());
                if (!getPamLevel().equals(LEVEL_APP)) {
                    assertTrue(grantResult.getChannels().containsKey(expectedChannel));
                    assertTrue(grantResult.getChannels().containsKey(expectedChannel.concat("-pnpres")));
                }
            }
        } catch (PubNubException e) {
            e.printStackTrace();
        }

        if (addDelays) {
            pause(2);
        }
    }

    private void observePam(final AtomicBoolean success) {
        Awaitility.await()
                .atMost(Durations.TEN_SECONDS)
                .pollInterval(new FibonacciPollInterval(TimeUnit.SECONDS))
                .untilTrue(success);
    }


    private void assertAuthKey(PNStatus status) throws AssertionError {
        if (!performOnServer()) {
            assertEquals(authKey, status.getAuthKey());
        }
    }

    private void assertStatusError(PNStatus status) throws AssertionError {
        if (!performOnServer()) {
            assertTrue(status.isError());
        } else {
            assertFalse(status.isError());
        }
    }

    private void assertStatusSuccess(PNStatus status) throws AssertionError {
        if (!performOnServer()) {
            assertFalse(status.isError());
        } else {
            assertFalse(status.isError());
        }
    }

    private void assertCategory(PNStatus status) throws AssertionError {
        if (!performOnServer()) {
            assertEquals(PNAccessDeniedCategory, status.getCategory());
        } else {
            assertEquals(PNAcknowledgmentCategory, status.getCategory());
        }
    }

    private void assertUuid(PNStatus pnStatus) throws AssertionError {
        assertEquals(pubNub.getConfiguration().getUserId().getValue(), pnStatus.getUuid());
    }

    abstract String getPamLevel();

    boolean performOnServer() {
        return false;
    }

    private void retry(PNStatus pnStatus) {
        // this causes OOMs
        // pnStatus.retry();
    }

}
