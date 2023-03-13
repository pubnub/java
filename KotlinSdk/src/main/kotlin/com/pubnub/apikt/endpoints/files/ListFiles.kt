package com.pubnub.apikt.endpoints.files

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.files.PNListFilesResult
import com.pubnub.apikt.models.consumer.objects.PNPage
import com.pubnub.apikt.models.server.files.ListFilesResult
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.listFiles]
 */
class ListFiles(
    private val channel: String,
    private val limit: Int? = null,
    private val next: PNPage.PNNext? = null,
    pubNub: PubNub
) : Endpoint<ListFilesResult, PNListFilesResult>(pubNub) {

    @Throws(PubNubException::class)
    override fun validateParams() {
        if (channel.isEmpty()) {
            throw PubNubException(PubNubError.CHANNEL_MISSING)
        }
        if (limit != null && limit !in MIN_LIMIT..MAX_LIMIT) {
            throw PubNubException(PubNubError.INVALID_ARGUMENTS).copy(
                errorMessage = "Limit should be in range from 1 to 100 (both inclusive)"
            )
        }
        if (next != null && (next.pageHash.isBlank())) {
            throw PubNubException(PubNubError.INVALID_ARGUMENTS).copy(
                errorMessage = "Next should not be an empty string"
            )
        }
    }

    @Throws(PubNubException::class)
    override fun doWork(queryParams: HashMap<String, String>): Call<ListFilesResult> {
        queryParams[LIMIT_QUERY_PARAM] = (limit ?: DEFAULT_LIMIT).toString()
        if (next != null) {
            queryParams[NEXT_PAGE_QUERY_PARAM] = next.pageHash
        }
        return pubnub.retrofitManager.filesService.listFiles(
            pubnub.configuration.subscribeKey,
            channel,
            queryParams
        )
    }

    @Throws(PubNubException::class)
    override fun createResponse(input: Response<ListFilesResult>): PNListFilesResult {
        return input.body()?.let { body ->
            PNListFilesResult(
                body.count,
                body.next,
                body.status,
                body.data
            )
        } ?: throw PubNubException(PubNubError.INTERNAL_ERROR)
    }

    override fun getAffectedChannels() = listOf(channel)
    override fun getAffectedChannelGroups(): List<String> = listOf()
    override fun operationType(): PNOperationType = PNOperationType.FileOperation
    override fun isAuthRequired(): Boolean = true
    override fun isSubKeyRequired(): Boolean = true
    override fun isPubKeyRequired(): Boolean = false

    companion object {
        private const val LIMIT_QUERY_PARAM = "limit"
        private const val NEXT_PAGE_QUERY_PARAM = "next"
        private const val DEFAULT_LIMIT = 100
        private const val MIN_LIMIT = 1
        private const val MAX_LIMIT = 100
    }
}
