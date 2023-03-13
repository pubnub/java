package com.pubnub.apikt.enums

enum class PNReconnectionPolicy {

    /**
     * No reconnection policy. If the subscribe loop gets cancelled due to network or other issues,
     * the SDK will not attempt to try to restore it.
     */
    NONE,

    /**
     * The SDK will try to reconnect to the network by doing so every 3 seconds.
     *
     * @see [PNConfiguration.maximumReconnectionRetries]
     */
    LINEAR,

    /**
     * The SDK will try to reconnect to the network by doing so in non-fixed intervals.
     * ie. the interval between retries is another power of 2, starting from 0 to 5.
     *
     * @see [PNConfiguration.maximumReconnectionRetries]
     */
    EXPONENTIAL
}
