package com.pubnub.apikt.services

import com.pubnub.apikt.models.consumer.message_actions.PNGetMessageActionsResult
import com.pubnub.apikt.models.consumer.message_actions.PNMessageAction
import com.pubnub.apikt.models.server.objects_api.EntityEnvelope
import retrofit2.Call
import retrofit2.http.*

internal interface MessageActionService {

    @POST("v1/message-actions/{subKey}/channel/{channel}/message/{messageTimetoken}")
    @Headers("Content-Type: application/json; charset=UTF-8")
    fun addMessageAction(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @Path("messageTimetoken") messageTimetoken: String,
        @Body body: Any,
        @QueryMap options: Map<String, String>
    ): Call<EntityEnvelope<PNMessageAction>>

    @GET("v1/message-actions/{subKey}/channel/{channel}")
    fun getMessageActions(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @QueryMap options: Map<String, String>
    ): Call<PNGetMessageActionsResult>

    @DELETE("v1/message-actions/{subKey}/channel/{channel}/message/{messageTimetoken}/action/{actionTimetoken}")
    fun deleteMessageAction(
        @Path("subKey") subKey: String,
        @Path("channel") channel: String,
        @Path("messageTimetoken") messageTimetoken: String,
        @Path("actionTimetoken") actionTimetoken: String,
        @QueryMap options: Map<String, String>
    ): Call<Void>
}
