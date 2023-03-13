package com.pubnub.apikt.endpoints.message_actions

import com.google.gson.JsonObject
import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.message_actions.PNAddMessageActionResult
import com.pubnub.apikt.models.consumer.message_actions.PNMessageAction
import com.pubnub.apikt.models.server.objects_api.EntityEnvelope
import retrofit2.Call
import retrofit2.Response
import java.util.*

/**
 * @see [PubNub.addMessageAction]
 */
class AddMessageAction internal constructor(
    pubnub: PubNub,
    val channel: String,
    val messageAction: PNMessageAction
) : Endpoint<EntityEnvelope<PNMessageAction>, PNAddMessageActionResult>(pubnub) {

    override fun validateParams() {
        super.validateParams()
        if (channel.isBlank()) throw PubNubException(PubNubError.CHANNEL_MISSING)
        if (messageAction.type.isBlank()) throw PubNubException(PubNubError.MESSAGE_ACTION_TYPE_MISSING)
        if (messageAction.value.isBlank()) throw PubNubException(PubNubError.MESSAGE_ACTION_VALUE_MISSING)
    }

    override fun getAffectedChannels() = listOf(channel)

    override fun doWork(queryParams: HashMap<String, String>): Call<EntityEnvelope<PNMessageAction>> {
        val body = JsonObject().apply {
            addProperty("type", messageAction.type)
            addProperty("value", messageAction.value)
        }

        return pubnub.retrofitManager.messageActionService
            .addMessageAction(
                subKey = pubnub.configuration.subscribeKey,
                channel = channel,
                messageTimetoken = messageAction.messageTimetoken.toString().lowercase(Locale.getDefault()),
                body = body,
                options = queryParams
            )
    }

    override fun createResponse(input: Response<EntityEnvelope<PNMessageAction>>): PNAddMessageActionResult =
        PNAddMessageActionResult(
            action = input.body()!!.data!!
        )

    override fun operationType() = PNOperationType.PNAddMessageAction
}
