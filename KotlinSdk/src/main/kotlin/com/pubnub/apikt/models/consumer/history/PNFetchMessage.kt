package com.pubnub.apikt.models.consumer.history

import com.google.gson.JsonElement
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.FetchMessages
import com.pubnub.apikt.models.consumer.PNBoundedPage

/**
 * Result of the [PubNub.fetchMessages] operation.
 *
 * @property channels Map of channels and their respective lists of [PNFetchMessageItem].
 */
data class PNFetchMessagesResult(
    val channels: Map<String, List<PNFetchMessageItem>>,
    val page: PNBoundedPage?
)

/**
 * Encapsulates a message in terms of a batch history entry.
 *
 * @property uuid Publisher uuid. Is `null` if not requested.
 * @property message The actual message content.
 * @property timetoken Publish timetoken of the message.
 * @property meta Metadata of the message, if requested via [FetchMessages.includeMeta].
 * Is `null` if not requested, otherwise an empty string if requested but no associated metadata.
 * @property actions The message actions associated with the message.
 * Is `null` if not requested via [FetchMessages.includeMessageActions].
 * The key of the map is the action type. The value is another map,
 * which key is the actual value of the message action,
 * and the key being a list of actions, ie. a list of UUIDs which have posted such a message action.
 *
 * @see [Action]
 */
data class PNFetchMessageItem(
    val uuid: String?,
    val message: JsonElement,
    val meta: JsonElement?,
    val timetoken: Long,
    val actions: Map<String, Map<String, List<Action>>>? = null
)

/**
 * Encapsulates a message action in terms of batch history.
 *
 * @property uuid The UUID of the publisher.
 * @property actionTimetoken The publish timetoken of the message action.
 */
data class Action(
    val uuid: String,
    val actionTimetoken: String
)
