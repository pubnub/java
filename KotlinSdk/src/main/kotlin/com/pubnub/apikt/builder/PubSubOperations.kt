package com.pubnub.apikt.builder

// basic publish/subscribe class
internal sealed class PubSubOperation

// concrete publish/subscribe cases
internal data class SubscribeOperation(
    val channels: List<String> = emptyList(),
    val channelGroups: List<String> = emptyList(),
    val presenceEnabled: Boolean = false,
    val timetoken: Long = 0L
) : PubSubOperation()

internal data class UnsubscribeOperation(
    val channels: List<String> = emptyList(),
    val channelGroups: List<String> = emptyList()
) : PubSubOperation()

internal data class PresenceOperation(
    val channels: List<String> = emptyList(),
    val channelGroups: List<String> = emptyList(),
    val connected: Boolean = false
) : PubSubOperation()

internal data class StateOperation(
    val channels: List<String> = emptyList(),
    val channelGroups: List<String> = emptyList(),
    val state: Any? = null
) : PubSubOperation()

internal data class TimetokenRegionOperation(
    val timetoken: Long = 0L,
    val region: String
) : PubSubOperation()

internal object ConnectedStatusAnnouncedOperation : PubSubOperation()

internal object NoOpOperation : PubSubOperation()
