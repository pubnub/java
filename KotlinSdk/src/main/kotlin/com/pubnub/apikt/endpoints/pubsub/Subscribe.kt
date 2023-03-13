package com.pubnub.apikt.endpoints.pubsub

import com.pubnub.apikt.*
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.server.SubscribeEnvelope
import retrofit2.Call
import retrofit2.Response

class Subscribe internal constructor(pubnub: PubNub) : Endpoint<SubscribeEnvelope, SubscribeEnvelope>(pubnub) {

    var channels = emptyList<String>()
    var channelGroups = emptyList<String>()
    var timetoken: Long? = null
    var region: String? = null
    var state: Any? = null
    var filterExpression: String? = null

    override fun validateParams() {
        super.validateParams()
        if (channels.isEmpty() && channelGroups.isEmpty()) {
            throw PubNubException(PubNubError.CHANNEL_AND_GROUP_MISSING)
        }
    }

    override fun getAffectedChannels() = channels

    override fun getAffectedChannelGroups() = channelGroups

    override fun doWork(queryParams: HashMap<String, String>): Call<SubscribeEnvelope> {
        if (channelGroups.isNotEmpty()) {
            queryParams["channel-group"] = channelGroups.joinToString(",")
        }

        if (!filterExpression.isNullOrBlank()) {
            queryParams["filter-expr"] = filterExpression!!
        }

        timetoken?.let {
            queryParams["tt"] = it.toString()
        }

        region?.let {
            queryParams["tr"] = it
        }

        queryParams["heartbeat"] = pubnub.configuration.presenceTimeout.toString()

        state?.let {
            queryParams["state"] = pubnub.mapper.toJson(it)
        }

        return pubnub.retrofitManager.subscribeService.subscribe(
            pubnub.configuration.subscribeKey,
            channels.toCsv(),
            queryParams
        )
    }

    override fun createResponse(input: Response<SubscribeEnvelope>): SubscribeEnvelope? {
        return input.body()!!
    }

    override fun operationType() = PNOperationType.PNSubscribeOperation
}
