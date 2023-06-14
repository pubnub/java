package com.pubnub.api.managers;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.builder.dto.ChangeTemporaryUnavailableOperation;
import com.pubnub.api.builder.dto.PresenceOperation;
import com.pubnub.api.builder.dto.PubSubOperation;
import com.pubnub.api.builder.dto.StateOperation;
import com.pubnub.api.builder.dto.SubscribeOperation;
import com.pubnub.api.builder.dto.TimetokenAndRegionOperation;
import com.pubnub.api.builder.dto.UnsubscribeOperation;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.SubscriptionItem;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StateManager {
    @Data
    @AllArgsConstructor
    private static class TemporaryUnavailableItem {
        private String item;
        private Date timestamp;
    }

    static final int MILLIS_IN_SECOND = 1000;

    /**
     * Contains a list of subscribed channels
     */
    private final Map<String, SubscriptionItem> channels = new HashMap<>();
    /**
     * Contains a list of subscribed presence channels.
     */
    private final Map<String, SubscriptionItem> presenceChannels = new HashMap<>();

    /**
     * Contains a list of subscribed channel groups.
     */
    private final Map<String, SubscriptionItem> groups = new HashMap<>();

    /**
     * Contains a list of subscribed presence channel groups.
     */
    private final Map<String, SubscriptionItem> presenceGroups = new HashMap<>();

    private final Map<String, SubscriptionItem> heartbeatChannels = new HashMap<>();
    private final Map<String, SubscriptionItem> heartbeatGroups = new HashMap<>();

    private final List<TemporaryUnavailableItem> temporaryUnavailableChannels = new ArrayList<>();
    private final List<TemporaryUnavailableItem> temporaryUnavailableChannelGroups = new ArrayList<>();

    /**
     * Store the latest timetoken to subscribe with, null by default to get the latest timetoken.
     */
    private Long timetoken = 0L;
    private Long storedTimetoken = null; // when changing the channel mix, store the timetoken for a later date.

    /**
     * Keep track of Region to support PSV2 specification.
     */
    private String region = null;

    private final PNConfiguration configuration;
    private PNStatusCategory announceStatus = null;

    public StateManager(final PNConfiguration configuration) {
        this.configuration = configuration;
    }

    public synchronized boolean handleOperation(final PubSubOperation... pubSubOperations) {
        boolean stateChanged = false;
        for (PubSubOperation pubSubOperation : pubSubOperations) {
            if (pubSubOperation instanceof SubscribeOperation) {
                if (adaptSubscribeBuilder((SubscribeOperation) pubSubOperation)) {
                    stateChanged = true;
                    announceStatus = PNStatusCategory.PNConnectedCategory;
                }
            } else if (pubSubOperation instanceof UnsubscribeOperation) {
                unsubscribe((UnsubscribeOperation) pubSubOperation);
                stateChanged = true;
                announceStatus = PNStatusCategory.PNConnectedCategory;
            } else if (pubSubOperation instanceof StateOperation) {
                stateChanged = true;
                adaptStateBuilder((StateOperation) pubSubOperation);
            } else if (pubSubOperation instanceof PresenceOperation) {
                adaptPresenceBuilder((PresenceOperation) pubSubOperation);
            } else if (pubSubOperation instanceof TimetokenAndRegionOperation) {
                TimetokenAndRegionOperation ttAndReg = (TimetokenAndRegionOperation) pubSubOperation;
                updateTimetokenAndRegion(ttAndReg.getTimetoken(), ttAndReg.getRegion());
                stateChanged = true;
            } else if (pubSubOperation instanceof PubSubOperation.DisconnectOperation) {
                resetTemporaryUnavailableChannelsAndGroups();
            } else if (pubSubOperation instanceof ChangeTemporaryUnavailableOperation) {
                changeTemporary((ChangeTemporaryUnavailableOperation) pubSubOperation);
            } else if (pubSubOperation instanceof PubSubOperation.ConnectedStatusAnnouncedOperation) {
                announceStatus = null;
            } else if (pubSubOperation instanceof PubSubOperation.ReconnectOperation) {
                stateChanged = true;
                announceStatus = PNStatusCategory.PNReconnectedCategory;
                storedTimetoken = timetoken;
                timetoken = 0L;
            }
        }
        return stateChanged;
    }

    public synchronized SubscriptionStateData subscriptionStateData(Boolean includePresence) {
        return subscriptionStateData(includePresence, ChannelFilter.WITH_TEMPORARY_UNAVAILABLE);
    }

    public synchronized SubscriptionStateData subscriptionStateData(Boolean includePresence,
                                                                    ChannelFilter channelFilter) {
        final List<String> channelsList;
        final List<String> groupsList;
        if (channelFilter == ChannelFilter.WITH_TEMPORARY_UNAVAILABLE) {
            channelsList = prepareMembershipList(channels, presenceChannels, includePresence);
            groupsList = prepareMembershipList(groups, presenceGroups, includePresence);
        } else {
            channelsList = effectiveChannels(includePresence);
            groupsList = effectiveChannelGroups(includePresence);
        }
        return new SubscriptionStateData(
                createStatePayload(),
                groupsList,
                channelsList,
                timetoken,
                region,
                hasAnythingToSubscribe(),
                subscribedToOnlyTemporaryUnavailable(),
                announceStatus
        );
    }

    @SuppressWarnings("deprecation")
    public synchronized HeartbeatStateData heartbeatStateData() {
        if (configuration.isManagePresenceListManually()) {
            return new HeartbeatStateData(createHeartbeatStatePayload(),
                    getNames(heartbeatGroups),
                    getNames(heartbeatChannels));
        } else {
            List<String> heartbeatGroupNames = getNames(heartbeatGroups);
            heartbeatGroupNames.addAll(getNames(groups));
            List<String> heartbeatChannelNames = getNames(heartbeatChannels);
            heartbeatChannelNames.addAll(getNames(channels));
            return new HeartbeatStateData(Collections.emptyMap(),
                    heartbeatGroupNames,
                    heartbeatChannelNames);
        }
    }

    private void updateTimetokenAndRegion(final Long newTimetoken, final String region) {
        if (storedTimetoken != null) {
            timetoken = storedTimetoken;
            storedTimetoken = null;
        } else {
            timetoken = newTimetoken;
        }

        this.region = region;
    }

    private void explicitlySetTimetoken(final Long timetokenToSet) {
        if (timetokenToSet != null) {
            this.timetoken = timetokenToSet;
        }

        // if the timetoken is not at starting position, reset the timetoken to get a connected event
        // and store the old timetoken to be reused later during subscribe.
        if (timetoken != 0L) {
            storedTimetoken = timetoken;
        }
        timetoken = 0L;
    }

    private boolean adaptSubscribeBuilder(SubscribeOperation subscribeOperation) {
        boolean changeDetected = false;

        for (String channel : subscribeOperation.getChannels()) {
            if (channel == null || channel.length() == 0) {
                continue;
            }

            final SubscriptionItem subscriptionItem = new SubscriptionItem().setName(channel);
            changeDetected = putIfDifferent(channels, channel, subscriptionItem) || changeDetected;

            if (subscribeOperation.isPresenceEnabled()) {
                final SubscriptionItem presenceSubscriptionItem = new SubscriptionItem().setName(channel);
                changeDetected = putIfDifferent(presenceChannels, channel, presenceSubscriptionItem) || changeDetected;
            }
        }

        for (String channelGroup : subscribeOperation.getChannelGroups()) {
            if (channelGroup == null || channelGroup.length() == 0) {
                continue;
            }

            final SubscriptionItem subscriptionItem = new SubscriptionItem().setName(channelGroup);
            changeDetected = putIfDifferent(groups, channelGroup, subscriptionItem) || changeDetected;

            if (subscribeOperation.isPresenceEnabled()) {
                final SubscriptionItem presenceSubscriptionItem = new SubscriptionItem().setName(channelGroup);
                changeDetected = putIfDifferent(presenceGroups,
                        channelGroup,
                        presenceSubscriptionItem) || changeDetected;
            }

        }
        if (changeDetected) {
            explicitlySetTimetoken(subscribeOperation.getTimetoken());
        }
        return changeDetected;
    }

    private <T> boolean putIfDifferent(final Map<String, T> map, final String key, final T newValue) {
        final T existingValue = map.get(key);
        if (existingValue == null) {
            map.put(key, newValue);
            return true;
        } else {
            if (existingValue.equals(newValue)) {
                return false;
            } else {
                map.put(key, newValue);
                return true;
            }
        }
    }

    private void adaptStateBuilder(StateOperation stateOperation) {
        for (String channel : stateOperation.getChannels()) {
            SubscriptionItem subscribedChannel = channels.get(channel);

            if (subscribedChannel != null) {
                subscribedChannel.setState(stateOperation.getState());
            }

            SubscriptionItem heartbeatChannel = heartbeatChannels.get(channel);

            if (heartbeatChannel != null) {
                heartbeatChannel.setState(stateOperation.getState());
            }
        }

        for (String channelGroup : stateOperation.getChannelGroups()) {
            SubscriptionItem subscribedChannelGroup = groups.get(channelGroup);

            if (subscribedChannelGroup != null) {
                subscribedChannelGroup.setState(stateOperation.getState());
            }

            SubscriptionItem heartbeatChannelGroup = heartbeatGroups.get(channelGroup);

            if (heartbeatChannelGroup != null) {
                heartbeatChannelGroup.setState(stateOperation.getState());
            }
        }
    }


    private void unsubscribe(UnsubscribeOperation unsubscribeOperation) {
        for (String channel : unsubscribeOperation.getChannels()) {
            this.channels.remove(channel);
            this.presenceChannels.remove(channel);
        }
        removeTemporaryUnavailableChannels(unsubscribeOperation.getChannels());

        for (String channelGroup : unsubscribeOperation.getChannelGroups()) {
            this.groups.remove(channelGroup);
            this.presenceGroups.remove(channelGroup);
        }
        removeTemporaryUnavailableChannelGroups(unsubscribeOperation.getChannelGroups());

        // if we unsubscribed from all the channels, reset the timetoken back to zero and remove the region.
        if (this.isEmpty()) {
            region = null;
            storedTimetoken = null;
        } else {
            storedTimetoken = timetoken;
        }
        timetoken = 0L;
    }

    private void adaptPresenceBuilder(PresenceOperation presenceOperation) {
        for (String channel : presenceOperation.getChannels()) {
            if (channel == null || channel.length() == 0) {
                continue;
            }

            if (presenceOperation.isConnected()) {
                SubscriptionItem subscriptionItem = new SubscriptionItem().setName(channel);
                heartbeatChannels.put(channel, subscriptionItem);
            } else {
                heartbeatChannels.remove(channel);
            }

        }

        for (String channelGroup : presenceOperation.getChannelGroups()) {
            if (channelGroup == null || channelGroup.length() == 0) {
                continue;
            }

            if (presenceOperation.isConnected()) {
                SubscriptionItem subscriptionItem = new SubscriptionItem().setName(channelGroup);
                heartbeatGroups.put(channelGroup, subscriptionItem);
            } else {
                heartbeatGroups.remove(channelGroup);
            }

        }
    }

    private void changeTemporary(ChangeTemporaryUnavailableOperation operation) {
        for (String channel : operation.getUnavailableChannels()) {
            temporaryUnavailableChannels.add(new TemporaryUnavailableItem(channel, new Date()));
        }
        for (String channelGroup : operation.getUnavailableChannelGroups()) {
            temporaryUnavailableChannelGroups.add(new TemporaryUnavailableItem(channelGroup, new Date()));
        }

        removeTemporaryUnavailableChannels(operation.getAvailableChannels());
        removeTemporaryUnavailableChannelGroups(operation.getAvailableChannelGroups());
    }

    private Map<String, Object> createStatePayload() {
        return createStatePayload(channels, groups);
    }

    private Map<String, Object> createHeartbeatStatePayload() {
        return createStatePayload(heartbeatChannels, heartbeatGroups);
    }

    private Map<String, Object> createStatePayload(Map<String, SubscriptionItem> channels, Map<String, SubscriptionItem> groups) {
        Map<String, Object> stateResponse = new HashMap<>();

        for (SubscriptionItem channel : channels.values()) {
            if (channel.getState() != null) {
                stateResponse.put(channel.getName(), channel.getState());
            }
        }

        for (SubscriptionItem channelGroup : groups.values()) {
            if (channelGroup.getState() != null) {
                stateResponse.put(channelGroup.getName(), channelGroup.getState());
            }
        }

        return stateResponse;
    }

    private boolean hasAnythingToSubscribe() {
        final List<String> combinedChannels = prepareMembershipList(channels, presenceChannels, true);
        final List<String> combinedChannelGroups = prepareMembershipList(groups, presenceGroups, true);

        return !combinedChannels.isEmpty() || !combinedChannelGroups.isEmpty();
    }

    private void resetTemporaryUnavailableChannelsAndGroups() {
        temporaryUnavailableChannels.clear();
        temporaryUnavailableChannelGroups.clear();
    }

    private void removeTemporaryUnavailableChannels(Collection<String> channels) {
        removeTemporaryUnavailable(channels, temporaryUnavailableChannels);
    }

    private void removeTemporaryUnavailableChannelGroups(Collection<String> channelGroups) {
        removeTemporaryUnavailable(channelGroups, temporaryUnavailableChannelGroups);
    }

    private void removeTemporaryUnavailable(final Collection<String> toBeRemoved,
                                            final Collection<TemporaryUnavailableItem> temporaryUnavailableItems) {
        if (toBeRemoved.isEmpty()) {
            return;
        }
        final List<TemporaryUnavailableItem> temporaryUnavailableItemsToBeRemoved = new ArrayList<>();
        for (final TemporaryUnavailableItem temporaryUnavailableItem : temporaryUnavailableItems) {
            if (toBeRemoved.contains(temporaryUnavailableItem.getItem())) {
                temporaryUnavailableItemsToBeRemoved.add(temporaryUnavailableItem);
            }
        }
        temporaryUnavailableItems.removeAll(temporaryUnavailableItemsToBeRemoved);
    }

    private boolean subscribedToOnlyTemporaryUnavailable() {
        return effectiveChannels().isEmpty() && effectiveChannelGroups().isEmpty();
    }

    private List<String> effectiveChannels() {
        return effectiveChannels(true);
    }

    private List<String> effectiveChannels(boolean includePresence) {
        final List<String> effectiveChannelsList = prepareMembershipList(channels, presenceChannels, includePresence);
        effectiveChannelsList.removeAll(channelsToPostponeSubscription(temporaryUnavailableChannels));
        return effectiveChannelsList;
    }

    private List<String> effectiveChannelGroups() {
        return effectiveChannelGroups(true);
    }

    private List<String> effectiveChannelGroups(boolean includePresence) {
        final List<String> effectiveChannelGroupsList = prepareMembershipList(groups, presenceGroups, includePresence);
        effectiveChannelGroupsList.removeAll(channelGroupsToPostponeSubscription(temporaryUnavailableChannelGroups));
        return effectiveChannelGroupsList;
    }

    private List<String> channelsToPostponeSubscription(final List<TemporaryUnavailableItem> temporaryUnavailableChannels) {
        final List<String> result = new ArrayList<>();

        for (TemporaryUnavailableItem temporaryUnavailableChannel : temporaryUnavailableChannels) {
            if (temporaryUnavailableChannel.getTimestamp()
                    .after(new Date(System.currentTimeMillis() - configuration.getConnectTimeout() * MILLIS_IN_SECOND))) {
                result.add(temporaryUnavailableChannel.getItem());
            }
        }
        return result;
    }

    private List<String> channelGroupsToPostponeSubscription(final List<TemporaryUnavailableItem> temporaryUnavailableChannelGroups) {
        final List<String> result = new ArrayList<>();

        for (TemporaryUnavailableItem temporaryUnavailableChannelGroup : temporaryUnavailableChannelGroups) {
            if (temporaryUnavailableChannelGroup.getTimestamp()
                    .after(new Date(System.currentTimeMillis() - configuration.getConnectTimeout() * MILLIS_IN_SECOND))) {
                result.add(temporaryUnavailableChannelGroup.getItem());
            }
        }
        return result;
    }

    private boolean isEmpty() {
        return (channels.isEmpty() && presenceChannels.isEmpty() && groups.isEmpty() && presenceGroups.isEmpty());
    }


    private List<String> getNames(Map<String, SubscriptionItem> dataStorage) {
        return new ArrayList<>(dataStorage.keySet());
    }

    private void addPresence(List<String> response, Map<String,
            SubscriptionItem> presenceStorage) {
        for (SubscriptionItem presenceChannelGroupItem : presenceStorage.values()) {
            response.add(presenceChannelGroupItem.getName().concat("-pnpres"));
        }
    }

    private List<String> prepareMembershipList(Map<String, SubscriptionItem> dataStorage, Map<String,
            SubscriptionItem> presenceStorage, boolean includePresence) {
        List<String> response = getNames(dataStorage);

        if (includePresence) {
            addPresence(response, presenceStorage);
        }

        return response;
    }

    enum ChannelFilter {
        WITH_TEMPORARY_UNAVAILABLE,
        WITHOUT_TEMPORARY_UNAVAILABLE

    }

    @Data
    public static class SubscriptionStateData {
        private final Map<String, Object> statePayload;
        private final List<String> channelGroups;
        private final List<String> channels;
        private final Long timetoken;
        private final String region;
        private final boolean anythingToSubscribe;
        private final boolean subscribedToOnlyTemporaryUnavailable;
        private final PNStatusCategory announceStatus;
        public boolean isShouldAnnounce() {
            return announceStatus != null;
        }
    }

    @Data
    public static class HeartbeatStateData {
        private final Map<String, Object> statePayload;
        private final List<String> heartbeatChannelGroups;
        private final List<String> heartbeatChannels;
    }
}
