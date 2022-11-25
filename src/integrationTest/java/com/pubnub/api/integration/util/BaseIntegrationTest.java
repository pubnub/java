package com.pubnub.api.integration.util;

import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import okhttp3.logging.HttpLoggingInterceptor;
import org.aeonbits.owner.ConfigFactory;
import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.awaitility.pollinterval.FibonacciPollInterval;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;

public abstract class BaseIntegrationTest {

    protected Logger log = LoggerFactory.getLogger(BaseIntegrationTest.class);

    private static String SUB_KEY;
    private static String PUB_KEY;
    private static String PAM_SUB_KEY;
    private static String PAM_PUB_KEY;
    private static String PAM_SEC_KEY;

    public PubNub pubNub;
    public PubNub server;

    public int TIMEOUT_MEDIUM = 5;
    public int TIMEOUT_LOW = 2;

    private List<PubNub> mGuestClients = new ArrayList<>();

    @BeforeClass
    public static void onlyOnce() {
        final ITTestConfig itTestConfig = ConfigFactory.create(ITTestConfig.class, System.getenv());
        SUB_KEY = itTestConfig.subscribeKey();
        PUB_KEY = itTestConfig.publishKey();
        PAM_SUB_KEY = itTestConfig.pamSubKey();
        PAM_PUB_KEY = itTestConfig.pamPubKey();
        PAM_SEC_KEY = itTestConfig.pamSecKey();
    }

    @Before
    public void before() {
        onPrePubNub();
        pubNub = getPubNub();
        if (needsServer()) {
            server = getServer();
        }
        onBefore();
    }

    @After
    public void after() {
        onAfter();
        destroyClient(pubNub);
        if (mGuestClients != null) {
            for (PubNub guestClient : mGuestClients) {
                destroyClient(guestClient);
            }
        }
        // properties.clear();
    }

    public PubNub getPubNub() {
        PNConfiguration pnConfiguration = provideStagingConfiguration();
        if (pnConfiguration == null) {
            pnConfiguration = getBasicPnConfiguration();
        }
        final PubNub pubNub = new PubNub(pnConfiguration);
        registerGuestClient(pubNub);
        return pubNub;
    }

    protected PubNub getServer() {
        final PubNub pubNub = new PubNub(getServerPnConfiguration());
        registerGuestClient(pubNub);
        return pubNub;
    }

    public PubNub getPubNub(PNConfiguration pnConfiguration) {
        final PubNub pubNub = new PubNub(pnConfiguration);
        registerGuestClient(pubNub);
        return pubNub;
    }

    private void registerGuestClient(PubNub guestClient) {
        if (mGuestClients == null) {
            mGuestClients = new ArrayList<>();
        }
        mGuestClients.add(guestClient);
    }

    protected void destroyClient(PubNub client) {
        client.unsubscribeAll();
        client.forceDestroy();
    }

    protected PNConfiguration getBasicPnConfiguration()  {
        final PNConfiguration pnConfiguration;
        try {
            pnConfiguration = new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
            pnConfiguration.setUserId(new UserId("client-".concat(UUID.randomUUID().toString())));
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }
        if (!needsServer()) {
            pnConfiguration.setSubscribeKey(SUB_KEY);
            pnConfiguration.setPublishKey(PUB_KEY);
        } else {
            pnConfiguration.setSubscribeKey(PAM_SUB_KEY);
            pnConfiguration.setPublishKey(PAM_PUB_KEY);
            pnConfiguration.setAuthKey(provideAuthKey());
        }
        pnConfiguration.setLogVerbosity(PNLogVerbosity.NONE);
        pnConfiguration.setHttpLoggingInterceptor(createInterceptor());
        return pnConfiguration;
    }

    private PNConfiguration getServerPnConfiguration(){
        final PNConfiguration pnConfiguration;
        try {
            pnConfiguration = new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
            pnConfiguration.setUserId(new UserId("server-".concat(UUID.randomUUID().toString())));
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }
        pnConfiguration.setSubscribeKey(PAM_SUB_KEY);
        pnConfiguration.setPublishKey(PAM_PUB_KEY);
        pnConfiguration.setSecretKey(PAM_SEC_KEY);
        pnConfiguration.setLogVerbosity(PNLogVerbosity.NONE);
        pnConfiguration.setHttpLoggingInterceptor(createInterceptor());
        return pnConfiguration;
    }

    private HttpLoggingInterceptor createInterceptor() {
        final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(log::debug);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    protected void subscribeToChannel(@NotNull PubNub pubnub, @NotNull String... channels) {
        pubnub.subscribe()
                .channels(Arrays.asList(channels))
                .withPresence()
                .execute();
        pause(1);
    }

    protected void subscribeToChannel(final PubNub pubnub, final List<String> channels) {

        final AtomicBoolean subscribeSuccess = new AtomicBoolean();

        pubnub.addListener(new SubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {

            }
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus status) {
                if (status.getOperation() == PNOperationType.PNSubscribeOperation) {
                    assert status.getAffectedChannels() != null;
                    if (status.getAffectedChannels().containsAll(channels)) {
                        subscribeSuccess.set(true);
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
            public void membership(@NotNull final PubNub pubnub, @NotNull final PNMembershipResult pnMembershipResult) {

            }

            @Override
            public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnActionResult) {

            }

        });

        pubnub.subscribe()
                .channels(channels)
                .withPresence()
                .execute();

        Awaitility.await().atMost(Durations.TEN_SECONDS).untilTrue(subscribeSuccess);
    }

    protected void subscribeToChannelGroup(@NotNull PubNub pubnub, @NotNull String group) {
        pubnub.subscribe()
                .channelGroups(Collections.singletonList(group))
                .withPresence()
                .execute();
        pause(1);
    }

    protected void unsubscribeFromChannel(PubNub pubNub, String channel) {
        pubNub.unsubscribe()
                .channels(Collections.singletonList(channel))
                .execute();
        pause(1);
    }

    protected void unsubscribeFromAllChannels(PubNub pubNub) {
        pubNub.unsubscribeAll();
        pause(1);
    }

    protected Map<String, String> generateMessage(PubNub pubNub, String message) {
        final Map<String, String> map = new HashMap<>();
        String userIdValue = null;
        try {
            userIdValue = pubNub.getConfiguration().getUserId().getValue();
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }
        map.put("publisher", userIdValue);
        map.put("text", "mymsg" + RandomGenerator.newValue(5) + "+" + RandomGenerator.newValue(5));
        map.put("uncd", RandomGenerator.unicode(8));
        map.put("extra", message);
        return map;
    }

    protected JsonObject generateMessage(PubNub pubNub) throws PubNubException {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("publisher", pubNub.getConfiguration().getUserId().getValue());
        jsonObject.addProperty("text", RandomGenerator.newValue(8));
        jsonObject.addProperty("uncd", RandomGenerator.unicode(8));
        return jsonObject;
    }

    protected JsonObject generatePayload() {
        final JsonObject state = new JsonObject();
        state.addProperty("text", RandomGenerator.newValue(10));
        state.addProperty("uncd", RandomGenerator.unicode(8));
        state.addProperty("info", RandomGenerator.newValue(8));
        return state;
    }

    protected JSONObject generatePayloadJSON() {
        final JSONObject state = new JSONObject();
        try {
            state.put("text", RandomGenerator.newValue(10));
            state.put("uncd", RandomGenerator.unicode(8));
            state.put("info", RandomGenerator.newValue(8));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return state;
    }

    protected HashMap<String, Object> generateMap() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("text", RandomGenerator.newValue(10));
        map.put("uncd", RandomGenerator.unicode(8));
        map.put("info", RandomGenerator.newValue(8));
        return map;
    }

    protected void publishMessage(PubNub pubNub, String channel, String message) {
        pubNub.publish()
                .message(generateMessage(pubNub, message))
                .channel(channel)
                .shouldStore(true)
                .async((result, status) -> {

                });
    }

    protected void publishMessage(PubNub pubNub, String channel, String message, Map<String, Object> meta) {
        pubNub.publish()
                .message(generateMessage(pubNub, message))
                .channel(channel)
                .meta(meta)
                .shouldStore(true)
                .async((result, status) -> {

                });
    }

    protected void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1_000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean needsServer() {
        return provideAuthKey() != null;
    }

    protected void listen(AtomicBoolean success) {
        Awaitility.await()
                .atMost(Durations.FIVE_SECONDS)
                .with()
                .untilTrue(success);
    }

    protected void listen(AtomicBoolean success, Callable<Boolean> callable) {
        Awaitility.await()
                .atMost(Durations.TEN_MINUTES)
                .pollInterval(new FibonacciPollInterval(TimeUnit.SECONDS))
                .with()
                .until(callable);
    }

    protected void assertException(PubNubError pubNubError, PubNubException e) {
        assertEquals(pubNubError, e.getPubnubError());
    }

    protected void onBefore() {

    }

    protected void onAfter() {

    }

    protected void onPrePubNub() {

    }

    protected String provideAuthKey() {
        return null;
    }

    protected PNConfiguration provideStagingConfiguration() {
        return null;
    }

}
