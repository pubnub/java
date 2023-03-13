package com.pubnub.apikt.endpoints.files

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.endpoints.remoteaction.ExtendedRemoteAction
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.server.files.FileUploadRequestDetails
import com.pubnub.apikt.models.server.files.FormField
import com.pubnub.apikt.services.S3Service
import com.pubnub.apikt.vendor.FileEncryptionUtil
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import javax.xml.parsers.DocumentBuilderFactory

internal class UploadFile(
    private val s3Service: S3Service,
    private val fileName: String,
    private val content: ByteArray,
    private val cipherKey: String?,
    private val key: FormField,
    private val formParams: List<FormField>,
    private val baseUrl: String
) : ExtendedRemoteAction<Unit> {
    private var call: Call<Unit>? = null

    @Throws(PubNubException::class)
    private fun prepareCall(): Call<Unit> {
        val builder = MultipartBody.Builder().setType(MultipartBody.FORM)
        addFormParamsWithKeyFirst(key, formParams, builder)
        val mediaType = getMediaType(formParams.findContentType())

        val bytes = prepareBytes(content, cipherKey)
        builder.addFormDataPart(FILE_PART_MULTIPART, fileName, bytes.toRequestBody(mediaType, 0, bytes.size))
        return s3Service.upload(baseUrl, builder.build())
    }

    private fun prepareBytes(content: ByteArray, cipherKey: String?): ByteArray =
        if (cipherKey == null) {
            content
        } else {
            FileEncryptionUtil.encryptToBytes(content, cipherKey)
        }

    private fun List<FormField>.findContentType(): String? {
        return find { (key, _) ->
            key.equals(CONTENT_TYPE_HEADER, ignoreCase = true)
        }?.value
    }

    private fun getMediaType(contentType: String?): MediaType {
        return if (contentType == null) {
            APPLICATION_OCTET_STREAM
        } else try {
            contentType.toMediaType()
        } catch (t: Throwable) {
            log.warn("Content-Type: $contentType was not recognized by MediaType.get", t)
            APPLICATION_OCTET_STREAM
        }
    }

    @Throws(PubNubException::class)
    override fun sync() {
        call = prepareCall()
        val serverResponse = try {
            call!!.execute()
        } catch (e: IOException) {
            throw PubNubException(
                errorMessage = e.message,
                affectedCall = call,
                pubnubError = PubNubError.PARSING_ERROR
            )
        }
        if (!serverResponse.isSuccessful) {
            throw createException(serverResponse)
        }
    }

    override fun async(callback: (result: Unit?, status: PNStatus) -> Unit) {
        try {
            call = prepareCall()
            call!!.enqueue(object : Callback<Unit> {
                override fun onResponse(performedCall: Call<Unit>, response: Response<Unit>) {
                    if (!response.isSuccessful) {
                        val ex = createException(response)
                        val pnStatusCategory = response.getCategory()
                        callback(
                            null,
                            createStatusResponse(pnStatusCategory, response, ex)
                        )
                        return
                    }
                    callback(
                        Unit,
                        createStatusResponse(
                            PNStatusCategory.PNAcknowledgmentCategory, response,
                            null
                        )
                    )
                }

                override fun onFailure(performedCall: Call<Unit>, throwable: Throwable) {
                    if (call!!.isCanceled) {
                        return
                    }
                    val (statusCategory, error) = when (throwable) {
                        is UnknownHostException -> PNStatusCategory.PNUnexpectedDisconnectCategory to PubNubError.CONNECTION_NOT_SET
                        is SocketException, is SSLException -> PNStatusCategory.PNUnexpectedDisconnectCategory to PubNubError.CONNECT_EXCEPTION
                        is SocketTimeoutException -> PNStatusCategory.PNTimeoutCategory to PubNubError.SUBSCRIBE_TIMEOUT
                        else -> if (performedCall.isCanceled) {
                            PNStatusCategory.PNCancelledCategory to PubNubError.HTTP_ERROR
                        } else {
                            PNStatusCategory.PNBadRequestCategory to PubNubError.HTTP_ERROR
                        }
                    }
                    callback(
                        null,
                        createStatusResponse(
                            statusCategory,
                            null,
                            PubNubException(error).copy(
                                errorMessage = throwable.message ?: error.message
                            )
                        )
                    )
                }
            })
        } catch (e: IOException) {
            callback(
                null,
                createStatusResponse(PNStatusCategory.PNUnknownCategory, null, e)
            )
        } catch (e: PubNubException) {
            callback(
                null,
                createStatusResponse(PNStatusCategory.PNUnknownCategory, null, e)
            )
        }
    }

    private fun Response<*>.getCategory(): PNStatusCategory = when (code()) {
        HttpURLConnection.HTTP_UNAUTHORIZED,
        HttpURLConnection.HTTP_FORBIDDEN -> PNStatusCategory.PNAccessDeniedCategory
        HttpURLConnection.HTTP_BAD_REQUEST -> PNStatusCategory.PNBadRequestCategory
        else -> PNStatusCategory.PNUnknownCategory
    }

    override fun retry() {}
    override fun silentCancel() {
        if (!call!!.isCanceled) {
            call!!.cancel()
        }
    }

    private fun createException(response: Response<Unit>): PubNubException {
        return try {
            PubNubException(
                errorMessage = response.readErrorMessage(),
                affectedCall = call,
                statusCode = response.code()
            )
        } catch (e: Exception) {
            PubNubException(
                errorMessage = e.message,
                affectedCall = call,
                statusCode = response.code()
            )
        }
    }

    private fun Response<Unit>.readErrorMessage(): String {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(errorBody()!!.byteStream())
        doc.documentElement.normalize()
        val elements = doc.getElementsByTagName("Message")
        return elements.item(0)?.firstChild?.nodeValue ?: "N/A"
    }

    private fun createStatusResponse(
        category: PNStatusCategory,
        response: Response<Unit>?,
        throwable: Exception?
    ): PNStatus {
        return PNStatus(
            category = category,
            operation = operationType(),
            statusCode = response?.code(),
            tlsEnabled = response?.raw()?.request?.url?.isHttps,
            origin = response?.raw()?.request?.url?.host,
            error = response == null || throwable != null
        ).apply { executedEndpoint = this@UploadFile }
    }

    internal class Factory(private val pubNub: PubNub) {
        fun create(
            fileName: String,
            content: ByteArray,
            cipherKey: String?,
            fileUploadRequestDetails: FileUploadRequestDetails
        ): ExtendedRemoteAction<Unit> {
            val effectiveCipherKey = FileEncryptionUtil.effectiveCipherKey(pubNub, cipherKey)
            return UploadFile(
                pubNub.retrofitManager.s3Service,
                fileName,
                content,
                effectiveCipherKey,
                fileUploadRequestDetails.keyFormField, fileUploadRequestDetails.formFields,
                fileUploadRequestDetails.url
            )
        }
    }

    companion object {
        private val APPLICATION_OCTET_STREAM = "application/octet-stream".toMediaType()
        private const val CONTENT_TYPE_HEADER = "Content-Type"
        private const val FILE_PART_MULTIPART = "file"
        private val log = LoggerFactory.getLogger(UploadFile::class.java)
        private fun addFormParamsWithKeyFirst(
            keyValue: FormField,
            formParams: List<FormField>,
            builder: MultipartBody.Builder
        ) {
            builder.addFormDataPart(keyValue.key, keyValue.value)
            formParams
                .filter { it.key != keyValue.key }
                .forEach { builder.addFormDataPart(it.key, it.value) }
        }
    }

    override fun operationType(): PNOperationType = PNOperationType.FileOperation
}
