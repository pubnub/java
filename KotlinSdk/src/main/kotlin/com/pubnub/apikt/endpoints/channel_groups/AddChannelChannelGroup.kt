package com.pubnub.apikt.endpoints.channel_groups

import com.pubnub.apikt.*
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.channel_group.PNChannelGroupsAddChannelResult
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.addChannelsToChannelGroup]
 */
class AddChannelChannelGroup internal constructor(
    pubnub: PubNub,
    val channelGroup: String,
    val channels: List<String>
) : Endpoint<Void, PNChannelGroupsAddChannelResult>(pubnub) {

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
            .addChannelChannelGroup(
                pubnub.configuration.subscribeKey,
                channelGroup,
                queryParams
            )
    }

    override fun createResponse(input: Response<Void>): PNChannelGroupsAddChannelResult =
        PNChannelGroupsAddChannelResult()

    override fun operationType() = PNOperationType.PNAddChannelsToGroupOperation

    private fun addQueryParams(queryParams: MutableMap<String, String>) {
        if (channels.isNotEmpty()) {
            queryParams["add"] = channels.toCsv()
        }
    }
}
