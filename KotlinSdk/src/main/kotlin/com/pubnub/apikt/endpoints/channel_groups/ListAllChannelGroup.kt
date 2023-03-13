package com.pubnub.apikt.endpoints.channel_groups

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.channel_group.PNChannelGroupsListAllResult
import com.pubnub.apikt.models.server.Envelope
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.listAllChannelGroups]
 */
class ListAllChannelGroup internal constructor(pubnub: PubNub) :
    Endpoint<Envelope<Map<String, Any>>, PNChannelGroupsListAllResult>(pubnub) {

    override fun doWork(queryParams: HashMap<String, String>): Call<Envelope<Map<String, Any>>> {
        return pubnub.retrofitManager.channelGroupService
            .listAllChannelGroup(
                pubnub.configuration.subscribeKey,
                queryParams
            )
    }

    @Suppress("UNCHECKED_CAST")
    override fun createResponse(input: Response<Envelope<Map<String, Any>>>): PNChannelGroupsListAllResult =
        PNChannelGroupsListAllResult(
            groups = input.body()!!.payload!!["groups"] as List<String>
        )

    override fun operationType() = PNOperationType.PNChannelGroupsOperation
}
