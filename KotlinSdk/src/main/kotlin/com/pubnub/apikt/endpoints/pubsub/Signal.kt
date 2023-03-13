package com.pubnub.apikt.endpoints.pubsub

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNPublishResult
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.signal]
 */
class Signal internal constructor(
    pubnub: PubNub,
    val channel: String,
    val message: Any
) : Endpoint<List<Any>, PNPublishResult>(pubnub) {

    override fun validateParams() {
        super.validateParams()
        if (channel.isBlank()) throw PubNubException(PubNubError.CHANNEL_MISSING)
    }

    override fun getAffectedChannels() = listOf(channel)

    override fun doWork(queryParams: HashMap<String, String>): Call<List<Any>> {
        return pubnub.retrofitManager.signalService.signal(
            pubKey = pubnub.configuration.publishKey,
            subKey = pubnub.configuration.subscribeKey,
            channel = channel,
            message = pubnub.mapper.toJson(message),
            options = queryParams
        )
    }

    override fun createResponse(input: Response<List<Any>>): PNPublishResult =
        PNPublishResult(
            timetoken = input.body()!![2].toString().toLong()
        )

    override fun operationType() = PNOperationType.PNSignalOperation

    override fun isPubKeyRequired() = true
}
