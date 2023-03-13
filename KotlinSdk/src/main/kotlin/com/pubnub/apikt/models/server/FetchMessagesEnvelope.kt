package com.pubnub.apikt.models.server

import com.pubnub.apikt.models.consumer.history.PNFetchMessageItem

data class FetchMessagesEnvelope(
    val channels: Map<String, List<PNFetchMessageItem>>,
    val more: FetchMessagesPage?
)

data class FetchMessagesPage(
    val start: Long? = null,
    val end: Long? = null,
    val max: Int? = null
)
