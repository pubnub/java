package com.pubnub.apikt.endpoints.objects.member

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.objects.internal.CollectionQueryParameters
import com.pubnub.apikt.endpoints.objects.internal.IncludeQueryParam
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.objects.member.PNMember
import com.pubnub.apikt.models.consumer.objects.member.PNMemberArrayResult
import com.pubnub.apikt.models.server.objects_api.EntityArrayEnvelope
import com.pubnub.extension.toPNMemberArrayResult
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.getChannelMembers]
 */
class GetChannelMembers internal constructor(
    pubnub: PubNub,
    private val channel: String,
    private val collectionQueryParameters: CollectionQueryParameters,
    private val includeQueryParam: IncludeQueryParam
) : Endpoint<EntityArrayEnvelope<PNMember>, PNMemberArrayResult>(pubnub) {

    override fun doWork(queryParams: HashMap<String, String>): Call<EntityArrayEnvelope<PNMember>> {
        val params = queryParams + collectionQueryParameters.createCollectionQueryParams() + includeQueryParam.createIncludeQueryParams()

        return pubnub.retrofitManager.objectsService.getChannelMembers(
            channel = channel,
            subKey = pubnub.configuration.subscribeKey,
            options = params
        )
    }

    override fun createResponse(input: Response<EntityArrayEnvelope<PNMember>>): PNMemberArrayResult? =
        input.toPNMemberArrayResult()

    override fun operationType(): PNOperationType {
        return PNOperationType.ObjectsOperation()
    }
}
