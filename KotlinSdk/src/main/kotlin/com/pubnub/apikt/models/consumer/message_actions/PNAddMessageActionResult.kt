package com.pubnub.apikt.models.consumer.message_actions

import com.pubnub.apikt.PubNub

/**
 * Result for the [PubNub.addMessageAction] API operation.
 *
 * Essentially a wrapper around [PNMessageAction].
 */
class PNAddMessageActionResult internal constructor(action: PNMessageAction) :
    PNMessageAction(action)
