package com.pubnub.apikt.endpoints

import com.google.gson.JsonElement
import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.history.PNHistoryItemResult
import com.pubnub.apikt.models.consumer.history.PNHistoryResult
import com.pubnub.extension.processHistoryMessage
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * @see [PubNub.history]
 */
class History internal constructor(
    pubnub: PubNub,
    val channel: String,
    val start: Long? = null,
    val end: Long? = null,
    val count: Int,
    val reverse: Boolean,
    val includeTimetoken: Boolean,
    val includeMeta: Boolean
) : Endpoint<JsonElement, PNHistoryResult>(pubnub) {

    internal companion object {
        internal const val MAX_COUNT = 100
    }

    private val countParam: Int = if (count in 1..MAX_COUNT) count else MAX_COUNT

    override fun validateParams() {
        super.validateParams()
        if (channel.isBlank()) throw PubNubException(PubNubError.CHANNEL_MISSING)
    }

    override fun getAffectedChannels() = listOf(channel)

    override fun doWork(queryParams: HashMap<String, String>): Call<JsonElement> {
        addQueryParams(queryParams)

        return pubnub.retrofitManager.historyService.fetchHistory(
            pubnub.configuration.subscribeKey,
            channel,
            queryParams
        )
    }

    override fun createResponse(input: Response<JsonElement>): PNHistoryResult {
        val startTimeToken =
            pubnub.mapper.elementToLong(pubnub.mapper.getArrayElement(input.body()!!, 1))
        val endTimeToken =
            pubnub.mapper.elementToLong(pubnub.mapper.getArrayElement(input.body()!!, 2))

        val messages = mutableListOf<PNHistoryItemResult>()

        val historyData = PNHistoryResult(
            messages = messages,
            startTimetoken = startTimeToken,
            endTimetoken = endTimeToken
        )

        if (pubnub.mapper.getArrayElement(input.body()!!, 0).isJsonArray) {
            val iterator = pubnub.mapper.getArrayIterator(pubnub.mapper.getArrayElement(input.body()!!, 0))!!
            while (iterator.hasNext()) {

                val historyEntry = iterator.next()

                var message: JsonElement
                var timetoken: Long? = null
                var meta: JsonElement? = null

                if (includeTimetoken || includeMeta) {
                    message = pubnub.mapper.getField(historyEntry, "message")!!.processHistoryMessage(pubnub.configuration, pubnub.mapper)
                    if (includeTimetoken) {
                        timetoken = pubnub.mapper.elementToLong(historyEntry, "timetoken")
                    }
                    if (includeMeta) {
                        meta = pubnub.mapper.getField(historyEntry, "meta")
                    }
                } else {
                    message = historyEntry.processHistoryMessage(pubnub.configuration, pubnub.mapper)
                }

                val historyItem = PNHistoryItemResult(
                    entry = message,
                    timetoken = timetoken,
                    meta = meta
                )

                messages.add(historyItem)
            }
        } else {
            throw PubNubException(
                pubnubError = PubNubError.HTTP_ERROR,
                errorMessage = "History is disabled"
            )
        }

        return historyData
    }

    override fun operationType() = PNOperationType.PNHistoryOperation

    private fun addQueryParams(queryParams: MutableMap<String, String>) {
        queryParams["reverse"] = reverse.toString()
        queryParams["include_token"] = includeTimetoken.toString()
        queryParams["include_meta"] = includeMeta.toString()
        queryParams["count"] = countParam.toString()

        start?.run { queryParams["start"] = this.toString().lowercase(Locale.US) }
        end?.run { queryParams["end"] = this.toString().lowercase(Locale.US) }
    }
}
