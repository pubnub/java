package com.pubnub.apikt.endpoints.files

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.endpoints.remoteaction.ExtendedRemoteAction
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.server.files.FileUploadRequestDetails
import com.pubnub.apikt.models.server.files.FormField
import com.pubnub.apikt.models.server.files.GenerateUploadUrlPayload
import com.pubnub.apikt.models.server.files.GeneratedUploadUrlResponse
import retrofit2.Call
import retrofit2.Response

internal class GenerateUploadUrl(
    private val channel: String,
    private val fileName: String,
    pubNub: PubNub
) : Endpoint<GeneratedUploadUrlResponse, FileUploadRequestDetails>(pubNub) {

    @Throws(PubNubException::class)
    override fun validateParams() {
        if (channel.isEmpty()) {
            throw PubNubException(PubNubError.CHANNEL_MISSING)
        }
    }

    @Throws(PubNubException::class)
    override fun createResponse(input: Response<GeneratedUploadUrlResponse>): FileUploadRequestDetails {
        if (input.body() == null) {
            throw PubNubException(PubNubError.INTERNAL_ERROR).copy(errorMessage = "Empty body, but GeneratedUploadUrlResponse expected")
        }
        val response = input.body()!!
        val keyFormField = getKeyFormField(response)
        return FileUploadRequestDetails(
            response.status,
            response.data,
            response.fileUploadRequest.url,
            response.fileUploadRequest.method,
            response.fileUploadRequest.expirationDate,
            keyFormField,
            response.fileUploadRequest.formFields
        )
    }

    @Throws(PubNubException::class)
    private fun getKeyFormField(response: GeneratedUploadUrlResponse): FormField {
        val formFields: List<FormField> = response.fileUploadRequest.formFields
        return formFields.find { it.key == "key" } ?: throw PubNubException(PubNubError.INTERNAL_ERROR).copy(
            errorMessage = "Couldn't find `key` form field in GeneratedUploadUrlResponse"
        )
    }

    override fun doWork(queryParams: HashMap<String, String>): Call<GeneratedUploadUrlResponse> {
        return pubnub.retrofitManager.filesService.generateUploadUrl(
            pubnub.configuration.subscribeKey,
            channel,
            GenerateUploadUrlPayload(fileName),
            queryParams
        )
    }

    override fun getAffectedChannels() = listOf(channel)
    override fun getAffectedChannelGroups(): List<String> = listOf()
    override fun operationType(): PNOperationType = PNOperationType.FileOperation
    override fun isAuthRequired(): Boolean = true
    override fun isSubKeyRequired(): Boolean = true
    override fun isPubKeyRequired(): Boolean = false

    internal class Factory(private val pubNub: PubNub) {
        fun create(channel: String, fileName: String): ExtendedRemoteAction<FileUploadRequestDetails> {
            return GenerateUploadUrl(channel, fileName, pubNub)
        }
    }
}
