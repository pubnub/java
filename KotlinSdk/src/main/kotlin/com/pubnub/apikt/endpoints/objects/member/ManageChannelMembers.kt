package com.pubnub.apikt.endpoints.objects.member

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.objects.internal.CollectionQueryParameters
import com.pubnub.apikt.endpoints.objects.internal.IncludeQueryParam
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.objects.member.MemberInput
import com.pubnub.apikt.models.consumer.objects.member.PNMember
import com.pubnub.apikt.models.consumer.objects.member.PNMemberArrayResult
import com.pubnub.apikt.models.server.objects_api.ChangeMemberInput
import com.pubnub.apikt.models.server.objects_api.EntityArrayEnvelope
import com.pubnub.apikt.models.server.objects_api.ServerMemberInput
import com.pubnub.apikt.models.server.objects_api.UUIDId
import com.pubnub.extension.toPNMemberArrayResult
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.manageChannelMembers]
 */
class ManageChannelMembers(
    pubnub: PubNub,
    private val uuidsToSet: Collection<MemberInput>,
    private val uuidsToRemove: Collection<String>,
    private val channel: String,
    private val collectionQueryParameters: CollectionQueryParameters,
    private val includeQueryParam: IncludeQueryParam
) : Endpoint<EntityArrayEnvelope<PNMember>, PNMemberArrayResult>(pubnub) {
    override fun doWork(queryParams: HashMap<String, String>): Call<EntityArrayEnvelope<PNMember>> {
        val params =
            queryParams + collectionQueryParameters.createCollectionQueryParams() + includeQueryParam.createIncludeQueryParams()

        return pubnub.retrofitManager.objectsService.patchChannelMembers(
            channel = channel,
            subKey = pubnub.configuration.subscribeKey,
            options = params,
            body = ChangeMemberInput(
                delete = uuidsToRemove.map { ServerMemberInput(UUIDId(id = it)) },
                set = uuidsToSet.map { ServerMemberInput(uuid = UUIDId(id = it.uuid), custom = it.custom, status = it.status) }
            )
        )
    }

    override fun createResponse(input: Response<EntityArrayEnvelope<PNMember>>): PNMemberArrayResult? =
        input.toPNMemberArrayResult()

    override fun operationType(): PNOperationType = PNOperationType.ObjectsOperation()
}
