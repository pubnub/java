package com.pubnub.apikt.models.consumer.presence

import com.google.gson.JsonElement
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.presence.HereNow

/**
 * Result of the [PubNub.hereNow] operation.
 *
 * @property totalChannels Total number channels matching the associated [PubNub.hereNow] call.
 * @property totalOccupancy Total occupancy matching the associated [PubNub.hereNow] call.
 * @property channels A map with values of [PNHereNowChannelData] for each channel.
 */
class PNHereNowResult internal constructor(
    val totalChannels: Int,
    val totalOccupancy: Int,
    val channels: HashMap<String, PNHereNowChannelData> = hashMapOf()
)

/**
 * Wrapper class representing 'here now' data for a given channel.
 *
 * @property channelName The channel name.
 * @property occupancy Total number of UUIDs currently in the channel.
 * @property occupants List of [PNHereNowOccupantData] (users) currently in the channel.
 */
class PNHereNowChannelData internal constructor(
    val channelName: String,
    val occupancy: Int,
    var occupants: List<PNHereNowOccupantData> = emptyList()
)

/**
 * Wrapper class representing a UUID (user) within the means of [PubNub.hereNow] calls.
 *
 * @property uuid UUID of the user if requested via [HereNow.includeUUIDs].
 * @property state Presence State of the user if requested via [HereNow.includeState].
 */
class PNHereNowOccupantData internal constructor(
    val uuid: String,
    val state: JsonElement? = null
)
