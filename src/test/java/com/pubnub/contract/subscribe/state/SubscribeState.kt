package com.pubnub.contract.subscribe.state

import com.pubnub.api.models.consumer.pubsub.MessageResult
import java.util.concurrent.CopyOnWriteArrayList


class SubscribeState {
    val messages: CopyOnWriteArrayList<MessageResult> = CopyOnWriteArrayList()
}