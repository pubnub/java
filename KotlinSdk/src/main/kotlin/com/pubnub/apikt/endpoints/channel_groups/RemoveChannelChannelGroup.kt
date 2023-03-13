package com.pubnub.apikt.endpoints.channel_groups

import com.pubnub.apikt.*
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.channel_group.PNChannelGroupsRemoveChannelResult
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.removeChannelsFromChannelGroup]
 */
class RemoveChannelChannelGroup internal constructor(
    pubnub: PubNub,
    val channelGroup: String,
    val channels: List<String>
) : Endpoint<Void, PNChannelGroupsRemoveChannelResult>(pubnub) {

    override fun getAffectedChannels() = channels

    override fun getAffectedChannelGroups() = listOf(channelGroup)

    override fun validateParams() {
        super.validateParams()
        if (channelGroup.isBlank()) throw PubNubException(PubNubError.GROUP_MISSING)
        if (channels.isEmpty()) throw PubNubException(PubNubError.CHANNEL_MISSING)
    }

    override fun doWork(queryParams: HashMap<String, String>): Call<Void> {

        addQueryParams(queryParams)

        return pubnub.retrofitManager.channelGroupService
            .removeChannel(
                pubnub.configuration.subscribeKey,
                channelGroup,
                queryParams
            )
    }

    override fun createResponse(input: Response<Void>): PNChannelGroupsRemoveChannelResult =
        PNChannelGroupsRemoveChannelResult()

    override fun operationType() = PNOperationType.PNRemoveChannelsFromGroupOperation

    private fun addQueryParams(queryParams: MutableMap<String, String>) {
        if (channels.isNotEmpty()) {
            queryParams["remove"] = channels.toCsv()
        }
    }
}
