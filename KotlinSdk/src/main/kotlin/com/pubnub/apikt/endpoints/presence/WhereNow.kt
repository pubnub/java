package com.pubnub.apikt.endpoints.presence

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.presence.PNWhereNowResult
import com.pubnub.apikt.models.server.Envelope
import com.pubnub.apikt.models.server.presence.WhereNowPayload
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.whereNow]
 */
class WhereNow internal constructor(
    pubnub: PubNub,
    val uuid: String = pubnub.configuration.userId.value
) : Endpoint<Envelope<WhereNowPayload>, PNWhereNowResult>(pubnub) {

    override fun doWork(queryParams: HashMap<String, String>): Call<Envelope<WhereNowPayload>> {
        return pubnub.retrofitManager.presenceService.whereNow(
            pubnub.configuration.subscribeKey,
            uuid,
            queryParams
        )
    }

    override fun createResponse(input: Response<Envelope<WhereNowPayload>>): PNWhereNowResult =
        PNWhereNowResult(channels = input.body()!!.payload!!.channels)

    override fun operationType() = PNOperationType.PNWhereNowOperation
}
