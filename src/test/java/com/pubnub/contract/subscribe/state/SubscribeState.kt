package com.pubnub.contract.subscribe.state

import com.pubnub.api.models.consumer.pubsub.MessageResult


class SubscribeState {
    var messages: List<MessageResult> = mutableListOf()
}