package com.pubnub.apikt.enums

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.models.consumer.PNStatus

/**
 * Check the status category via [PNStatus.category] in the `async` callback
 * when executing API methods like [PubNub.publish] or [PubNub.history].
 *
 * Or in the [SubscribeCallback.status] for API methods like [PubNub.subscribe] or [PubNub.unsubscribeAll] ie.
 * methods able to manage the channel mix.
 */
enum class PNStatusCategory {

    /**
     * Successful acknowledgment of an operation.
     */
    PNAcknowledgmentCategory,

    /**
     * Request failed because of access error (active PAM).
     * [PNStatus.affectedChannels] or [PNStatus.affectedChannelGroups] contain list of channels and groups
     * the client can't access to.
     */
    PNAccessDeniedCategory,

    /**
     * Processing has failed because of request time out.
     * This may happen due to very slow connection when the request doesn't have enough time to complete processing.
     */
    PNTimeoutCategory,

    /**
     * SDK subscribed with a new mix of channels (fired every time the channel / channel group mix changed).
     */
    PNConnectedCategory,

    /**
     * SDK was able to reconnect to PubNub, i.e. the subscription loop has been reconnected.
     */
    PNReconnectedCategory,

    /**
     * Previously started subscribe loop did fail and at this moment client disconnected from real-time data channels.
     */
    PNUnexpectedDisconnectCategory,

    /**
     * Request was cancelled by user.
     */
    PNCancelledCategory,

    /**
     * PubNub API server was unable to parse SDK request correctly.
     *
     * Request can't be completed because not all required values have been passed or passed values have
     * unexpected data type.
     *
     * The SDK will send a `PNBadRequestCategory` when one or more parameters are missing
     * like message, channel, subscribe key, publish key.
     */
    PNBadRequestCategory,

    /**
     * PubNub sent a malformed response.
     * This may happen when you connect to a public WiFi Hotspot that requires you to auth via your web browser first,
     * or if there is a proxy somewhere returning an HTML access denied error,
     * or if there was an intermittent server issue.
     */
    PNMalformedResponseCategory,

    /**
     * Fired when the limit is exceeded by the number of messages received in a single subscribe request.
     */
    PNRequestMessageCountExceededCategory,

    /**
     * The subscribe loop has been stopped due maximum reconnection exhausted.
     */
    PNReconnectionAttemptsExhausted,

    /**
     * The subscriber got a HTTP 404 from the server.
     */
    PNNotFoundCategory,

    /**
     * The subscriber got a 4xx code from the server, other than 400, 403 and 404
     */
    PNUnknownCategory,
}
