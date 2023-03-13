package com.pubnub.apikt.endpoints.objects.membership

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.endpoints.objects.internal.CollectionQueryParameters
import com.pubnub.apikt.endpoints.objects.internal.IncludeQueryParam
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.objects.membership.ChannelMembershipInput
import com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembership
import com.pubnub.apikt.models.consumer.objects.membership.PNChannelMembershipArrayResult
import com.pubnub.apikt.models.server.objects_api.ChangeMembershipInput
import com.pubnub.apikt.models.server.objects_api.ChannelId
import com.pubnub.apikt.models.server.objects_api.EntityArrayEnvelope
import com.pubnub.apikt.models.server.objects_api.ServerMembershipInput
import com.pubnub.extension.toPNChannelMembershipArrayResult
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.manageMemberships]
 */
class ManageMemberships internal constructor(
    pubnub: PubNub,
    private val channelsToSet: Collection<ChannelMembershipInput>,
    private val channelsToRemove: Collection<String>,
    private val uuid: String,
    private val collectionQueryParameters: CollectionQueryParameters,
    private val includeQueryParam: IncludeQueryParam
) : Endpoint<EntityArrayEnvelope<PNChannelMembership>, PNChannelMembershipArrayResult>(pubnub) {

    override fun doWork(queryParams: HashMap<String, String>): Call<EntityArrayEnvelope<PNChannelMembership>> {
        val params = queryParams + collectionQueryParameters.createCollectionQueryParams() +
            includeQueryParam.createIncludeQueryParams()
        return pubnub.retrofitManager.objectsService.patchMemberships(
            uuid = uuid,
            subKey = pubnub.configuration.subscribeKey,
            options = params,
            body = ChangeMembershipInput(
                set = channelsToSet.map {
                    ServerMembershipInput(
                        channel = ChannelId(id = it.channel),
                        custom = it.custom,
                        status = it.status
                    )
                },
                delete = channelsToRemove.map { ServerMembershipInput(channel = ChannelId(id = it)) }
            )
        )
    }

    override fun createResponse(input: Response<EntityArrayEnvelope<PNChannelMembership>>): PNChannelMembershipArrayResult? =
        input.toPNChannelMembershipArrayResult()

    override fun operationType(): PNOperationType = PNOperationType.ObjectsOperation()
}
