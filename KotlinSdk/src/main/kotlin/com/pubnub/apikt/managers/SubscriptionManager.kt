package com.pubnub.apikt.managers

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.builder.*
import com.pubnub.apikt.callbacks.Listener
import com.pubnub.apikt.callbacks.ReconnectionCallback
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.endpoints.presence.Heartbeat
import com.pubnub.apikt.endpoints.presence.Leave
import com.pubnub.apikt.endpoints.pubsub.Subscribe
import com.pubnub.apikt.enums.PNHeartbeatNotificationOptions
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.server.SubscribeMessage
import com.pubnub.apikt.workers.SubscribeMessageWorker
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.timerTask

class SubscriptionManager(val pubnub: PubNub, private val subscriptionState: StateManager = StateManager()) {

    private companion object {
        private const val HEARTBEAT_INTERVAL_MULTIPLIER = 1000L
    }

    private var subscribeCall: Subscribe? = null
    private var heartbeatCall: Heartbeat? = null

    private var messageQueue: LinkedBlockingQueue<SubscribeMessage> = LinkedBlockingQueue()

    private var duplicationManager: DuplicationManager = DuplicationManager(pubnub.configuration)

    /**
     * Timer for heartbeat operations.
     */
    private var heartbeatTimer: Timer? = null

    internal val listenerManager = ListenerManager(pubnub)
    private val reconnectionManager = ReconnectionManager(pubnub)

    private var consumerThread: Thread? = null

    init {
        reconnectionManager.reconnectionCallback = object : ReconnectionCallback() {
            override fun onReconnection() {
                reconnect()
                subscriptionState.subscriptionStateData(true).let {
                    listenerManager.announce(
                        PNStatus(
                            category = PNStatusCategory.PNReconnectedCategory,
                            operation = PNOperationType.PNSubscribeOperation,
                            error = false,
                            affectedChannels = it.channels,
                            affectedChannelGroups = it.channelGroups
                        )
                    )
                }
            }

            override fun onMaxReconnectionExhaustion() {
                subscriptionState.subscriptionStateData(true).let {

                    listenerManager.announce(
                        PNStatus(
                            category = PNStatusCategory.PNReconnectionAttemptsExhausted,
                            operation = PNOperationType.PNSubscribeOperation,
                            error = false,
                            affectedChannels = it.channels,
                            affectedChannelGroups = it.channelGroups
                        )
                    )
                }

                disconnect()
            }
        }

        if (pubnub.configuration.startSubscriberThread) {
            consumerThread = Thread(
                SubscribeMessageWorker(
                    pubnub,
                    listenerManager,
                    messageQueue,
                    duplicationManager
                )
            )
            consumerThread?.name = "Subscription Manager Consumer Thread"
            consumerThread?.isDaemon = true
            consumerThread?.start()
        }
    }

    fun getSubscribedChannels(): List<String> {
        return subscriptionState.subscriptionStateData(false).channels
    }

    fun getSubscribedChannelGroups(): List<String> {
        return subscriptionState.subscriptionStateData(false).channelGroups
    }

    internal fun adaptStateBuilder(stateOperation: StateOperation) {
        reconnect(stateOperation)
    }

    internal fun adaptSubscribeBuilder(subscribeOperation: SubscribeOperation) {
        reconnect(subscribeOperation)
    }

    internal fun reconnect(
        pubSubOperation: PubSubOperation = NoOpOperation
    ) {
        startSubscribeLoop(pubSubOperation)
        registerHeartbeatTimer(NoOpOperation)
    }

    fun disconnect() {
        heartbeatTimer?.cancel()
        stopSubscribeLoop()
    }

    @Synchronized
    private fun registerHeartbeatTimer(pubSubOperation: PubSubOperation) {
        subscriptionState.handleOperation(pubSubOperation)

        // make sure only one timer is running at a time.
        heartbeatTimer?.cancel()

        // if the interval is 0 or less, do not start the timer
        if (pubnub.configuration.heartbeatInterval <= 0) {
            return
        }

        heartbeatTimer = Timer("Subscription Manager Heartbeat Timer", true)
        heartbeatTimer?.schedule(
            timerTask {
                performHeartbeatLoop()
            },
            0,
            pubnub.configuration.heartbeatInterval * HEARTBEAT_INTERVAL_MULTIPLIER
        )
    }

    @Synchronized
    private fun performHeartbeatLoop() {
        heartbeatCall?.silentCancel()

        val subscriptionStateData = subscriptionState.subscriptionStateData(false)

        // do not start the loop if we do not have any presence channels or channel groups enabled.
        if (subscriptionStateData.channels.isEmpty() &&
            subscriptionStateData.channelGroups.isEmpty() &&
            subscriptionStateData.heartbeatChannels.isEmpty() &&
            subscriptionStateData.heartbeatChannelGroups.isEmpty()
        ) {
            return
        }
        val channels = subscriptionStateData.channels + subscriptionStateData.heartbeatChannels
        val groups = subscriptionStateData.channelGroups + subscriptionStateData.heartbeatChannelGroups
        heartbeatCall = Heartbeat(pubnub, channels, groups)
        heartbeatCall?.async { _, status ->
            val heartbeatVerbosity = pubnub.configuration.heartbeatNotificationOptions

            if (status.error) {
                if (heartbeatVerbosity == PNHeartbeatNotificationOptions.ALL ||
                    heartbeatVerbosity == PNHeartbeatNotificationOptions.FAILURES
                ) {
                    listenerManager.announce(status)
                }
                // stop the heartbeating logic since an error happened.
                heartbeatTimer?.cancel()
            } else {
                if (heartbeatVerbosity == PNHeartbeatNotificationOptions.ALL) {
                    listenerManager.announce(status)
                }
            }
        }
    }

    @Synchronized
    private fun startSubscribeLoop(
        vararg pubSubOperations: PubSubOperation = arrayOf(NoOpOperation)
    ) {
        // this function can be called from different points, make sure any old loop is closed
        stopSubscribeLoop()
        subscriptionState.handleOperation(*pubSubOperations)

        if (pubSubOperations.any { it is SubscribeOperation }) {
            duplicationManager.clearHistory()
        }

        val stateData = subscriptionState.subscriptionStateData(true)
        // do not start the subscribe loop if we have no channels to subscribe to.
        if (stateData.channels.isEmpty() && stateData.channelGroups.isEmpty()) {
            return
        }

        subscribeCall = Subscribe(pubnub).apply {
            channels = stateData.channels
            channelGroups = stateData.channelGroups
            timetoken = stateData.timetoken
            region = stateData.region
            filterExpression = pubnub.configuration.filterExpression.ifBlank { null }
            state = stateData.statePayload
        }

        subscribeCall?.async { result, status ->
            if (status.error) {
                if (status.category == PNStatusCategory.PNTimeoutCategory) {
                    startSubscribeLoop()
                    return@async
                }

                disconnect()
                listenerManager.announce(status)

                if (status.category == PNStatusCategory.PNUnexpectedDisconnectCategory) {
                    // stop all announcements and ask the reconnection manager to start polling for connection restoration..
                    reconnectionManager.startPolling(pubnub.configuration)
                }

                return@async
            }

            val announcedOperation = if (stateData.shouldAnnounce) {
                val pnStatus = createPublicStatus(status).apply {
                    category = PNStatusCategory.PNConnectedCategory
                    error = false
                }

                listenerManager.announce(pnStatus)
                ConnectedStatusAnnouncedOperation
            } else null

            pubnub.configuration.requestMessageCountThreshold?.let {
                // todo default value of size if all ?s are null
                if (it <= result!!.messages.size) {
                    listenerManager.announce(
                        createPublicStatus(status).apply {
                            category = PNStatusCategory.PNRequestMessageCountExceededCategory
                            error = false
                        }
                    )
                }
            }

            if (result!!.messages.isNotEmpty()) {
                messageQueue.addAll(result.messages)
            }

            startSubscribeLoop(
                TimetokenRegionOperation(
                    timetoken = result.metadata.timetoken,
                    region = result.metadata.region
                ),
                announcedOperation ?: NoOpOperation
            )
        }
    }

    private fun stopSubscribeLoop() {
        subscribeCall?.silentCancel()
    }

    private fun stopHeartbeatLoop() {
        heartbeatCall?.silentCancel()
    }

    internal fun adaptPresenceBuilder(presenceOperation: PresenceOperation) {

        if (!pubnub.configuration.suppressLeaveEvents && !presenceOperation.connected) {
            Leave(pubnub).apply {
                channels = presenceOperation.channels
                channelGroups = presenceOperation.channelGroups
            }.async { _, status ->
                listenerManager.announce(status)
            }
        }

        registerHeartbeatTimer(presenceOperation)
    }

    internal fun adaptUnsubscribeBuilder(unsubscribeOperation: UnsubscribeOperation) {
        if (!pubnub.configuration.suppressLeaveEvents) {
            Leave(pubnub).apply {
                channels = unsubscribeOperation.channels
                channelGroups = unsubscribeOperation.channelGroups
            }.async { _, status ->
                listenerManager.announce(status)
            }
        }
        reconnect(pubSubOperation = unsubscribeOperation)
    }

    fun unsubscribeAll() {
        subscriptionState.subscriptionStateData(false).let {
            adaptUnsubscribeBuilder(
                UnsubscribeOperation(
                    channels = it.channels,
                    channelGroups = it.channelGroups
                )
            )
        }
    }

    private fun createPublicStatus(privateStatus: PNStatus): PNStatus {
        with(privateStatus) {
            return PNStatus(
                category = category,
                error = this.error,
                operation = operation,
                exception = exception,
                statusCode = statusCode,
                tlsEnabled = tlsEnabled,
                origin = origin,
                uuid = uuid,
                authKey = authKey,
                affectedChannels = affectedChannels,
                affectedChannelGroups = affectedChannelGroups
            )
        }
    }

    fun addListener(listener: SubscribeCallback) {
        listenerManager.addListener(listener)
    }

    fun removeListener(listener: Listener) {
        listenerManager.removeListener(listener)
    }

    @Synchronized
    fun destroy(forceDestroy: Boolean = false) {
        disconnect()
        if (forceDestroy && consumerThread != null) {
            consumerThread!!.interrupt()
        }
    }
}
