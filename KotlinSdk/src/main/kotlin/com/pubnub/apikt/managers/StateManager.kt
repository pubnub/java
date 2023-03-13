package com.pubnub.apikt.managers

import com.pubnub.apikt.builder.*
import com.pubnub.apikt.models.SubscriptionItem

class StateManager {

    /**
     * Contains a list of subscribed channels
     */
    private val channels: HashMap<String, SubscriptionItem> = hashMapOf()

    /**
     * Contains a list of subscribed presence channels.
     */
    private val presenceChannels: HashMap<String, SubscriptionItem> = hashMapOf()

    /**
     * Contains a list of subscribed channel groups.
     */
    private val groups: HashMap<String, SubscriptionItem> = hashMapOf()

    /**
     * Contains a list of subscribed presence channel groups.
     */
    private val presenceGroups: HashMap<String, SubscriptionItem> = hashMapOf()

    private val heartbeatChannels: HashMap<String, SubscriptionItem> = hashMapOf()
    private val heartbeatGroups: HashMap<String, SubscriptionItem> = hashMapOf()

    /**
     * Keep track of Region to support PSV2 specification.
     */
    private var region: String? = null
    private var timetoken = 0L

    /**
     * Store the latest timetoken to subscribe with, null by default to get the latest timetoken.
     */
    private var storedTimetoken: Long? = null

    private var shouldAnnounce: Boolean = false

    @Synchronized
    internal fun handleOperation(vararg pubSubOperations: PubSubOperation) {
        pubSubOperations.forEach { pubSubOperation ->
            when (pubSubOperation) {
                is SubscribeOperation -> {
                    adaptSubscribeBuilder(subscribeOperation = pubSubOperation)
                    shouldAnnounce = true
                }
                is UnsubscribeOperation -> {
                    adaptUnsubscribeBuilder(unsubscribeOperation = pubSubOperation)
                    shouldAnnounce = true
                }
                is PresenceOperation -> adaptPresenceBuilder(presenceOperation = pubSubOperation)
                is StateOperation -> adaptStateBuilder(stateOperation = pubSubOperation)
                is TimetokenRegionOperation -> {
                    if (storedTimetoken != null) {
                        timetoken = storedTimetoken!!
                        storedTimetoken = null
                    } else {
                        timetoken = pubSubOperation.timetoken
                    }

                    region = pubSubOperation.region
                }
                ConnectedStatusAnnouncedOperation -> {
                    shouldAnnounce = false
                }
                NoOpOperation -> {
                    // do nothing
                }
            }
        }
    }

    @Synchronized
    internal fun subscriptionStateData(includePresence: Boolean): SubscriptionStateData {
        return SubscriptionStateData(
            channelGroups = prepareChannelGroupList(includePresence),
            channels = prepareChannelList(includePresence),
            heartbeatChannelGroups = prepareHeartbeatChannelGroupList(includePresence),
            heartbeatChannels = prepareHeartbeatChannelList(includePresence),
            timetoken = timetoken,
            region = region,
            statePayload = createStatePayload(),
            shouldAnnounce = shouldAnnounce
        )
    }

    private fun adaptSubscribeBuilder(subscribeOperation: SubscribeOperation) {
        for (channel in subscribeOperation.channels) {
            if (channel.isEmpty()) {
                continue
            }
            val subscriptionItem = SubscriptionItem(channel)

            channels[channel] = subscriptionItem

            if (subscribeOperation.presenceEnabled) {
                val presenceSubscriptionItem = SubscriptionItem(channel)
                presenceChannels[channel] = presenceSubscriptionItem
            }
        }
        for (channelGroup in subscribeOperation.channelGroups) {
            if (channelGroup.isEmpty()) {
                continue
            }
            val subscriptionItem = SubscriptionItem(channelGroup)
            groups.put(channelGroup, subscriptionItem)
            if (subscribeOperation.presenceEnabled) {
                val presenceSubscriptionItem = SubscriptionItem(channelGroup)
                presenceGroups[channelGroup] = presenceSubscriptionItem
            }
        }
        timetoken = subscribeOperation.timetoken

        // if the timetoken is not at starting position, reset the timetoken to get a connected event
        // and store the old timetoken to be reused later during subscribe.
        if (timetoken != 0L) {
            storedTimetoken = timetoken
        }
        timetoken = 0L
    }

    private fun adaptStateBuilder(stateOperation: StateOperation) {
        for (channel in stateOperation.channels) {
            val subscribedChannel = channels[channel]
            subscribedChannel?.state = stateOperation.state
        }
        for (channelGroup in stateOperation.channelGroups) {
            val subscribedChannelGroup = groups[channelGroup]
            subscribedChannelGroup?.state = stateOperation.state
        }
    }

    private fun adaptUnsubscribeBuilder(unsubscribeOperation: UnsubscribeOperation) {
        for (channel in unsubscribeOperation.channels) {
            channels.remove(channel)
            presenceChannels.remove(channel)
        }
        for (channelGroup in unsubscribeOperation.channelGroups) {
            groups.remove(channelGroup)
            presenceGroups.remove(channelGroup)
        }
        // if we unsubscribed from all the channels, reset the timetoken back to zero and remove the region.
        if (isEmpty()) {
            region = null
            storedTimetoken = null
            timetoken = 0L
        } else {
            storedTimetoken = timetoken
            timetoken = 0L
        }
    }

    private fun adaptPresenceBuilder(presenceOperation: PresenceOperation) {
        for (channel in presenceOperation.channels) {
            if (channel.isEmpty()) {
                continue
            }
            if (presenceOperation.connected) {
                val subscriptionItem = SubscriptionItem(channel)
                heartbeatChannels[channel] = subscriptionItem
            } else {
                heartbeatChannels.remove(channel)
            }
        }
        for (channelGroup in presenceOperation.channelGroups) {
            if (channelGroup.isEmpty()) {
                continue
            }
            if (presenceOperation.connected) {
                val subscriptionItem = SubscriptionItem(channelGroup)
                heartbeatGroups[channelGroup] = subscriptionItem
            } else {
                heartbeatGroups.remove(channelGroup)
            }
        }
    }

    private fun createStatePayload(): Map<String, Any?> {
        val stateResponse: HashMap<String, Any?> = hashMapOf()
        for (channel in channels.values) {
            if (channel.state != null) {
                stateResponse[channel.name] = channel.state
            }
        }
        for (channelGroup in groups.values) {
            if (channelGroup.state != null) {
                stateResponse[channelGroup.name] = channelGroup.state
            }
        }
        return stateResponse
    }

    private fun prepareChannelList(includePresence: Boolean): List<String> {
        return prepareMembershipList(channels, presenceChannels, includePresence)
    }

    private fun prepareChannelGroupList(includePresence: Boolean): List<String> {
        return prepareMembershipList(groups, presenceGroups, includePresence)
    }

    private fun prepareHeartbeatChannelList(includePresence: Boolean): List<String> {
        return prepareMembershipList(heartbeatChannels, presenceChannels, includePresence)
    }

    private fun prepareHeartbeatChannelGroupList(includePresence: Boolean): List<String> {
        return prepareMembershipList(heartbeatGroups, presenceGroups, includePresence)
    }

    private fun isEmpty(): Boolean {
        return channels.isEmpty() && presenceChannels.isEmpty() && groups.isEmpty() && presenceGroups.isEmpty()
    }

    private fun prepareMembershipList(
        dataStorage: Map<String, SubscriptionItem>,
        presenceStorage: Map<String, SubscriptionItem>,
        includePresence: Boolean
    ): List<String> {
        val response: MutableList<String> = ArrayList()
        for (channelGroupItem in dataStorage.values) {
            response.add(channelGroupItem.name)
        }
        if (includePresence) {
            for (presenceChannelGroupItem in presenceStorage.values) {
                response.add(presenceChannelGroupItem.name + "-pnpres")
            }
        }
        return response
    }
}

internal data class SubscriptionStateData(
    val statePayload: Map<String, Any?>,
    val heartbeatChannelGroups: List<String>,
    val heartbeatChannels: List<String>,
    val channelGroups: List<String>,
    val channels: List<String>,
    val timetoken: Long,
    val region: String?,
    val shouldAnnounce: Boolean
)
