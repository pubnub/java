package com.pubnub.apikt.endpoints.objects.uuid

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.objects.internal.IncludeQueryParam
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadataResult
import com.pubnub.apikt.models.server.objects_api.EntityEnvelope
import com.pubnub.apikt.models.server.objects_api.UUIDMetadataInput
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.setUUIDMetadata]
 */
class SetUUIDMetadata internal constructor(
    pubnub: PubNub,
    private val uuid: String?,
    private val name: String?,
    private val externalId: String?,
    private val profileUrl: String?,
    private val email: String?,
    private val custom: Any?,
    private val withInclude: IncludeQueryParam,
    private val type: String?,
    private val status: String?
) : Endpoint<EntityEnvelope<PNUUIDMetadata>, PNUUIDMetadataResult>(pubnub) {

    override fun doWork(queryParams: HashMap<String, String>): Call<EntityEnvelope<PNUUIDMetadata>> {
        val params = queryParams + withInclude.createIncludeQueryParams()
        return pubnub.retrofitManager.objectsService.setUUIDMetadata(
            subKey = pubnub.configuration.subscribeKey,
            body = UUIDMetadataInput(
                name = name,
                custom = custom,
                email = email,
                externalId = externalId,
                profileUrl = profileUrl,
                type = type,
                status = status
            ),
            uuid = uuid ?: pubnub.configuration.userId.value,
            options = params
        )
    }

    override fun createResponse(input: Response<EntityEnvelope<PNUUIDMetadata>>): PNUUIDMetadataResult? {
        return input.body()?.let {
            PNUUIDMetadataResult(
                status = it.status,
                data = it.data
            )
        }
    }

    override fun operationType(): PNOperationType {
        return PNOperationType.PNSetUUIDMetadataOperation
    }
}
