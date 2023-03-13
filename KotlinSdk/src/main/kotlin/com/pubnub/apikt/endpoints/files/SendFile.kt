package com.pubnub.apikt.endpoints.files

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.endpoints.remoteaction.ComposableRemoteAction
import com.pubnub.apikt.endpoints.remoteaction.ExtendedRemoteAction
import com.pubnub.apikt.endpoints.remoteaction.MappingRemoteAction
import com.pubnub.apikt.endpoints.remoteaction.RetryingRemoteAction
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.files.PNBaseFile
import com.pubnub.apikt.models.consumer.files.PNFileUploadResult
import com.pubnub.apikt.models.consumer.files.PNPublishFileMessageResult
import com.pubnub.apikt.models.server.files.FileUploadRequestDetails
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicReference

/**
 * @see [PubNub.sendFile]
 */
class SendFile internal constructor(
    private val channel: String,
    private val fileName: String,
    inputStream: InputStream,
    private val message: Any? = null,
    private val meta: Any? = null,
    private val ttl: Int? = null,
    private val shouldStore: Boolean? = null,
    private val cipherKey: String? = null,
    private val fileMessagePublishRetryLimit: Int,
    private val executorService: ExecutorService,
    generateUploadUrlFactory: GenerateUploadUrl.Factory,
    publishFileMessageFactory: PublishFileMessage.Factory,
    sendFileToS3Factory: UploadFile.Factory
) : ExtendedRemoteAction<PNFileUploadResult> {

    private val sendFileMultistepAction: ExtendedRemoteAction<PNFileUploadResult> = sendFileComposedActions(
        generateUploadUrlFactory,
        publishFileMessageFactory,
        sendFileToS3Factory,
        inputStream.readBytes()
    )

    @Throws(PubNubException::class)
    override fun sync(): PNFileUploadResult? {
        validate()
        return sendFileMultistepAction.sync()
    }

    override fun async(callback: (result: PNFileUploadResult?, status: PNStatus) -> Unit) {
        executorService.execute {
            try {
                validate()
                sendFileMultistepAction.async(callback)
            } catch (ex: PubNubException) {
                callback(
                    null,
                    PNStatus(
                        category = PNStatusCategory.PNBadRequestCategory,
                        error = true,
                        operation = operationType(),
                        exception = ex
                    )
                )
            }
        }
    }

    @Throws(PubNubException::class)
    private fun validate() {
        if (channel.isEmpty()) {
            throw PubNubException(PubNubError.CHANNEL_MISSING)
        }
        if (fileName.isEmpty()) {
            throw PubNubException(PubNubError.INVALID_ARGUMENTS)
                .copy(errorMessage = "File name cannot be null nor empty")
        }
    }

    private fun sendFileComposedActions(
        generateUploadUrlFactory: GenerateUploadUrl.Factory,
        publishFileMessageFactory: PublishFileMessage.Factory,
        sendFileToS3Factory: UploadFile.Factory,
        content: ByteArray
    ): ExtendedRemoteAction<PNFileUploadResult> {
        val result = AtomicReference<FileUploadRequestDetails>()
        return ComposableRemoteAction
            .firstDo(generateUploadUrlFactory.create(channel, fileName)) // generateUrl
            .then { res ->
                result.set(res)
                sendFileToS3Factory.create(fileName, content, cipherKey, res) // upload to s3
            }
            .checkpoint()
            .then {
                val details = result.get()
                RetryingRemoteAction.autoRetry(
                    publishFileMessageFactory.create(
                        channel = channel,
                        fileName = details.data.name,
                        fileId = details.data.id,
                        message = message,
                        meta = meta,
                        ttl = ttl,
                        shouldStore = shouldStore
                    ),
                    fileMessagePublishRetryLimit,
                    PNOperationType.FileOperation,
                    executorService
                ) // publish file message
            }
            .then { res: PNPublishFileMessageResult ->
                mapPublishFileMessageToFileUpload(result.get(), res) // prepare result to return
            }
    }

    private fun mapPublishFileMessageToFileUpload(
        requestDetails: FileUploadRequestDetails,
        res: PNPublishFileMessageResult
    ): ExtendedRemoteAction<PNFileUploadResult> = MappingRemoteAction.map(
        res,
        operationType()
    ) { pnPublishFileMessageResult: PNPublishFileMessageResult ->

        PNFileUploadResult(
            pnPublishFileMessageResult.timetoken,
            HttpURLConnection.HTTP_OK,
            PNBaseFile(requestDetails.data.id, requestDetails.data.name)
        )
    }

    override fun retry() {
        sendFileMultistepAction.retry()
    }

    override fun silentCancel() {
        sendFileMultistepAction.silentCancel()
    }

    override fun operationType(): PNOperationType {
        return PNOperationType.FileOperation
    }
}
