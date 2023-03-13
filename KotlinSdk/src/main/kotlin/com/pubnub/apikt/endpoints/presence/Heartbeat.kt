package com.pubnub.apikt.endpoints.presence

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import retrofit2.Call
import retrofit2.Response

class Heartbeat internal constructor(
    pubnub: PubNub,
    val channels: List<String> = listOf(),
    val channelGroups: List<String> = listOf(),
    val state: Any? = null
) : Endpoint<Void, Boolean>(pubnub) {

    override fun getAffectedChannels() = channels
    override fun getAffectedChannelGroups() = channelGroups

    override fun validateParams() {
        super.validateParams()
        if (channels.isEmpty() && channelGroups.isEmpty()) {
            throw PubNubException(PubNubError.CHANNEL_AND_GROUP_MISSING)
        }
    }

    override fun doWork(queryParams: HashMap<String, String>): Call<Void> {
        queryParams["heartbeat"] = pubnub.configuration.presenceTimeout.toString()

        if (channelGroups.isNotEmpty()) {
            queryParams["channel-group"] = channelGroups.joinToString(",")
        }

        val channelsCsv =
            if (channels.isNotEmpty())
                channels.joinToString(",")
            else
                ","

        state?.let {
            queryParams["state"] = pubnub.mapper.toJson(it)
        }

        return pubnub.retrofitManager.presenceService.heartbeat(
            pubnub.configuration.subscribeKey,
            channelsCsv,
            queryParams
        )
    }

    override fun createResponse(input: Response<Void>): Boolean? {
        return true
    }

    override fun operationType() = PNOperationType.PNHeartbeatOperation
}
