package com.pubnub.api.managers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.Crypto;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.dto.SubscribeOperation;
import com.pubnub.api.builder.dto.UnsubscribeOperation;
import com.pubnub.api.enums.PNHeartbeatNotificationOptions;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.enums.SubscriptionType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.server.Envelope;
import com.pubnub.api.models.SubscriptionItem;
import com.pubnub.api.models.server.SubscribeEnvelope;
import com.pubnub.api.models.server.SubscribeMessage;
import com.pubnub.api.endpoints.presence.Heartbeat;
import com.pubnub.api.endpoints.presence.Leave;
import com.pubnub.api.endpoints.pubsub.Subscribe;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;

import java.io.IOException;
import java.util.*;

@Slf4j
public class SubscriptionManager {

    private Map<String, SubscriptionItem> subscribedChannels;
    private Map<String, SubscriptionItem> subscribedChannelGroups;
    private Map<String, Object> stateStorage;
    private List<SubscribeCallback> listeners;
    private PubNub pubnub;
    private Call<SubscribeEnvelope> subscribeCall;
    private Call<Envelope> heartbeatCall;
    /**
     * Store the latest timetoken to subscribe with, null by default to get the latest timetoken.
     */
    private Long timetoken;

    /**
     * Keep track of Region to support PSV2 specification.
     */
    private String region;

    Timer timer;

    public SubscriptionManager(PubNub pubnub) {
        this.subscribedChannelGroups = new HashMap<>();
        this.subscribedChannels = new HashMap<>();
        this.pubnub = pubnub;
        this.listeners = new ArrayList<>();
        this.stateStorage = new HashMap<>();
    }


    public final synchronized void addListener(SubscribeCallback listener) {
        listeners.add(listener);
    }

    public final synchronized void removeListener(SubscribeCallback listener) {
        listeners.remove(listener);
    }

    public final synchronized void reconnect() {
        this.startSubscribeLoop();
        this.registerHeartbeatTimer();
    }

    public synchronized void stop() {
        stopHeartbeatTimer();
        stopSubscribeLoop();
    }

    public final synchronized void adaptStateBuilder(List<String> channels, List<String> channelGroups, Object state) {
        for (String channel: channels) {
            stateStorage.put(channel, state);
        }

        for (String channelGroup: channelGroups) {
            stateStorage.put(channelGroup, state);
        }

    }

    public final synchronized void adaptSubscribeBuilder(final SubscribeOperation subscribeOperation) {
        for (String channel : subscribeOperation.getChannels()) {
            SubscriptionItem subscriptionItem = SubscriptionItem.builder()
                .name(channel)
                .withPresence(subscribeOperation.isPresenceEnabled())
                .type(SubscriptionType.CHANNEL)
                .build();
            subscribedChannels.put(channel, subscriptionItem);
        }

        for (String channelGroup : subscribeOperation.getChannelGroups()) {
            SubscriptionItem subscriptionItem = SubscriptionItem.builder()
                    .name(channelGroup)
                    .withPresence(subscribeOperation.isPresenceEnabled())
                    .type(SubscriptionType.CHANNEL_GROUP)
                    .build();
            subscribedChannelGroups.put(channelGroup, subscriptionItem);
        }

        if (subscribeOperation.getTimetoken() != null) {
            this.timetoken = subscribeOperation.getTimetoken();
        }

        reconnect();
    }

    public final synchronized void adaptUnsubscribeBuilder(final UnsubscribeOperation unsubscribeOperation) {

        for (String channel: unsubscribeOperation.getChannels()) {
            this.subscribedChannels.remove(channel);
            this.stateStorage.remove(channel);
        }

        for (String channelGroup: unsubscribeOperation.getChannelGroups()) {
            this.subscribedChannelGroups.remove(channelGroup);
            this.stateStorage.remove(channelGroup);
        }

        new Leave(pubnub)
            .channels(unsubscribeOperation.getChannels()).channelGroups(unsubscribeOperation.getChannelGroups())
            .async(new PNCallback<Boolean>() {
                @Override
                public void onResponse(Boolean result, PNStatus status) {
                    announce(status);
                }
        });

        reconnect();
    }

    private void registerHeartbeatTimer() {
        // make sure only one timer is running at a time.
        stopHeartbeatTimer();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                performHeartbeatLoop();
            }
        }, 0, pubnub.getConfiguration().getHeartbeatInterval() * 1000);

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

        List<String> combinedChannels = new ArrayList<>();
        List<String> combinedChannelGroups = new ArrayList<>();

        for (SubscriptionItem subscriptionItem: subscribedChannels.values()) {
            combinedChannels.add(subscriptionItem.getName());

            if  (subscriptionItem.isWithPresence()) {
                combinedChannels.add(subscriptionItem.getName().concat("-pnpres"));
            }
        }

        for (SubscriptionItem subscriptionItem: subscribedChannelGroups.values()) {
            combinedChannelGroups.add(subscriptionItem.getName());

            if  (subscriptionItem.isWithPresence()) {
                combinedChannelGroups.add(subscriptionItem.getName().concat("-pnpres"));
            }
        }

        // do not start the subscribe loop if we have no channels to subscribe to.
        if (combinedChannels.isEmpty() && combinedChannelGroups.isEmpty()) {
            return;
        }

        subscribeCall =  new Subscribe(pubnub)
                .channels(combinedChannels).channelGroups(combinedChannelGroups)
                .timetoken(timetoken).region(region)
                .filterExpression(pubnub.getConfiguration().getFilterExpression())
                .async(new PNCallback<SubscribeEnvelope>() {
                    @Override
                    public void onResponse(SubscribeEnvelope result, PNStatus status) {
                        if (status.isError()) {

                            if (status.getCategory() == PNStatusCategory.PNTimeoutCategory) {
                                startSubscribeLoop();
                            } else {
                                announce(status);
                            }

                            return;
                        }

                        if (result.getMessages().size() != 0) {
                            processIncomingMessages(result.getMessages());
                        }

                        timetoken = result.getMetadata().getTimetoken();
                        region = result.getMetadata().getRegion();
                        startSubscribeLoop();
                    }
                });
    }

    private void stopSubscribeLoop() {
        if (subscribeCall != null && !subscribeCall.isExecuted() && !subscribeCall.isCanceled()) {
            subscribeCall.cancel();
        }
    }

    private void performHeartbeatLoop() {
        if (heartbeatCall != null && !heartbeatCall.isCanceled() && !heartbeatCall.isExecuted()) {
            heartbeatCall.cancel();
        }

        List<String> presenceChannels = new ArrayList<>();
        List<String> presenceChannelGroups = new ArrayList<>();

        for (SubscriptionItem subscriptionItem: subscribedChannels.values()) {
            if  (subscriptionItem.isWithPresence()) {
                presenceChannels.add(subscriptionItem.getName());
            }
        }

        for (SubscriptionItem subscriptionItem: subscribedChannelGroups.values()) {
            if  (subscriptionItem.isWithPresence()) {
                presenceChannelGroups.add(subscriptionItem.getName());
            }
        }

        // do not start the loop if we do not have any presence channels or channel groups enabled.
        if (presenceChannels.isEmpty() && presenceChannelGroups.isEmpty()) {
            return;
        }

        heartbeatCall = new Heartbeat(pubnub)
                .channels(presenceChannels).channelGroups(presenceChannelGroups).state(stateStorage)
                .async(new PNCallback<Boolean>() {
                    @Override
                    public void onResponse(Boolean result, PNStatus status) {
                        PNHeartbeatNotificationOptions heartbeatVerbosity = pubnub
                                .getConfiguration().getHeartbeatNotificationOptions();

                        if (status.isError()) {
                            if (heartbeatVerbosity == PNHeartbeatNotificationOptions.All ||
                                    heartbeatVerbosity == PNHeartbeatNotificationOptions.Failures) {
                                announce(status);
                            }

                        } else {
                            if (heartbeatVerbosity == PNHeartbeatNotificationOptions.All) {
                                announce(status);
                            }
                        }
                    }
                });
    }

    private void processIncomingMessages(List<SubscribeMessage> messages) {

        for (SubscribeMessage message : messages) {

            String channel = message.getChannel();
            String subscriptionMatch = message.getSubscriptionMatch();

            if (channel.equals(subscriptionMatch)) {
                subscriptionMatch = null;
            }

            if (message.getChannel().contains("-pnpres")) {
                Map<String, Object> presencePayload = (Map<String, Object>) message.getPayload();

                PNPresenceEventResult pnPresenceEventResult = PNPresenceEventResult.builder()
                        .event(presencePayload.get("action").toString())
                        .actualChannel((subscriptionMatch != null) ? channel : null)
                        .subscribedChannel(subscriptionMatch != null ? subscriptionMatch : channel)
                        .timetoken(timetoken)
                        .occupancy((int) presencePayload.get("occupancy"))
                        .uuid(presencePayload.get("uuid").toString())
                        .timestamp(Long.valueOf(presencePayload.get("timestamp").toString()))
                        .build();

                announce(pnPresenceEventResult);
            } else {
                Object extractedMessage;

                try {
                    extractedMessage = processMessage(message.getPayload());
                } catch (PubNubException e) {
                    PNStatus pnStatus = PNStatus.builder().error(true)
                            .operation(PNOperationType.PNSubscribeOperation)
                            .category(PNStatusCategory.PNDecryptionErrorCategory)
                            .build();

                    announce(pnStatus);
                    return;
                }

                PNMessageResult pnMessageResult = PNMessageResult.builder()
                    .message(extractedMessage)
                    .actualChannel((subscriptionMatch != null) ? channel : null)
                    .subscribedChannel(subscriptionMatch != null ? subscriptionMatch : channel)
                    .timetoken(timetoken)
                    .build();


                announce(pnMessageResult);
            }
        }
    }

    private Object processMessage(Object input) throws PubNubException {
        if (pubnub.getConfiguration().getCipherKey() == null) {
            return input;
        }

        Crypto crypto = new Crypto(pubnub.getConfiguration().getCipherKey());
        String outputText = crypto.decrypt(input.toString());

        ObjectMapper mapper = new ObjectMapper();
        Object outputObject;
        try {
            outputObject = mapper.readValue(outputText, JsonNode.class);
        } catch (IOException e) {
            throw PubNubException.builder().pubnubError(PubNubError.PNERROBJ_PARSING_ERROR).errormsg(e.getMessage()).build();
        }

        return outputObject;
    }

    /**
     * announce a PNStatus to listeners.
     * @param status PNStatus which will be broadcast to listeners.
     */
    private void announce(final PNStatus status) {
        for (SubscribeCallback subscribeCallback: listeners) {
            subscribeCallback.status(this.pubnub, status);
        }
    }

    private void announce(final PNMessageResult message) {
        for (SubscribeCallback subscribeCallback: listeners) {
            subscribeCallback.message(this.pubnub, message);
        }
    }

    private void announce(final PNPresenceEventResult presence) {
        for (SubscribeCallback subscribeCallback: listeners) {
            subscribeCallback.presence(this.pubnub, presence);
        }
    }

}
