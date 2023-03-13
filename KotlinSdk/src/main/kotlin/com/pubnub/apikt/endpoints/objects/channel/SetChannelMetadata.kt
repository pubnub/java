package com.pubnub.apikt.endpoints.objects.channel

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.objects.internal.IncludeQueryParam
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadata
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadataResult
import com.pubnub.apikt.models.server.objects_api.ChannelMetadataInput
import com.pubnub.apikt.models.server.objects_api.EntityEnvelope
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.setChannelMetadata]
 */
class SetChannelMetadata internal constructor(
    pubnub: PubNub,
    private val name: String?,
    private val description: String?,
    private val custom: Any?,
    private val channel: String,
    private val includeQueryParam: IncludeQueryParam,
    private val type: String?,
    private val status: String?
) : Endpoint<EntityEnvelope<PNChannelMetadata>, PNChannelMetadataResult>(pubnub) {

    override fun doWork(queryParams: HashMap<String, String>): Call<EntityEnvelope<PNChannelMetadata>> {
        val params = queryParams + includeQueryParam.createIncludeQueryParams()
        return pubnub.retrofitManager.objectsService.setChannelMetadata(
            subKey = pubnub.configuration.subscribeKey,
            body = ChannelMetadataInput(
                name = name,
                custom = custom,
                description = description,
                status = status,
                type = type
            ),
            channel = channel,
            options = params
        )
    }

    override fun createResponse(input: Response<EntityEnvelope<PNChannelMetadata>>): PNChannelMetadataResult? {
        return input.body()?.let {
            PNChannelMetadataResult(
                status = it.status,
                data = it.data
            )
        }
    }

    override fun operationType(): PNOperationType {
        return PNOperationType.PNSetChannelMetadataOperation
    }
}
