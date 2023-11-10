package com.pubnub.api.workers;

import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.managers.ListenerManager;
import com.pubnub.api.models.consumer.PNErrorData;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNEvent;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import com.pubnub.api.models.server.SubscribeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;


@Slf4j
@AllArgsConstructor
public class SubscribeMessageWorker implements Runnable {

    private final ListenerManager listenerManager;
    private final LinkedBlockingQueue<SubscribeMessage> queue;
    private final SubscribeMessageProcessor subscribeMessageProcessor;

    @Override
    public void run() {
        takeMessage();
    }


    private void takeMessage() {
        while (!Thread.interrupted()) {
            try {
                PNEvent event = subscribeMessageProcessor.processIncomingPayload(this.queue.take());
                if (event instanceof PNMessageResult) {
                    listenerManager.announce((PNMessageResult) event);
                } else if (event instanceof PNPresenceEventResult) {
                    listenerManager.announce((PNPresenceEventResult) event);
                } else if (event instanceof PNSignalResult) {
                    listenerManager.announce((PNSignalResult) event);
                } else if (event instanceof PNMessageActionResult) {
                    listenerManager.announce((PNMessageActionResult) event);
                } else if (event instanceof PNUUIDMetadataResult) {
                    listenerManager.announce((PNUUIDMetadataResult) event);
                } else if (event instanceof PNChannelMetadataResult) {
                    listenerManager.announce((PNChannelMetadataResult) event);
                } else if (event instanceof PNMembershipResult) {
                    listenerManager.announce((PNMembershipResult) event);
                } else if (event instanceof PNFileEventResult) {
                    listenerManager.announce((PNFileEventResult) event);
                }
            } catch (PubNubException e) {
                PNStatus pnStatus = PNStatus.builder().error(true)
                        .errorData(new PNErrorData(e.getMessage(), e))
                        .operation(PNOperationType.PNSubscribeOperation)
                        .category(PNStatusCategory.PNDecryptionErrorCategory)
                        .build();

                listenerManager.announce(pnStatus);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.trace("take message interrupted", e);
            } catch (Exception e) { // don't crash the thread on malformed messages
                log.warn("Unexpected message processing error", e);
            }
        }
    }

}
