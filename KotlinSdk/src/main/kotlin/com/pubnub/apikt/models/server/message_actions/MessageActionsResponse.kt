package com.pubnub.apikt.models.server.message_actions

import com.pubnub.apikt.models.consumer.PNBoundedPage
import com.pubnub.apikt.models.consumer.message_actions.PNMessageAction

data class MessageActionsResponse(
    val status: Int,
    val data: List<PNMessageAction> = listOf(),
    val more: PNBoundedPage
)
