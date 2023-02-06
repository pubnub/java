package com.pubnub.api.integration;

import com.pubnub.api.MessageType;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.integration.util.BaseIntegrationTest;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.files.PNDownloadFileResult;
import com.pubnub.api.models.consumer.files.PNFileUploadResult;
import com.pubnub.api.models.consumer.files.PNListFilesResult;
import com.pubnub.api.models.consumer.files.PNUploadedFile;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static com.pubnub.api.integration.util.Utils.random;
import static com.pubnub.api.integration.util.Utils.randomChannel;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FilesIntegrationTests extends BaseIntegrationTest {

    @Test
    public void uploadListDownloadDeleteWithCipher() throws PubNubException, InterruptedException, IOException {
        doItAllFilesTest(true);
    }

    @Test
    public void canPublishFileMessage() throws InterruptedException, PubNubException {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch fileEventReceived = new CountDownLatch(1);
        String channel = randomChannel();
        String message = "This is message";
        String meta = "This is meta";
        String fileName = "fileName" + channel + ".txt";
        String fileId = "fileId_" + random();
        MessageType expectedMessageType = new MessageType("myMessageType");
        SpaceId expectedSpaceId = new SpaceId("mySpace");

        pubNub.addListener(new LimitedListener() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {
                if (pnStatus.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    connectedLatch.countDown();
                }
            }

            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {
                if (pnFileEventResult.getFile().getName().equals(fileName)) {
                    assertEquals(expectedMessageType, pnFileEventResult.getMessageType());
                    assertEquals(expectedSpaceId, pnFileEventResult.getSpaceId());
                    fileEventReceived.countDown();
                }
            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList(channel))
                .execute();

        assertTrue(connectedLatch.await(10, TimeUnit.SECONDS));

        pubNub.publishFileMessage()
                .channel(channel)
                .fileName(fileName)
                .fileId(fileId)
                .message(message)
                .meta(meta)
                .messageType(expectedMessageType)
                .spaceId(expectedSpaceId)
                .sync();

        assertTrue(fileEventReceived.await(5, TimeUnit.SECONDS));

        pubNub.deleteFile()
                .channel(channel)
                .fileName(fileName)
                .fileId(fileId)
                .sync();
    }

    @Test
    public void uploadListDownloadDeleteWithoutCipher() throws PubNubException, InterruptedException, IOException {
        doItAllFilesTest(false);
    }

    public void doItAllFilesTest(boolean withCipher) throws PubNubException, InterruptedException, IOException {
        if (withCipher) {
            pubNub.getConfiguration().setCipherKey("enigma");
        } else {
            pubNub.getConfiguration().setCipherKey(null);
        }
        String channel = randomChannel();
        String content = "This is content";
        String message = "This is message";
        String meta = "This is meta";
        String fileName = "fileName" + channel + ".txt";
        MessageType expectedMessageType = new MessageType("myMessageType");
        SpaceId expectedSpaceId = new SpaceId("mySpace");
        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch fileEventReceived = new CountDownLatch(1);

        pubNub.addListener(new LimitedListener() {
            @Override
            public void status(@NotNull PubNub pubnub, @NotNull PNStatus pnStatus) {
                if (pnStatus.getCategory() == PNStatusCategory.PNConnectedCategory) {
                    connectedLatch.countDown();
                }
            }

            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {
                if (pnFileEventResult.getFile().getName().equals(fileName)) {
                    assertEquals(expectedMessageType, pnFileEventResult.getMessageType());
                    assertEquals(expectedSpaceId, pnFileEventResult.getSpaceId());
                    fileEventReceived.countDown();
                }
            }
        });

        pubNub.subscribe()
                .channels(Collections.singletonList(channel))
                .execute();

        PNFileUploadResult sendResult;
        assertTrue(connectedLatch.await(10, TimeUnit.SECONDS));
        try (InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
            sendResult = pubNub.sendFile()
                    .channel(channel)
                    .fileName(fileName)
                    .inputStream(is)
                    .message(message)
                    .meta(meta)
                    .messageType(expectedMessageType)
                    .spaceId(expectedSpaceId)
                    .sync();
        }


        assertTrue(fileEventReceived.await(10, TimeUnit.SECONDS));
        PNListFilesResult listedFiles = pubNub.listFiles()
                .channel(channel)
                .sync();

        boolean fileFoundOnList = false;
        for (PNUploadedFile f : listedFiles.getData()) {
            if (f.getId().equals(sendResult.getFile().getId())) {
                fileFoundOnList = true;
                break;
            }
        }
        assertTrue(fileFoundOnList);

        PNDownloadFileResult downloadResult = pubNub
                .downloadFile()
                .channel(channel)
                .fileName(fileName)
                .fileId(sendResult.getFile().getId())
                .sync();

        try (InputStream is = downloadResult.getByteStream()) {
            assertEquals(content, readToString(is));
        }


        pubNub.deleteFile()
                .channel(channel)
                .fileName(fileName)
                .fileId(sendResult.getFile().getId())
                .sync();
    }

    private String readToString(InputStream inputStream) {
        try (Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
            return s.hasNext() ? s.next() : "";
        }
    }

    private abstract static class LimitedListener extends SubscribeCallback {
        @Override
        public void presence(@NotNull PubNub pubnub, @NotNull PNPresenceEventResult pnPresenceEventResult) {

        }

        @Override
        public void message(@NotNull PubNub pubnub, @NotNull PNMessageResult pnMessageResult) {

        }

        @Override
        public void signal(@NotNull PubNub pubnub, @NotNull PNSignalResult pnSignalResult) {

        }

        @Override
        public void uuid(@NotNull PubNub pubnub, @NotNull PNUUIDMetadataResult pnUUIDMetadataResult) {

        }

        @Override
        public void channel(@NotNull PubNub pubnub, @NotNull PNChannelMetadataResult pnChannelMetadataResult) {

        }

        @Override
        public void membership(@NotNull PubNub pubnub, @NotNull PNMembershipResult pnMembershipResult) {

        }

        @Override
        public void messageAction(@NotNull PubNub pubnub, @NotNull PNMessageActionResult pnMessageActionResult) {

        }
    }


}
