package com.pubnub.apikt.endpoints.files

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PNConfiguration.Companion.isValid
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.endpoints.remoteaction.ExtendedRemoteAction
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.files.PNBaseFile
import com.pubnub.apikt.models.consumer.files.PNPublishFileMessageResult
import com.pubnub.apikt.models.server.files.FileUploadNotification
import com.pubnub.apikt.vendor.Crypto
import com.pubnub.extension.numericString
import com.pubnub.extension.quoted
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.publishFileMessage]
 */
open class PublishFileMessage(
    private val channel: String,
    fileName: String,
    fileId: String,
    private val message: Any? = null,
    private val meta: Any? = null,
    private val ttl: Int? = null,
    private val shouldStore: Boolean? = null,
    pubNub: PubNub
) : Endpoint<List<Any>, PNPublishFileMessageResult>(pubNub) {
    private val pnFile = PNBaseFile(fileId, fileName)

    @Throws(PubNubException::class)
    override fun validateParams() {
        if (channel.isEmpty()) {
            throw PubNubException(PubNubError.CHANNEL_MISSING)
        }
    }

    @Throws(PubNubException::class)
    override fun doWork(queryParams: HashMap<String, String>): Call<List<Any>> {
        val stringifiedMessage: String = pubnub.mapper.toJsonUsingJackson(FileUploadNotification(message, pnFile))
        val messageAsString = if (pubnub.configuration.cipherKey.isValid()) {
            val crypto = Crypto(pubnub.configuration.cipherKey, pubnub.configuration.useRandomInitializationVector)
            crypto.encrypt(stringifiedMessage).quoted()
        } else {
            stringifiedMessage
        }
        meta?.let {
            val stringifiedMeta: String = pubnub.mapper.toJsonUsingJackson(it)
            queryParams["meta"] = stringifiedMeta
        }
        shouldStore?.numericString?.let { queryParams["store"] = it }
        ttl?.let { queryParams["ttl"] = it.toString() }

        return pubnub.retrofitManager.filesService.notifyAboutFileUpload(
            pubnub.configuration.publishKey,
            pubnub.configuration.subscribeKey,
            channel,
            messageAsString,
            queryParams
        )
    }

    @Throws(PubNubException::class)
    override fun createResponse(input: Response<List<Any>>): PNPublishFileMessageResult {
        return input.body()?.let { body ->
            val timetoken = body[2].toString().toLong()
            PNPublishFileMessageResult(timetoken)
        } ?: throw PubNubException(PubNubError.INTERNAL_ERROR)
    }

    override fun getAffectedChannels() = listOf(channel)
    override fun getAffectedChannelGroups(): List<String> = listOf()
    override fun operationType(): PNOperationType = PNOperationType.FileOperation
    override fun isAuthRequired(): Boolean = true
    override fun isSubKeyRequired(): Boolean = true
    override fun isPubKeyRequired(): Boolean = true

    internal class Factory(private val pubNub: PubNub) {
        fun create(
            channel: String,
            fileName: String,
            fileId: String,
            message: Any? = null,
            meta: Any? = null,
            ttl: Int? = null,
            shouldStore: Boolean? = null
        ): ExtendedRemoteAction<PNPublishFileMessageResult> {
            return PublishFileMessage(
                channel = channel,
                fileName = fileName,
                fileId = fileId,
                message = message,
                meta = meta,
                ttl = ttl,
                shouldStore = shouldStore,
                pubNub = pubNub
            )
        }
    }
}
