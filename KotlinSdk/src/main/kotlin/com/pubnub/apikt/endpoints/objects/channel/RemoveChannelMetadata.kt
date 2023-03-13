package com.pubnub.apikt.endpoints.objects.channel

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.objects.PNRemoveMetadataResult
import com.pubnub.apikt.models.server.objects_api.EntityEnvelope
import retrofit2.Call
import retrofit2.Response

class RemoveChannelMetadata(
    pubnub: PubNub,
    private val channel: String
) : Endpoint<EntityEnvelope<Any?>, PNRemoveMetadataResult>(pubnub) {

    override fun doWork(queryParams: HashMap<String, String>): Call<EntityEnvelope<Any?>> {
        return pubnub.retrofitManager.objectsService.deleteChannelMetadata(
            subKey = pubnub.configuration.subscribeKey,
            channel = channel
        )
    }

    override fun createResponse(input: Response<EntityEnvelope<Any?>>): PNRemoveMetadataResult? {
        return input.body()?.let { PNRemoveMetadataResult(it.status) }
    }

    override fun operationType(): PNOperationType {
        return PNOperationType.PNRemoveChannelMetadataOperation
    }
}
