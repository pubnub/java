package com.pubnub.apikt.workers

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.managers.DuplicationManager
import com.pubnub.apikt.managers.ListenerManager
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.pubsub.PNMessageResult
import com.pubnub.apikt.models.consumer.pubsub.PNPresenceEventResult
import com.pubnub.apikt.models.consumer.pubsub.PNSignalResult
import com.pubnub.apikt.models.consumer.pubsub.files.PNFileEventResult
import com.pubnub.apikt.models.consumer.pubsub.message_actions.PNMessageActionResult
import com.pubnub.apikt.models.consumer.pubsub.objects.PNObjectEventResult
import com.pubnub.apikt.models.server.SubscribeMessage
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue

internal class SubscribeMessageWorker(
    pubnub: PubNub,
    val listenerManager: ListenerManager,
    val queue: LinkedBlockingQueue<SubscribeMessage>,
    duplicationManager: DuplicationManager,
    private val messageProcessor: SubscribeMessageProcessor = SubscribeMessageProcessor(pubnub, duplicationManager)
) : Runnable {

    private val log = LoggerFactory.getLogger("SubscribeMessageWorker")

    override fun run() {
        takeMessage()
    }

    private fun takeMessage() {
        while (!Thread.interrupted()) {
            try {
                when (val event = messageProcessor.processIncomingPayload(queue.take())) {
                    is PNMessageResult -> listenerManager.announce(event)
                    is PNPresenceEventResult -> listenerManager.announce(event)
                    is PNSignalResult -> listenerManager.announce(event)
                    is PNMessageActionResult -> listenerManager.announce(event)
                    is PNObjectEventResult -> listenerManager.announce(event)
                    is PNFileEventResult -> listenerManager.announce(event)
                }
            } catch (e: PubNubException) {
                val pnStatus = PNStatus(
                    PNStatusCategory.PNMalformedResponseCategory, true, PNOperationType.PNSubscribeOperation, e
                )
                listenerManager.announce(pnStatus)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                log.trace("take message interrupted!", e)
            }
        }
    }
}
