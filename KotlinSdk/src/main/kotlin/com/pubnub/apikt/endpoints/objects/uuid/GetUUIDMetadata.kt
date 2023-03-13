package com.pubnub.apikt.endpoints.objects.uuid

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.objects.internal.IncludeQueryParam
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadataResult
import com.pubnub.apikt.models.server.objects_api.EntityEnvelope
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.getUUIDMetadata]
 */
class GetUUIDMetadata internal constructor(
    pubnub: PubNub,
    val uuid: String,
    private val includeQueryParam: IncludeQueryParam
) : Endpoint<EntityEnvelope<PNUUIDMetadata>, PNUUIDMetadataResult>(pubnub) {

    override fun doWork(queryParams: HashMap<String, String>): Call<EntityEnvelope<PNUUIDMetadata>> {
        val params = queryParams + includeQueryParam.createIncludeQueryParams()
        return pubnub.retrofitManager.objectsService.getUUIDMetadata(
            subKey = pubnub.configuration.subscribeKey,
            uuid = uuid,
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
        return PNOperationType.PNGetUUIDMetadataOperation
    }
}
