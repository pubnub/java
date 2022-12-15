package com.pubnub.api.managers;

import com.pubnub.api.PubNub;
import com.pubnub.api.builder.dto.ChangeTemporaryUnavailableOperation;
import com.pubnub.api.builder.dto.PubSubOperation;
import com.pubnub.api.builder.dto.SubscribeOperation;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.managers.subscription.utils.RequestDetails;
import com.pubnub.api.managers.subscription.utils.ResponseHolder;
import com.pubnub.api.managers.subscription.utils.ResponseSupplier;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.server.SubscribeEnvelope;
import com.pubnub.api.models.server.SubscribeMetadata;
import com.pubnub.api.services.SubscribeService;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.awaitility.core.ThrowingRunnable;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import retrofit2.Response;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.pubnub.api.managers.subscription.utils.SubscriptionTestUtils.pubnub;
import static com.pubnub.api.managers.subscription.utils.SubscriptionTestUtils.retrofitManagerMock;
import static com.pubnub.api.managers.subscription.utils.SubscriptionTestUtils.telemetryManager;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class FastSubscriptionManagerTest {

    public static final String FAKE_REGION = "12";
    private final ListenerManager listenerManagerMock = mock(ListenerManager.class);
    private final ReconnectionManager reconnectionManagerMock = mock(ReconnectionManager.class);
    private final DelayedReconnectionManager delayedReconnectionManagerMock = mock(DelayedReconnectionManager.class);

    @Test
    public void performsLongPollingAfterTimeout() throws IllegalAccessException {
        final ResponseSupplier<SubscribeEnvelope> responseSupplier = requestDetails -> new ResponseHolder<>(new SocketTimeoutException(
                "timeout"));

        final RetrofitManager retrofitManagerMock = retrofitManagerMock(responseSupplier);

        final SubscriptionManager subscriptionManager = spy(subscriptionManagerUnderTest(retrofitManagerMock));

        final SubscribeOperation subscribeOperation = SubscribeOperation.builder()
                .channels(singletonList("ch1"))
                .channelGroups(emptyList())
                .build();

        subscriptionManager.adaptSubscribeBuilder(subscribeOperation);

        await().atMost(2, SECONDS).untilAsserted(() -> {
            verify(subscriptionManager, atLeast(2)).startSubscribeLoop(any());
        });
    }

    @Test
    public void reconnectRestartsTheLoopRegardlessOfState() {
        AtomicInteger numberOfCalls = new AtomicInteger(0);
        final ResponseSupplier<SubscribeEnvelope> responseSupplier = requestDetails -> {
            numberOfCalls.incrementAndGet();
            return new ResponseHolder<>(new SocketException(
                    "socket exception"));
        };

        final RetrofitManager retrofitManagerMock = retrofitManagerMock(responseSupplier);

        final SubscriptionManager subscriptionManager = subscriptionManagerUnderTest(retrofitManagerMock);

        final SubscribeOperation subscribeOperation = SubscribeOperation.builder()
                .channels(singletonList("ch1"))
                .channelGroups(emptyList())
                .build();

        subscriptionManager.adaptSubscribeBuilder(subscribeOperation);

        subscriptionManager.reconnect();
        await().atMost(1, SECONDS).untilAsserted(() -> assertEquals(2, numberOfCalls.get()));
    }

    @Test
    public void disconnectsAndSchedulesReconnectionOnUnknownHostException() throws IllegalAccessException {
        final ResponseSupplier<SubscribeEnvelope> responseSupplier = requestDetails -> new ResponseHolder<>(new UnknownHostException(
                "example.com"));

        final RetrofitManager retrofitManagerMock = retrofitManagerMock(responseSupplier);

        final SubscriptionManager subscriptionManager = spy(subscriptionManagerUnderTest(retrofitManagerMock));

        final SubscribeOperation subscribeOperation = SubscribeOperation.builder()
                .channels(singletonList("ch1"))
                .channelGroups(emptyList())
                .build();

        subscriptionManager.adaptSubscribeBuilder(subscribeOperation);

        await().atMost(2, SECONDS).untilAsserted(() -> {
            verify(subscriptionManager, times(1)).disconnect();
            verify(reconnectionManagerMock, times(1)).startPolling();
        });
    }

    @Test
    public void forbiddenChannelsAddedToTemporaryUnavailable() throws IllegalAccessException, InterruptedException {
        final String channel = "ch1";
        final String rawResponseBody = String.format(
                "{\"message\":\"Forbidden\",\"payload\":{\"channels\":[\"%s\"]},\"error\":true,\"service\":\"Access Manager\",\"status\":403}",
                channel);

        final ResponseSupplier<SubscribeEnvelope> responseSupplier = requestDetails -> {
            final ResponseBody responseBody = ResponseBody.create(rawResponseBody, MediaType.parse("json"));
            return new ResponseHolder<>(Response.error(403, responseBody));
        };

        final RetrofitManager retrofitManagerMock = retrofitManagerMock(responseSupplier);

        final SubscriptionManager subscriptionManager = spy(subscriptionManagerUnderTest(retrofitManagerMock));

        subscriptionManager.subscriptionState.handleOperation(subscribeOperation(channel));

        subscriptionManager.reconnect();

        final int connectTimeout = subscriptionManager.pubnub.getConfiguration().getConnectTimeout();

        await().atMost(connectTimeout, SECONDS).untilAsserted(() ->
                assertEquals(emptyList(),
                        subscriptionManager.subscriptionState.subscriptionStateData(false,
                                StateManager.ChannelFilter.WITHOUT_TEMPORARY_UNAVAILABLE).getChannels()));
    }

    @Test
    public void temporaryUnavailableAttemptedToSubscribeAfterSomeTime() {
        final String channel = "ch1";
        final ResponseSupplier<SubscribeEnvelope> responseSupplier = requestDetails -> {
            final SubscribeMetadata subscribeMetadata = new SubscribeMetadata(System.currentTimeMillis(),
                    FAKE_REGION);
            final SubscribeEnvelope subscribeEnvelope = new SubscribeEnvelope(emptyList(),
                    subscribeMetadata);
            return new ResponseHolder<>(Response.success(subscribeEnvelope));
        };

        final RetrofitManager retrofitManagerMock = retrofitManagerMock(responseSupplier);

        final SubscriptionManager subscriptionManager = spy(subscriptionManagerUnderTest(retrofitManagerMock));

        subscriptionManager.subscriptionState.handleOperation(subscribeOperation(channel),
                unavailableOperation(channel));
        final int connectTimeout = subscriptionManager.pubnub.getConfiguration().getConnectTimeout();
        assertEquals(emptyList(),
                subscriptionManager.subscriptionState.subscriptionStateData(false,
                        StateManager.ChannelFilter.WITHOUT_TEMPORARY_UNAVAILABLE).getChannels());

        subscriptionManager.reconnect();

        await().atMost(3 * connectTimeout, SECONDS).untilAsserted(() ->
                assertEquals(singletonList(channel),
                        subscriptionManager.subscriptionState.subscriptionStateData(false,
                                StateManager.ChannelFilter.WITHOUT_TEMPORARY_UNAVAILABLE).getChannels()));
    }

    private PubSubOperation subscribeOperation(String channel) {
        return SubscribeOperation.builder().channels(singletonList(channel)).build();
    }

    private PubSubOperation unavailableOperation(String channel) {
        return ChangeTemporaryUnavailableOperation.builder().unavailableChannel(channel).build();
    }

    private PubSubOperation availableOperation(String channel) {
        return ChangeTemporaryUnavailableOperation.builder().availableChannel(channel).build();
    }

    @Test
    public void noSubscribeOnUnchangedState() {
        long timeToken = System.currentTimeMillis();
        final ResponseSupplier<SubscribeEnvelope> responseSupplier = requestDetails -> {
            final SubscribeEnvelope subscribeEnvelope = new SubscribeEnvelope(emptyList(),
                    new SubscribeMetadata(timeToken, FAKE_REGION));
            try {
                SECONDS.sleep(5);
            } catch (InterruptedException e) {
            }
            return new ResponseHolder<>(Response.success(200, subscribeEnvelope));
        };
        final RetrofitManager retrofitManagerMock = retrofitManagerMock(responseSupplier);
        final SubscribeService spiedSubscribeService = retrofitManagerMock.getSubscribeService();
        final SubscriptionManager subscriptionManager = subscriptionManagerUnderTest(retrofitManagerMock);

        final SubscribeOperation subscribeOperation = SubscribeOperation.builder()
                .channels(singletonList("ch1"))
                .channelGroups(singletonList("group1"))
                .build();

        for (int i = 0; i < new Random().nextInt(10) + 1; i++) {
            subscriptionManager.adaptSubscribeBuilder(subscribeOperation);
        }

        await().atMost(1, SECONDS).untilAsserted(() ->
                verify(spiedSubscribeService, times(1)).subscribe(any(), any(), any())
        );
    }

    @Test
    public void subscribeOnChangedState() {
        long timeToken = System.currentTimeMillis();
        final ResponseSupplier<SubscribeEnvelope> responseSupplier = new ResponseSupplier<SubscribeEnvelope>() {
            @Override
            public ResponseHolder<SubscribeEnvelope> get(final RequestDetails requestDetails) {
                final SubscribeEnvelope subscribeEnvelope = new SubscribeEnvelope(emptyList(),
                        new SubscribeMetadata(timeToken, FAKE_REGION));
                try {
                    SECONDS.sleep(5);
                } catch (InterruptedException e) {
                }
                return new ResponseHolder<>(Response.success(200, subscribeEnvelope));
            }
        };
        final RetrofitManager retrofitManagerMock = retrofitManagerMock(responseSupplier);
        final SubscribeService spiedSubscribeService = retrofitManagerMock.getSubscribeService();
        final SubscriptionManager subscriptionManager = subscriptionManagerUnderTest(retrofitManagerMock);

        final SubscribeOperation subscribeOperation1 = SubscribeOperation.builder()
                .channels(singletonList("ch1"))
                .channelGroups(singletonList("group1"))
                .build();

        final SubscribeOperation subscribeOperation2 = SubscribeOperation.builder()
                .channels(singletonList("ch2"))
                .channelGroups(singletonList("group2"))
                .build();

        subscriptionManager.adaptSubscribeBuilder(subscribeOperation1);
        subscriptionManager.adaptSubscribeBuilder(subscribeOperation2);

        await().atMost(1, SECONDS).untilAsserted(new ThrowingRunnable() {
            @Override
            public void run() throws Throwable {
                verify(spiedSubscribeService, times(2)).subscribe(any(), any(), any());
            }
        });
    }

    @Test
    public void sendReconnectedOnManualReconnect() {
        long timeToken = System.currentTimeMillis();
        final ResponseSupplier<SubscribeEnvelope> responseSupplier = new ResponseSupplier<SubscribeEnvelope>() {
            private boolean first = true;

            @Override
            public ResponseHolder<SubscribeEnvelope> get(final RequestDetails requestDetails) {
                try {
                    MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                }
                if (first) {
                    first = false;
                    return new ResponseHolder<>(Response.error(500, ResponseBody.create("{}", MediaType.parse("application/json"))));
                }
                final SubscribeEnvelope subscribeEnvelope = new SubscribeEnvelope(emptyList(),
                        new SubscribeMetadata(timeToken, FAKE_REGION));
                return new ResponseHolder<>(Response.success(200, subscribeEnvelope));
            }
        };
        final RetrofitManager retrofitManagerMock = retrofitManagerMock(responseSupplier);
        final SubscriptionManager subscriptionManager = subscriptionManagerUnderTest(retrofitManagerMock);

        final SubscribeOperation subscribeOperation = SubscribeOperation.builder()
                .channels(singletonList("ch2"))
                .channelGroups(singletonList("group2"))
                .build();

        subscriptionManager.adaptSubscribeBuilder(subscribeOperation);
        subscriptionManager.reconnect();
        ArgumentCaptor<PNStatus> statusArgumentCaptor = ArgumentCaptor.forClass(PNStatus.class);
        await().atMost(1, SECONDS).untilAsserted(() -> verify(listenerManagerMock, times(2)).announce(statusArgumentCaptor.capture()));

        List<PNStatusCategory> statusCategories = statusArgumentCaptor.getAllValues().stream().map(PNStatus::getCategory).collect(Collectors.toList());
        MatcherAssert.assertThat(statusCategories, Matchers.hasItem(PNStatusCategory.PNReconnectedCategory));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void reconnectUsesTT0ForFastUserNotification() {
        final ResponseSupplier<SubscribeEnvelope> responseSupplier = requestDetails -> {
            final SubscribeEnvelope subscribeEnvelope = new SubscribeEnvelope(emptyList(),
                    new SubscribeMetadata(System.currentTimeMillis(), FAKE_REGION));
            try {
                MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
            return new ResponseHolder<>(Response.success(200, subscribeEnvelope));
        };

        final RetrofitManager retrofitManagerMock = retrofitManagerMock(responseSupplier);
        final SubscribeService spiedSubscribeService = retrofitManagerMock.getSubscribeService();
        final SubscriptionManager subscriptionManager = subscriptionManagerUnderTest(retrofitManagerMock);

        final SubscribeOperation subscribeOperation = SubscribeOperation.builder()
                .channels(singletonList("ch2"))
                .channelGroups(singletonList("group2"))
                .timetoken(42L)
                .build();

        subscriptionManager.adaptSubscribeBuilder(subscribeOperation);
        await().atMost(1, SECONDS).untilAsserted(() -> verify(spiedSubscribeService, atLeast(2)).subscribe(any(), any(), any()));
        subscriptionManager.reconnect();
        ArgumentCaptor<Map<String, String>> queryParamsCaptor = ArgumentCaptor.forClass(Map.class);
        await().atMost(1, SECONDS).untilAsserted(() -> verify(spiedSubscribeService, atLeast(1)).subscribe(any(), any(), queryParamsCaptor.capture()));

        List<String> statusCategories = queryParamsCaptor.getAllValues().stream().map((map) -> map.get("tt")).collect(Collectors.toList());
        MatcherAssert.assertThat(statusCategories, Matchers.hasItem("0"));
    }

    @NotNull
    private SubscriptionManager subscriptionManagerUnderTest(final RetrofitManager retrofitManagerMock) {
        final PubNub pubnub = spy(pubnub(retrofitManagerMock));
        final TelemetryManager telemetryManager = spy(telemetryManager(pubnub));
        final StateManager stateManager = spy(new StateManager(pubnub.getConfiguration()));
        final DuplicationManager duplicationManager = spy(new DuplicationManager(pubnub.getConfiguration()));

        return new SubscriptionManager(pubnub,
                retrofitManagerMock,
                telemetryManager,
                stateManager,
                listenerManagerMock,
                reconnectionManagerMock,
                delayedReconnectionManagerMock,
                duplicationManager,
                new TokenManager());
    }
}
