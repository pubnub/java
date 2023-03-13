package com.pubnub.apikt.models.consumer.message_actions

/**
 * Concrete implementation of a message action.
 *
 * Add or remove actions on published messages to build features like receipts,
 * reactions or to associate custom metadata to messages.
 *
 * Clients can subscribe to a channel to receive message action events on that channel.
 * They can also fetch past message actions from PubNub Storage independently or when they fetch original messages.
 *
 * @property type Message action's type.
 * @property value Message action's value.
 * @property messageTimetoken Timestamp when the actual message was created the message action belongs to.
 */
open class PNMessageAction(
    val type: String,
    val value: String,
    val messageTimetoken: Long
) {

    internal constructor(pnMessageAction: PNMessageAction) : this(
        pnMessageAction.type,
        pnMessageAction.value,
        pnMessageAction.messageTimetoken
    ) {
        this.uuid = pnMessageAction.uuid
        this.actionTimetoken = pnMessageAction.actionTimetoken
    }

    /**
     * Message action's author.
     */
    var uuid: String? = null
        private set

    /**
     * Timestamp when the message action was created.
     */
    var actionTimetoken: Long? = null
        private set
}
