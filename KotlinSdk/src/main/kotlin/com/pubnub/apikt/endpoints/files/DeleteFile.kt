package com.pubnub.apikt.endpoints.files

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.files.PNDeleteFileResult
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.deleteFile]
 */
class DeleteFile(
    private val channel: String,
    private val fileName: String,
    private val fileId: String,
    pubNub: PubNub
) : Endpoint<Unit, PNDeleteFileResult?>(pubNub) {
    @Throws(PubNubException::class)
    override fun validateParams() {
        if (channel.isEmpty()) {
            throw PubNubException(PubNubError.CHANNEL_MISSING)
        }
    }

    @Throws(PubNubException::class)
    override fun doWork(queryParams: HashMap<String, String>): Call<Unit> =
        pubnub.retrofitManager.filesService.deleteFile(
            pubnub.configuration.subscribeKey,
            channel,
            fileId,
            fileName,
            queryParams
        )

    @Throws(PubNubException::class)
    override fun createResponse(input: Response<Unit>): PNDeleteFileResult = if (input.isSuccessful) {
        PNDeleteFileResult(input.code())
    } else {
        throw PubNubException(PubNubError.HTTP_ERROR)
    }

    override fun getAffectedChannels() = listOf(channel)
    override fun getAffectedChannelGroups(): List<String> = listOf()
    override fun operationType(): PNOperationType = PNOperationType.FileOperation
    override fun isAuthRequired(): Boolean = true
    override fun isSubKeyRequired(): Boolean = true
    override fun isPubKeyRequired(): Boolean = false
}
