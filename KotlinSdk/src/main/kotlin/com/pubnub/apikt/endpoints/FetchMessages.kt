package com.pubnub.apikt.endpoints

import com.pubnub.apikt.*
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNBoundedPage
import com.pubnub.apikt.models.consumer.history.PNFetchMessagesResult
import com.pubnub.apikt.models.server.FetchMessagesEnvelope
import com.pubnub.extension.limit
import com.pubnub.extension.nonPositiveToNull
import com.pubnub.extension.processHistoryMessage
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * @see [PubNub.fetchMessages]
 */
class FetchMessages internal constructor(
    pubnub: PubNub,
    val channels: List<String>,
    val page: PNBoundedPage,
    val includeUUID: Boolean,
    val includeMeta: Boolean,
    val includeMessageActions: Boolean
) : Endpoint<FetchMessagesEnvelope, PNFetchMessagesResult>(pubnub) {

    internal companion object {
        private const val SINGLE_CHANNEL_DEFAULT_MESSAGES = 100
        private const val SINGLE_CHANNEL_MAX_MESSAGES = 100
        private const val MULTIPLE_CHANNEL_DEFAULT_MESSAGES = 25
        private const val MULTIPLE_CHANNEL_MAX_MESSAGES = 25
        private const val DEFAULT_MESSAGES_WITH_ACTIONS = 25
        private const val MAX_MESSAGES_WITH_ACTIONS = 25

        internal fun effectiveMax(
            maximumPerChannel: Int?,
            includeMessageActions: Boolean,
            numberOfChannels: Int
        ): Int = when {
            includeMessageActions ->
                maximumPerChannel?.limit(MAX_MESSAGES_WITH_ACTIONS)?.nonPositiveToNull()
                    ?: DEFAULT_MESSAGES_WITH_ACTIONS
            numberOfChannels == 1 ->
                maximumPerChannel?.limit(SINGLE_CHANNEL_MAX_MESSAGES)?.nonPositiveToNull()
                    ?: SINGLE_CHANNEL_DEFAULT_MESSAGES
            else ->
                maximumPerChannel?.limit(MULTIPLE_CHANNEL_MAX_MESSAGES)?.nonPositiveToNull()
                    ?: MULTIPLE_CHANNEL_DEFAULT_MESSAGES
        }
    }

    override fun validateParams() {
        super.validateParams()
        if (channels.isEmpty()) throw PubNubException(PubNubError.CHANNEL_MISSING)
        if (includeMessageActions && channels.size > 1) throw PubNubException(PubNubError.HISTORY_MESSAGE_ACTIONS_MULTIPLE_CHANNELS)
    }

    override fun getAffectedChannels() = channels

    override fun doWork(queryParams: HashMap<String, String>): Call<FetchMessagesEnvelope> {
        addQueryParams(queryParams)

        return if (!includeMessageActions) {
            pubnub.retrofitManager.historyService.fetchMessages(
                subKey = pubnub.configuration.subscribeKey,
                channels = channels.toCsv(),
                options = queryParams
            )
        } else {
            pubnub.retrofitManager.historyService.fetchMessagesWithActions(
                subKey = pubnub.configuration.subscribeKey,
                channel = channels.first(),
                options = queryParams
            )
        }
    }

    override fun createResponse(input: Response<FetchMessagesEnvelope>): PNFetchMessagesResult {
        val body = input.body()!!
        val channelsMap = body.channels.mapValues { (_, value) ->
            value.map { messageItem ->
                val newMessage = messageItem.message.processHistoryMessage(pubnub.configuration, pubnub.mapper)
                val newActions = if (includeMessageActions) messageItem.actions ?: mapOf() else messageItem.actions
                messageItem.copy(actions = newActions, message = newMessage)
            }
        }.toMap()

        val page = body.more?.let {
            PNBoundedPage(start = it.start, end = it.end, limit = it.max)
        }
        return PNFetchMessagesResult(channels = channelsMap, page = page)
    }

    override fun operationType() = PNOperationType.PNFetchMessagesOperation

    private fun addQueryParams(queryParams: MutableMap<String, String>) {
        queryParams["max"] =
            effectiveMax(page.limit, includeMessageActions, channels.size).toString()
        queryParams["include_uuid"] = includeUUID.toString()

        page.start?.run { queryParams["start"] = this.toString().lowercase(Locale.US) }
        page.end?.run { queryParams["end"] = this.toString().lowercase(Locale.US) }

        if (includeMeta) queryParams["include_meta"] = includeMeta.toString()
    }
}
