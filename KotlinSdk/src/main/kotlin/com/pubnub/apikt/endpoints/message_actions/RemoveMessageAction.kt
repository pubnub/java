package com.pubnub.apikt.endpoints.message_actions

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.message_actions.PNRemoveMessageActionResult
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * @see [PubNub.removeMessageAction]
 */
class RemoveMessageAction internal constructor(
    pubnub: PubNub,
    val channel: String,
    val messageTimetoken: Long,
    val actionTimetoken: Long
) : Endpoint<Void, PNRemoveMessageActionResult>(pubnub) {

    override fun validateParams() {
        super.validateParams()
        if (channel.isBlank()) throw PubNubException(PubNubError.CHANNEL_MISSING)
    }

    override fun getAffectedChannels() = listOf(channel)

    override fun doWork(queryParams: HashMap<String, String>): Call<Void> {
        return pubnub.retrofitManager.messageActionService
            .deleteMessageAction(
                subKey = pubnub.configuration.subscribeKey,
                channel = channel,
                messageTimetoken = messageTimetoken.toString().lowercase(Locale.getDefault()),
                actionTimetoken = actionTimetoken.toString().lowercase(Locale.getDefault()),
                options = queryParams
            )
    }

    override fun createResponse(input: Response<Void>): PNRemoveMessageActionResult? {
        return PNRemoveMessageActionResult()
    }

    override fun operationType() = PNOperationType.PNDeleteMessageAction
}
