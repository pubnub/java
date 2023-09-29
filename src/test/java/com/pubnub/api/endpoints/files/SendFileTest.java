package com.pubnub.api.endpoints.files;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.endpoints.remoteaction.TestRemoteAction;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.files.PNBaseFile;
import com.pubnub.api.models.consumer.files.PNFileUploadResult;
import com.pubnub.api.models.consumer.files.PNPublishFileMessageResult;
import com.pubnub.api.models.server.files.FileUploadRequestDetails;
import com.pubnub.api.models.server.files.FormField;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.pubnub.api.PubNubUtil.readBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SendFileTest implements TestsWithFiles {
    private final String channel = "channel";
    private final String filename = "test.txt";
    private final GenerateUploadUrl.Factory generateUploadUrlFactory = mock(GenerateUploadUrl.Factory.class);
    private final PublishFileMessage.Builder publishFileMessageBuilder = mock(PublishFileMessage.Builder.class,
            RETURNS_DEEP_STUBS);
    private final UploadFile.Factory sendFileToS3Factory = mock(UploadFile.Factory.class);

    @Override
    @Rule
    public TemporaryFolder getTemporaryFolder() {
        return folder;
    }

    @Test
    public void sync_happyPath() throws PubNubException, IOException {
        //given
        File file = getTemporaryFile(filename);
        FileUploadRequestDetails fileUploadRequestDetails = generateUploadUrlProperResponse();
        PNFileUploadResult expectedResponse = pnFileUploadResult();
        PNPublishFileMessageResult publishFileMessageResult = new PNPublishFileMessageResult(expectedResponse.getTimetoken());

        when(generateUploadUrlFactory.create(any(), any())).thenReturn(TestRemoteAction.successful(
                fileUploadRequestDetails));
        when(sendFileToS3Factory.create(any(), any(), any(), any())).thenReturn(TestRemoteAction.successful(null));
        PublishFileMessage publishFileMessage = AlwaysSuccessfulPublishFileMessage.create(publishFileMessageResult);
        when(publishFileMessageBuilder.channel(any()).fileName(any()).fileId(any()))
                .thenReturn(publishFileMessage);

        //when
        PNFileUploadResult result;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            result = sendFile(channel, file.getName(), fileInputStream).sync();
        }

        //then
        assertEquals(expectedResponse, result);
    }

    @Test
    public void async_happyPath() throws InterruptedException, IOException {
        //given
        CountDownLatch countDownLatch = new CountDownLatch(1);
        File file = getTemporaryFile(filename);
        FileUploadRequestDetails fileUploadRequestDetails = generateUploadUrlProperResponse();
        PNFileUploadResult expectedResponse = pnFileUploadResult();
        PNPublishFileMessageResult publishFileMessageResult = new PNPublishFileMessageResult(expectedResponse.getTimetoken());


        when(generateUploadUrlFactory.create(any(), any())).thenReturn(TestRemoteAction.successful(
                fileUploadRequestDetails));
        when(sendFileToS3Factory.create(any(), any(), any(), any())).thenReturn(TestRemoteAction.successful(null));
        PublishFileMessage publishFileMessage = AlwaysSuccessfulPublishFileMessage.create(publishFileMessageResult);
        when(publishFileMessageBuilder.channel(any()).fileName(any()).fileId(any()))
                .thenReturn(publishFileMessage);

        //when
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            sendFile(channel, file.getName(), fileInputStream).async(
                    (result, status) -> {
                        assertEquals(expectedResponse, result);
                        countDownLatch.countDown();
                    }
            );
        }

        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS));
    }
    @Test
    public void async_publishFileMessageRetry() throws InterruptedException, IOException {
        //given
        CountDownLatch countDownLatch = new CountDownLatch(1);
        File file = getTemporaryFile(filename);
        FileUploadRequestDetails fileUploadRequestDetails = generateUploadUrlProperResponse();
        PNFileUploadResult expectedResponse = pnFileUploadResult();
        PNPublishFileMessageResult publishFileMessageResult = new PNPublishFileMessageResult(expectedResponse.getTimetoken());
        int numberOfRetries = 5;

        when(generateUploadUrlFactory.create(any(), any())).thenReturn(TestRemoteAction.successful(
                fileUploadRequestDetails));
        when(sendFileToS3Factory.create(any(), any(), any(), any())).thenReturn(TestRemoteAction.successful(null));
        PublishFileMessage publishFileMessage = spy(FailingPublishFileMessage.create(publishFileMessageResult, numberOfRetries - 1));
        when(publishFileMessageBuilder.channel(any()).fileName(any()).fileId(any()))
                .thenReturn(publishFileMessage);

        //when
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            sendFile(channel, file.getName(), fileInputStream, numberOfRetries).async(
                    (result, status) -> {
                        assertEquals(expectedResponse, result);
                        countDownLatch.countDown();
                    }
            );
        }

        //then
        assertTrue(countDownLatch.await(1, TimeUnit.SECONDS));
        verify(publishFileMessage, times(numberOfRetries)).async(any());
    }

    @Test
    public void sync_publishFileMessageRetry() throws InterruptedException, IOException, PubNubException {
        //given
        File file = getTemporaryFile(filename);
        FileUploadRequestDetails fileUploadRequestDetails = generateUploadUrlProperResponse();
        PNFileUploadResult expectedResponse = pnFileUploadResult();
        PNPublishFileMessageResult publishFileMessageResult = new PNPublishFileMessageResult(expectedResponse.getTimetoken());
        int numberOfRetries = 5;

        when(generateUploadUrlFactory.create(any(), any())).thenReturn(TestRemoteAction.successful(
                fileUploadRequestDetails));
        when(sendFileToS3Factory.create(any(), any(), any(), any())).thenReturn(TestRemoteAction.successful(null));
        PublishFileMessage publishFileMessage = spy(FailingPublishFileMessage.create(publishFileMessageResult, numberOfRetries - 1));
        when(publishFileMessageBuilder.channel(any()).fileName(any()).fileId(any()))
                .thenReturn(publishFileMessage);

        //when
        PNFileUploadResult result;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            result = sendFile(channel, file.getName(), fileInputStream, numberOfRetries).sync();
        }

        //then
        assertEquals(expectedResponse, result);
        verify(publishFileMessage, times(numberOfRetries)).sync();

    }

    private FileUploadRequestDetails generateUploadUrlProperResponse() {
        return new FileUploadRequestDetails(200,
                new PNBaseFile("id", "name"),
                "url",
                "GET",
                Instant.now().plusSeconds(50).toString(),
                new FormField("key", "value"),
                Collections.emptyList());
    }

    private PNFileUploadResult pnFileUploadResult() {
        return new PNFileUploadResult(1337L, 200, new PNBaseFile("id", "name"));
    }

    @SneakyThrows
    private SendFile sendFile(String channel, String fileName, InputStream inputStream, int numberOfRetries) {
        return new SendFile(new SendFile.Builder.SendFileRequiredParams(channel, fileName, readBytes(inputStream), null),
                generateUploadUrlFactory,
                publishFileMessageBuilder,
                sendFileToS3Factory,
                Executors.newSingleThreadExecutor(),
                numberOfRetries,
                CryptoModule.createLegacyCryptoModule("enigma", true)
        );
    }

    private SendFile sendFile(String channel, String fileName, InputStream inputStream) {
        return sendFile(channel, fileName, inputStream, 1);
    }

    static class FailingPublishFileMessage extends PublishFileMessage {

        private final PNPublishFileMessageResult result;
        private final int numberOfFailsBeforeSuccess;
        private AtomicInteger numberOfFails = new AtomicInteger(0);


        public static PublishFileMessage create(PNPublishFileMessageResult result, int numberOfFailsBeforeSuccess) {
            return new FailingPublishFileMessage(result, numberOfFailsBeforeSuccess);
        }


        public FailingPublishFileMessage(PNPublishFileMessageResult result,
                                         int numberOfFailsBeforeSuccess)  {
            super("channel", "fileName", "fileId", mock(PubNub.class), null, mock(RetrofitManager.class), new TokenManager());
            this.result = result;
            this.numberOfFailsBeforeSuccess = numberOfFailsBeforeSuccess;
        }

        @Override
        public void async(@NotNull PNCallback<PNPublishFileMessageResult> callback) {
            if (numberOfFails.getAndAdd(1) < numberOfFailsBeforeSuccess) {
                callback.onResponse(null, PNStatus.builder().error(true).statusCode(400).build());
            } else {
                callback.onResponse(result, PNStatus.builder().statusCode(200).build());
            }
        }

        @Override
        public @Nullable PNPublishFileMessageResult sync() throws PubNubException {
            if (numberOfFails.getAndAdd(1) < numberOfFailsBeforeSuccess) {
                throw PubNubException.builder().build();
            }
            return result;
        }
    }

    static class AlwaysSuccessfulPublishFileMessage extends PublishFileMessage {

        private final PNPublishFileMessageResult result;

        public static PublishFileMessage create(PNPublishFileMessageResult result) {
            PubNub pubNub = mock(PubNub.class);
            RetrofitManager retrofitManager = mock(RetrofitManager.class);
            return new AlwaysSuccessfulPublishFileMessage(result,
                    pubNub,
                    retrofitManager);
        }

        AlwaysSuccessfulPublishFileMessage(PNPublishFileMessageResult result,
                                           PubNub pubnubInstance,
                                           RetrofitManager retrofitInstance) {
            super("channel", "fileName", "fileId", mock(PubNub.class), null, mock(RetrofitManager.class), new TokenManager());
            this.result = result;
        }

        @Nullable
        @Override
        public PNPublishFileMessageResult sync() throws PubNubException {
            return result;
        }

        @Override
        public void async(@NotNull PNCallback<PNPublishFileMessageResult> callback) {
            callback.onResponse(result, PNStatus.builder().statusCode(200).build());
        }
    }
}
