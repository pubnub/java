package com.pubnub.contract.subscribe.state

import com.pubnub.api.models.consumer.pubsub.MessageResult
import java.util.concurrent.CountDownLatch


class SubscribeState {
    var messages: List<MessageResult> = mutableListOf()
    var messageDeliveredToListener = CountDownLatch(0)
}