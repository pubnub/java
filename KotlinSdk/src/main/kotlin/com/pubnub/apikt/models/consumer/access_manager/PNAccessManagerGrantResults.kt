package com.pubnub.apikt.models.consumer.access_manager

import com.google.gson.annotations.SerializedName
import com.pubnub.apikt.PubNub

/**
 * Result of the [PubNub.grant] operation
 *
 * @property level Permissions level, one of `subkey`, `subkey+auth`, `channel`, `channel-group`,
 * `channel-group+auth` level.
 * @property ttl Time in minutes for which granted permissions are valid. Value of `0` means indefinite.
 * @property subscribeKey The subscribe key.
 * @property channels Access rights per channel.
 * @property channelGroups Access rights per channel group.
 */
class PNAccessManagerGrantResult(
    val level: String,
    val ttl: Int,
    val subscribeKey: String,
    val channels: Map<String, Map<String, PNAccessManagerKeyData>?>,
    val channelGroups: Map<String, Map<String, PNAccessManagerKeyData>?>
)

class PNAccessManagerKeyData {

    /**
     * Is `true` if *read* rights are granted.
     */
    @SerializedName("r")
    internal var readEnabled: Boolean = false

    /**
     * Is `true` if *write* rights are granted.
     */
    @SerializedName("w")
    internal var writeEnabled: Boolean = false

    /**
     * Is `true` if *manage* rights are granted.
     */
    @SerializedName("m")
    internal var manageEnabled: Boolean = false

    /**
     * Is `true` if *delete* rights are granted.
     */
    @SerializedName("d")
    internal var deleteEnabled: Boolean = false
}

class PNAccessManagerKeysData {
    @SerializedName("auths")
    internal val authKeys: Map<String, PNAccessManagerKeyData>? = null
}
