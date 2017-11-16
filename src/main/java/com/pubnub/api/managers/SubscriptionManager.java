package com.pubnub.api.managers;

import com.pubnub.api.PubNub;
import com.pubnub.api.builder.dto.StateOperation;
import com.pubnub.api.builder.dto.SubscribeOperation;
import com.pubnub.api.builder.dto.UnsubscribeOperation;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.ReconnectionCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.endpoints.presence.Heartbeat;
import com.pubnub.api.endpoints.presence.Leave;
import com.pubnub.api.endpoints.pubsub.Subscribe;
import com.pubnub.api.enums.PNHeartbeatNotificationOptions;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.server.SubscribeEnvelope;
import com.pubnub.api.models.server.SubscribeMessage;
import com.pubnub.api.workers.SubscribeMessageWorker;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class SubscriptionManager {

    private static final int HEARTBEAT_INTERVAL_MULTIPLIER = 1000;

    private PubNub pubnub;
    private TelemetryManager telemetryManager;
    private Subscribe subscribeCall;
    private Heartbeat heartbeatCall;

    private LinkedBlockingQueue<SubscribeMessage> messageQueue;

    private DuplicationManager duplicationManager;

    /**
     * Store the latest timetoken to subscribe with, null by default to get the latest timetoken.
     */
    private Long timetoken;
    private Long storedTimetoken; // when changing the channel mix, store the timetoken for a later date.

    /**
     * Keep track of Region to support PSV2 specification.
     */
    private String region;

    /**
     * Timer for heartbeat operations.
     */
    private Timer timer;

    private StateManager subscriptionState;
    private ListenerManager listenerManager;
    private ReconnectionManager reconnectionManager;
    private RetrofitManager retrofitManager;

    private Thread consumerThread;

    /**
     * lever to indicate if an announcement to the user about the subscription should be made.
     * the announcement happens only after the channel mix has been changed.
     */
    private boolean subscriptionStatusAnnounced;

    public SubscriptionManager(PubNub pubnubInstance, RetrofitManager retrofitManagerInstance, TelemetryManager telemetry) {
        this.pubnub = pubnubInstance;
        this.telemetryManager = telemetry;

        this.subscriptionStatusAnnounced = false;
        this.messageQueue = new LinkedBlockingQueue<>();
        this.subscriptionState = new StateManager();

        this.listenerManager = new ListenerManager(this.pubnub);
        this.reconnectionManager = new ReconnectionManager(this.pubnub);
        this.retrofitManager = retrofitManagerInstance;
        this.duplicationManager = new DuplicationManager(this.pubnub.getConfiguration());

        this.timetoken = 0L;
        this.storedTimetoken = null;

        this.reconnectionManager.setReconnectionListener(new ReconnectionCallback() {
            @Override
            public void onReconnection() {
                reconnect();
                PNStatus pnStatus = PNStatus.builder()
                        .error(false)
                        .affectedChannels(subscriptionState.prepareChannelList(true))
                        .affectedChannelGroups(subscriptionState.prepareChannelGroupList(true))
                        .category(PNStatusCategory.PNReconnectedCategory)
                        .build();

                subscriptionStatusAnnounced = true;
                listenerManager.announce(pnStatus);
            }

            @Override
            public void onMaxReconnectionExhaustion() {
                PNStatus pnStatus = PNStatus.builder()
                        .error(false)
                        .category(PNStatusCategory.PNReconnectionAttemptsExhausted)
                        .affectedChannels(subscriptionState.prepareChannelList(true))
                        .affectedChannelGroups(subscriptionState.prepareChannelGroupList(true))
                        .build();
                listenerManager.announce(pnStatus);

                disconnect();

            }
        });

        if (this.pubnub.getConfiguration().isStartSubscriberThread()) {
            consumerThread = new Thread(new SubscribeMessageWorker(this.pubnub, listenerManager, messageQueue, duplicationManager));
            consumerThread.setName("Subscription Manager Consumer Thread");
            consumerThread.start();
        }
    }

    public void addListener(SubscribeCallback listener) {
        listenerManager.addListener(listener);
    }

    public void removeListener(SubscribeCallback listener) {
        listenerManager.removeListener(listener);
    }


    public synchronized void reconnect() {
        this.startSubscribeLoop();
        this.registerHeartbeatTimer();
    }

    public synchronized void disconnect() {
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
        if (forceDestroy) {
            if (consumerThread != null) {
                consumerThread.interrupt();
            }
        }
    }

    public synchronized void adaptStateBuilder(StateOperation stateOperation) {
        this.subscriptionState.adaptStateBuilder(stateOperation);
        reconnect();
    }

    public synchronized void adaptSubscribeBuilder(SubscribeOperation subscribeOperation) {
        this.subscriptionState.adaptSubscribeBuilder(subscribeOperation);
        // the channel mix changed, on the successful subscribe, there is going to be announcement.
        this.subscriptionStatusAnnounced = false;

        this.duplicationManager.clearHistory();

        if (subscribeOperation.getTimetoken() != null) {
            this.timetoken = subscribeOperation.getTimetoken();
        }

        // if the timetoken is not at starting position, reset the timetoken to get a connected event
        // and store the old timetoken to be reused later during subscribe.
        if (timetoken != 0L) {
            storedTimetoken = timetoken;
        }
        timetoken = 0L;

        reconnect();
    }

    public synchronized void adaptUnsubscribeBuilder(UnsubscribeOperation unsubscribeOperation) {
        this.subscriptionState.adaptUnsubscribeBuilder(unsubscribeOperation);

        this.subscriptionStatusAnnounced = false;

        if (!this.pubnub.getConfiguration().isSupressLeaveEvents()) {
            new Leave(pubnub, this.telemetryManager, this.retrofitManager)
                    .channels(unsubscribeOperation.getChannels()).channelGroups(unsubscribeOperation.getChannelGroups())
                    .async(new PNCallback<Boolean>() {
                        @Override
                        public void onResponse(Boolean result, PNStatus status) {
                            listenerManager.announce(status);
                        }
                    });
        }


        // if we unsubscribed from all the channels, reset the timetoken back to zero and remove the region.
        if (this.subscriptionState.isEmpty()) {
            region = null;
            storedTimetoken = null;
            timetoken = 0L;
        } else {
            storedTimetoken = timetoken;
            timetoken = 0L;
        }

        reconnect();
    }

    private void registerHeartbeatTimer() {
        // make sure only one timer is running at a time.
        stopHeartbeatTimer();

        // if the interval is 0, do not start the timer
        if (pubnub.getConfiguration().getHeartbeatInterval() == 0) {
            return;
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                performHeartbeatLoop();
            }
        }, 0, pubnub.getConfiguration().getHeartbeatInterval() * HEARTBEAT_INTERVAL_MULTIPLIER);

    }

    private void stopHeartbeatTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private void startSubscribeLoop() {
        // this function can be called from different points, make sure any old loop is closed
        stopSubscribeLoop();

        List<String> combinedChannels = this.subscriptionState.prepareChannelList(true);
        List<String> combinedChannelGroups = this.subscriptionState.prepareChannelGroupList(true);

        // do not start the subscribe loop if we have no channels to subscribe to.
        if (combinedChannels.isEmpty() && combinedChannelGroups.isEmpty()) {
            return;
        }

        subscribeCall = new Subscribe(pubnub, this.retrofitManager)
                .channels(combinedChannels).channelGroups(combinedChannelGroups)
                .timetoken(timetoken).region(region)
                .filterExpression(pubnub.getConfiguration().getFilterExpression());

        subscribeCall.async(new PNCallback<SubscribeEnvelope>() {
            @Override
            public void onResponse(SubscribeEnvelope result, PNStatus status) {
                if (status.isError()) {

                    if (status.getCategory() == PNStatusCategory.PNTimeoutCategory) {
                        startSubscribeLoop();
                    } else {
                        disconnect();
                        listenerManager.announce(status);

                        // stop all announcements and ask the reconnection manager to start polling for connection restoration..
                        reconnectionManager.startPolling();
                    }

                    return;
                }

                if (!subscriptionStatusAnnounced) {
                    PNStatus pnStatus = createPublicStatus(status)
                            .category(PNStatusCategory.PNConnectedCategory)
                            .error(false)
                            .build();
                    subscriptionStatusAnnounced = true;
                    listenerManager.announce(pnStatus);
                }

                Integer requestMessageCountThreshold = pubnub.getConfiguration().getRequestMessageCountThreshold();
                if (requestMessageCountThreshold != null && requestMessageCountThreshold <= result.getMessages().size()) {
                    PNStatus pnStatus = createPublicStatus(status)
                            .category(PNStatusCategory.PNRequestMessageCountExceededCategory)
                            .error(false)
                            .build();

                    listenerManager.announce(pnStatus);
                }

                if (result.getMessages().size() != 0) {
                    messageQueue.addAll(result.getMessages());
                }

                if (storedTimetoken != null) {
                    timetoken = storedTimetoken;
                    storedTimetoken = null;
                } else {
                    timetoken = result.getMetadata().getTimetoken();
                }

                region = result.getMetadata().getRegion();
                startSubscribeLoop();
            }
        });

    }

    private void stopSubscribeLoop() {
        if (subscribeCall != null) {
            subscribeCall.silentCancel();
            subscribeCall = null;
        }
    }

    private void performHeartbeatLoop() {
        if (heartbeatCall != null) {
            heartbeatCall.silentCancel();
            heartbeatCall = null;
        }

        List<String> presenceChannels = this.subscriptionState.prepareChannelList(false);
        List<String> presenceChannelGroups = this.subscriptionState.prepareChannelGroupList(false);
        Map<String, Object> stateStorage = this.subscriptionState.createStatePayload();

        // do not start the loop if we do not have any presence channels or channel groups enabled.
        if (presenceChannels.isEmpty() && presenceChannelGroups.isEmpty()) {
            return;
        }

        heartbeatCall = new Heartbeat(pubnub, this.telemetryManager, this.retrofitManager)
                .channels(presenceChannels).channelGroups(presenceChannelGroups).state(stateStorage);

        heartbeatCall.async(new PNCallback<Boolean>() {
            @Override
            public void onResponse(Boolean result, PNStatus status) {
                PNHeartbeatNotificationOptions heartbeatVerbosity = pubnub
                        .getConfiguration().getHeartbeatNotificationOptions();

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

    public synchronized List<String> getSubscribedChannels() {
        return subscriptionState.prepareChannelList(false);
    }

    public synchronized List<String> getSubscribedChannelGroups() {
        return subscriptionState.prepareChannelGroupList(false);
    }

    public synchronized void unsubscribeAll() {
        adaptUnsubscribeBuilder(UnsubscribeOperation.builder()
                .channelGroups(subscriptionState.prepareChannelGroupList(false))
                .channels(subscriptionState.prepareChannelList(false))
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
