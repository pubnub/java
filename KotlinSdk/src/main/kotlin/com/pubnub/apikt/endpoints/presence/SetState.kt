package com.pubnub.apikt.endpoints.presence

import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.pubnub.apikt.*
import com.pubnub.apikt.builder.StateOperation
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.presence.PNSetStateResult
import com.pubnub.apikt.models.server.Envelope
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.setPresenceState]
 */
class SetState internal constructor(
    pubnub: PubNub,
    val channels: List<String>,
    val channelGroups: List<String>,
    val state: Any,
    val uuid: String = pubnub.configuration.userId.value
) : Endpoint<Envelope<JsonElement>, PNSetStateResult>(pubnub) {

    override fun getAffectedChannels() = channels
    override fun getAffectedChannelGroups() = channelGroups

    override fun validateParams() {
        super.validateParams()
        if (channels.isEmpty() && channelGroups.isEmpty())
            throw PubNubException(PubNubError.CHANNEL_AND_GROUP_MISSING)
    }

    override fun doWork(queryParams: HashMap<String, String>): Call<Envelope<JsonElement>> {
        if (uuid == pubnub.configuration.userId.value) {
            pubnub.subscriptionManager.adaptStateBuilder(
                StateOperation(
                    state = state,
                    channels = channels,
                    channelGroups = channelGroups
                )
            )
        }

        addQueryParams(queryParams)

        return pubnub.retrofitManager.presenceService.setState(
            pubnub.configuration.subscribeKey,
            channels.toCsv(),
            uuid,
            queryParams
        )
    }

    override fun createResponse(input: Response<Envelope<JsonElement>>): PNSetStateResult {
        if (input.body()!!.payload!! is JsonNull) {
            throw PubNubException(PubNubError.PARSING_ERROR)
        }
        return PNSetStateResult(state = input.body()!!.payload!!)
    }

    override fun operationType() = PNOperationType.PNSetStateOperation

    private fun addQueryParams(queryParams: MutableMap<String, String>) {
        if (channelGroups.isNotEmpty()) queryParams["channel-group"] = channelGroups.toCsv()
        queryParams["state"] = pubnub.mapper.toJson(state)
    }
}
