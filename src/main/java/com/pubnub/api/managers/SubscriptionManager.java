package com.pubnub.api.managers;

import com.pubnub.api.PubNub;
import com.pubnub.api.builder.dto.*;
import com.pubnub.api.builder.dto.ChangeTemporaryUnavailableOperation.ChangeTemporaryUnavailableOperationBuilder;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.ReconnectionCallback;
import com.pubnub.api.endpoints.presence.Heartbeat;
import com.pubnub.api.endpoints.presence.Leave;
import com.pubnub.api.endpoints.pubsub.Subscribe;
import com.pubnub.api.enums.PNHeartbeatNotificationOptions;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.server.SubscribeMessage;
import com.pubnub.api.workers.SubscribeMessageProcessor;
import com.pubnub.api.workers.SubscribeMessageWorker;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

import static com.pubnub.api.managers.StateManager.ChannelFilter.WITHOUT_TEMPORARY_UNAVAILABLE;
import static com.pubnub.api.managers.StateManager.MILLIS_IN_SECOND;

@Slf4j
public class SubscriptionManager {
    private static final int TWO_SECONDS = 2 * MILLIS_IN_SECOND;

    private static final int HEARTBEAT_INTERVAL_MULTIPLIER = 1000;

    private volatile boolean connected;

    PubNub pubnub;
    private final TelemetryManager telemetryManager;
    private final TokenManager tokenManager;
    private Subscribe subscribeCall;
    private Heartbeat heartbeatCall;

    private final LinkedBlockingQueue<SubscribeMessage> messageQueue;

    private final DuplicationManager duplicationManager;

    /**
     * Timer for heartbeat operations.
     */
    private Timer timer;

    final StateManager subscriptionState;

    private final ListenerManager listenerManager;
    private final ReconnectionManager reconnectionManager;
    private final DelayedReconnectionManager delayedReconnectionManager;
    private final RetrofitManager retrofitManager;

    private Timer temporaryUnavailableChannelsDelayer;

    private Thread consumerThread;

    public SubscriptionManager(final PubNub pubnubInstance,
                               final RetrofitManager retrofitManagerInstance,
                               final TelemetryManager telemetry,
                               final StateManager stateManager,
                               final ListenerManager listenerManager,
                               final ReconnectionManager reconnectionManager,
                               final DelayedReconnectionManager delayedReconnectionManager,
                               final DuplicationManager duplicationManager,
                               final TokenManager tokenManager) {
        this.pubnub = pubnubInstance;
        this.telemetryManager = telemetry;

        this.messageQueue = new LinkedBlockingQueue<>();
        this.subscriptionState = stateManager;

        this.listenerManager = listenerManager;
        this.reconnectionManager = reconnectionManager;
        this.delayedReconnectionManager = delayedReconnectionManager;
        this.retrofitManager = retrofitManagerInstance;
        this.duplicationManager = duplicationManager;
        this.tokenManager = tokenManager;


        final ReconnectionCallback reconnectionCallback = new ReconnectionCallback() {
            @Override
            public void onReconnection() {
                reconnect(PubSubOperation.RECONNECT);
                StateManager.SubscriptionStateData subscriptionStateData = subscriptionState.subscriptionStateData(true);
                PNStatus pnStatus = PNStatus.builder()
                        .error(false)
                        .affectedChannels(subscriptionStateData.getChannels())
                        .affectedChannelGroups(subscriptionStateData.getChannelGroups())
                        .category(PNStatusCategory.PNReconnectedCategory)
                        .build();

                listenerManager.announce(pnStatus);
            }

            @Override
            public void onMaxReconnectionExhaustion() {
                StateManager.SubscriptionStateData subscriptionStateData = subscriptionState.subscriptionStateData(true);
                PNStatus pnStatus = PNStatus.builder()
                        .error(false)
                        .category(PNStatusCategory.PNReconnectionAttemptsExhaustedCategory)
                        .affectedChannels(subscriptionStateData.getChannels())
                        .affectedChannelGroups(subscriptionStateData.getChannelGroups())
                        .build();
                listenerManager.announce(pnStatus);

                disconnect();

            }
        };

        this.delayedReconnectionManager.setReconnectionListener(reconnectionCallback);
        this.reconnectionManager.setReconnectionListener(reconnectionCallback);

        if (this.pubnub.getConfiguration().isStartSubscriberThread()) {
            consumerThread = new Thread(new SubscribeMessageWorker(
                    listenerManager, messageQueue, new SubscribeMessageProcessor(this.pubnub, duplicationManager)));
            consumerThread.setName("Subscription Manager Consumer Thread");
            consumerThread.setDaemon(true);
            consumerThread.start();
        }
    }

    public void reconnect() {
        reconnect(PubSubOperation.RECONNECT);
    }

    private synchronized void reconnect(PubSubOperation pubSubOperation) {
        connected = true;
        this.startSubscribeLoop(pubSubOperation);
        this.registerHeartbeatTimer(PubSubOperation.NO_OP);
    }

    public synchronized void disconnect() {
        connected = false;
        cancelDelayedLoopIterationForTemporaryUnavailableChannels();
        subscriptionState.handleOperation(PubSubOperation.DISCONNECT);
        delayedReconnectionManager.stop();
        stopHeartbeatTimer();
        stopSubscribeLoop();
    }


    @Deprecated
    public synchronized void stop() {
        this.disconnect();
        consumerThread.interrupt();
    }

    public synchronized void destroy(boolean forceDestroy) {
        this.disconnect();
        if (forceDestroy && consumerThread != null) {
            consumerThread.interrupt();
        }
    }

    public void adaptStateBuilder(StateOperation stateOperation) {
        reconnect(stateOperation);
    }

    public void adaptSubscribeBuilder(SubscribeOperation subscribeOperation) {
        reconnect(subscribeOperation);
    }

    public void adaptPresenceBuilder(PresenceOperation presenceOperation) {
        if (!this.pubnub.getConfiguration().isSuppressLeaveEvents() && !presenceOperation.isConnected()) {
            new Leave(pubnub, this.telemetryManager, this.retrofitManager, tokenManager)
                    .channels(presenceOperation.getChannels()).channelGroups(presenceOperation.getChannelGroups())
                    .async(new PNCallback<Boolean>() {
                        @Override
                        public void onResponse(Boolean result, @NotNull PNStatus status) {
                            listenerManager.announce(status);
                        }
                    });
        }

        registerHeartbeatTimer(presenceOperation);
    }

    public void adaptUnsubscribeBuilder(UnsubscribeOperation unsubscribeOperation) {
        reconnect(unsubscribeOperation);

        if (!this.pubnub.getConfiguration().isSuppressLeaveEvents()) {
            new Leave(pubnub, this.telemetryManager, this.retrofitManager, tokenManager)
                    .channels(unsubscribeOperation.getChannels())
                    .channelGroups(unsubscribeOperation.getChannelGroups())
                    .async(new PNCallback<Boolean>() {
                        @Override
                        public void onResponse(Boolean result, @NotNull PNStatus status) {
                            //In case we get PNAccessDeniedCategory while sending Leave event we do not announce it.
                            //Client did initiate it explicitly,
                            if (status.isError() && status.getCategory() == PNStatusCategory.PNAccessDeniedCategory) {
                                return;
                            }
                            listenerManager.announce(status);
                        }
                    });
        }
    }

    private synchronized void registerHeartbeatTimer(PubSubOperation pubSubOperation) {
        // make sure only one timer is running at a time.
        stopHeartbeatTimer();

        // if the interval is 0 or less, do not start the timer
        if (pubnub.getConfiguration().getHeartbeatInterval() <= 0) {
            return;
        }

        timer = new Timer("Subscription Manager Heartbeat Timer", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                performHeartbeatLoop(pubSubOperation);
            }
        }, 0, pubnub.getConfiguration().getHeartbeatInterval() * HEARTBEAT_INTERVAL_MULTIPLIER);

    }

    private void stopHeartbeatTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (heartbeatCall != null) {
            heartbeatCall.silentCancel();
            heartbeatCall = null;
        }
    }

    private synchronized void cancelDelayedLoopIterationForTemporaryUnavailableChannels() {
        if (temporaryUnavailableChannelsDelayer != null) {
            temporaryUnavailableChannelsDelayer.cancel();
            temporaryUnavailableChannelsDelayer = null;
        }
    }

    private void scheduleDelayedLoopIterationForTemporaryUnavailableChannels() {
        cancelDelayedLoopIterationForTemporaryUnavailableChannels();

        temporaryUnavailableChannelsDelayer = new Timer("Subscription Manager TMP Unavailable Channel Delayer", true);
        temporaryUnavailableChannelsDelayer.schedule(new TimerTask() {
            @Override
            public void run() {
                startSubscribeLoop(PubSubOperation.NO_OP);
            }
        }, TWO_SECONDS);
    }

    /**
     * user is calling subscribe:
     * <p>
     * if the state has changed we should restart the subscribe loop
     * if the state hasn't change but the loop is not running we should restart the loop
     * if the state hasn't change and the loop is running fine, we should do nothing
     */

    synchronized void startSubscribeLoop(final PubSubOperation... pubSubOperations) {
        if (!connected) {
            return;
        }
        boolean subscriptionLoopStateChanged = subscriptionState.handleOperation(pubSubOperations);
        if (!subscriptionLoopStateChanged) {
            return;
        }
        stopSubscribeLoop();

        for (PubSubOperation pubSubOperation : pubSubOperations) {
            if (pubSubOperation instanceof SubscribeOperation) {
                duplicationManager.clearHistory();
            }
        }

        final StateManager.SubscriptionStateData subscriptionStateData = subscriptionState.subscriptionStateData(
                true,
                WITHOUT_TEMPORARY_UNAVAILABLE);

        if (!subscriptionStateData.isAnythingToSubscribe()) {
            return;
        }

        if (subscriptionStateData.isSubscribedToOnlyTemporaryUnavailable()) {
            scheduleDelayedLoopIterationForTemporaryUnavailableChannels();
            return;
        }

        subscribeCall = new Subscribe(pubnub, this.retrofitManager, tokenManager)
                .channels(subscriptionStateData.getChannels())
                .channelGroups(subscriptionStateData.getChannelGroups())
                .timetoken(subscriptionStateData.getTimetoken())
                .region(subscriptionStateData.getRegion())
                .filterExpression(pubnub.getConfiguration().getFilterExpression())
                .state(subscriptionStateData.getStatePayload());

        subscribeCall.async((result, status) -> {
            if (status.isError()) {
                handleError(status, pubSubOperations);
            } else {
                final ChangeTemporaryUnavailableOperationBuilder availableChannels = ChangeTemporaryUnavailableOperation
                        .builder();
                if (status.getCategory() == PNStatusCategory.PNAcknowledgmentCategory) {
                    final List<String> affectedChannels = status.getAffectedChannels();
                    final List<String> affectedChannelGroups = status.getAffectedChannelGroups();

                    if (affectedChannels != null) {
                        for (final String affectedChannel : affectedChannels) {
                            availableChannels.availableChannel(affectedChannel);
                        }
                    }
                    if (affectedChannelGroups != null) {
                        for (final String affectedChannelGroup : affectedChannelGroups) {
                            availableChannels.availableChannelGroup(affectedChannelGroup);
                        }
                    }
                }

                final PubSubOperation statusAnnouncedOperation;
                if (subscriptionStateData.isShouldAnnounce()) {
                    PNStatus pnStatus = createPublicStatus(status)
                            .category(subscriptionStateData.getAnnounceStatus())
                            .error(false)
                            .build();
                    listenerManager.announce(pnStatus);
                    statusAnnouncedOperation = PubSubOperation.STATUS_ANNOUNCED;
                } else {
                    statusAnnouncedOperation = PubSubOperation.NO_OP;
                }

                Integer requestMessageCountThreshold = pubnub.getConfiguration().getRequestMessageCountThreshold();
                if (requestMessageCountThreshold != null && requestMessageCountThreshold <= result.getMessages()
                        .size()) {
                    PNStatus pnStatus = createPublicStatus(status)
                            .category(PNStatusCategory.PNRequestMessageCountExceededCategory)
                            .error(false)
                            .build();

                    listenerManager.announce(pnStatus);
                }

                if (result.getMessages().size() != 0) {
                    messageQueue.addAll(result.getMessages());
                }

                final TimetokenAndRegionOperation timetokenAndRegionOperation = new TimetokenAndRegionOperation(
                        result.getMetadata()
                                .getTimetoken(),
                        result.getMetadata().getRegion());
                startSubscribeLoop(timetokenAndRegionOperation, availableChannels.build(), statusAnnouncedOperation);
            }
        });

    }

    private void handleError(@NotNull PNStatus status,
                             PubSubOperation... pubSubOperations) {
        final PNStatusCategory category = status.getCategory();

        switch (category) {
            case PNTimeoutCategory:
                startSubscribeLoop(pubSubOperations);
                break;
            case PNUnexpectedDisconnectCategory:
                // stop all announcements and ask the reconnection manager to start polling for connection
                // restoration..
                disconnect();
                listenerManager.announce(status);
                reconnectionManager.startPolling();
                break;
            case PNBadRequestCategory:
            case PNURITooLongCategory:
                disconnect();
                listenerManager.announce(status);
                break;
            case PNAccessDeniedCategory:
                listenerManager.announce(status);
                final List<String> affectedChannels = status.getAffectedChannels();
                final List<String> affectedChannelGroups = status.getAffectedChannelGroups();
                final ChangeTemporaryUnavailableOperationBuilder unavailableChannels = ChangeTemporaryUnavailableOperation
                        .builder();
                if (affectedChannels != null || affectedChannelGroups != null) {
                    if (affectedChannels != null) {
                        for (final String channelToMoveToTemporaryUnavailable : affectedChannels) {
                            unavailableChannels.unavailableChannel(channelToMoveToTemporaryUnavailable);
                        }
                    }
                    if (affectedChannelGroups != null) {
                        for (final String channelGroupToMoveToTemporaryUnavailable : affectedChannelGroups) {
                            unavailableChannels.unavailableChannelGroup(
                                    channelGroupToMoveToTemporaryUnavailable);
                        }
                    }
                    startSubscribeLoop(unavailableChannels.build());
                }

                break;
            default:
                listenerManager.announce(status);
                delayedReconnectionManager.scheduleDelayedReconnection();
                break;
        }
    }

    private void stopSubscribeLoop() {
        cancelDelayedLoopIterationForTemporaryUnavailableChannels();
        if (subscribeCall != null) {
            subscribeCall.silentCancel();
            subscribeCall = null;
        }
    }

    private synchronized void performHeartbeatLoop(PubSubOperation pubSubOperation) {
        if (heartbeatCall != null) {
            heartbeatCall.silentCancel();
            heartbeatCall = null;
        }

        subscriptionState.handleOperation(pubSubOperation);
        StateManager.HeartbeatStateData heartbeatStateData = subscriptionState.heartbeatStateData();

        final List<String> heartbeatChannels = heartbeatStateData.getHeartbeatChannels();
        final List<String> heartbeatChannelGroups = heartbeatStateData.getHeartbeatChannelGroups();


        // do not start the loop if we do not have any presence channels or channel groups enabled.
        if (heartbeatChannels.isEmpty()
                && heartbeatChannelGroups.isEmpty()) {
            return;
        }

        final Map<String, Object> statePayload;
        if (heartbeatStateData.getStatePayload().isEmpty()) {
            statePayload = null;
        } else {
            statePayload = heartbeatStateData.getStatePayload();
        }

        heartbeatCall = new Heartbeat(pubnub, this.telemetryManager, this.retrofitManager, this.tokenManager)
                .channels(heartbeatChannels)
                .channelGroups(heartbeatChannelGroups)
                .state(statePayload);

        heartbeatCall.async(new PNCallback<Boolean>() {
            @Override
            public void onResponse(Boolean result, @NotNull PNStatus status) {
                PNHeartbeatNotificationOptions heartbeatVerbosity =
                        pubnub.getConfiguration().getHeartbeatNotificationOptions();

                if (status.isError()) {
                    if (heartbeatVerbosity == PNHeartbeatNotificationOptions.ALL
                            || heartbeatVerbosity == PNHeartbeatNotificationOptions.FAILURES) {
                        listenerManager.announce(status);
                    }

                    // stop the heartbeating logic since an error happened.
                    stopHeartbeatTimer();

                } else {
                    if (heartbeatVerbosity == PNHeartbeatNotificationOptions.ALL) {
                        listenerManager.announce(status);
                    }
                }
            }
        });
    }

    public void unsubscribeAll() {
        StateManager.SubscriptionStateData subscriptionStateData = subscriptionState.subscriptionStateData(false);

        adaptUnsubscribeBuilder(UnsubscribeOperation.builder()
                .channelGroups(subscriptionStateData.getChannelGroups())
                .channels(subscriptionStateData.getChannels())
                .build());
    }

    private PNStatus.PNStatusBuilder createPublicStatus(PNStatus privateStatus) {
        return PNStatus.builder()
                .statusCode(privateStatus.getStatusCode())
                .authKey(privateStatus.getAuthKey())
                .operation(privateStatus.getOperation())
                .affectedChannels(privateStatus.getAffectedChannels())
                .affectedChannelGroups(privateStatus.getAffectedChannelGroups())
                .clientRequest(privateStatus.getClientRequest())
                .origin(privateStatus.getOrigin())
                .tlsEnabled(privateStatus.isTlsEnabled());
    }
}
