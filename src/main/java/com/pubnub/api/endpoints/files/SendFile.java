package com.pubnub.api.endpoints.files;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.endpoints.BuilderSteps.ChannelStep;
import com.pubnub.api.endpoints.files.requiredparambuilder.FilesBuilderSteps.FileIdStep;
import com.pubnub.api.endpoints.files.requiredparambuilder.FilesBuilderSteps.FileNameStep;
import com.pubnub.api.endpoints.files.requiredparambuilder.FilesBuilderSteps.InputStreamStep;
import com.pubnub.api.endpoints.remoteaction.ComposableRemoteAction;
import com.pubnub.api.endpoints.remoteaction.MappingRemoteAction;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.endpoints.remoteaction.RetryingRemoteAction;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNErrorData;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.files.PNBaseFile;
import com.pubnub.api.models.consumer.files.PNFileUploadResult;
import com.pubnub.api.models.consumer.files.PNPublishFileMessageResult;
import com.pubnub.api.models.server.files.FileUploadRequestDetails;
import com.pubnub.api.vendor.FileEncryptionUtil;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import static com.pubnub.api.PubNubUtil.readBytes;

@Accessors(chain = true, fluent = true)
public class SendFile implements RemoteAction<PNFileUploadResult> {

    private final RemoteAction<PNFileUploadResult> sendFileMultistepAction;
    private final String channel;
    private final String fileName;
    private final byte[] content;
    private final Exception byteContentReadingException;
    private final ExecutorService executorService;
    private final int fileMessagePublishRetryLimit;
    @Setter
    private Object message;
    @Setter
    private Object meta;
    @Setter
    private Integer ttl;
    @Setter
    private Boolean shouldStore;
    @Setter
    private String cipherKey;
    private CryptoModule cryptoModule;

    SendFile(Builder.SendFileRequiredParams requiredParams,
             GenerateUploadUrl.Factory generateUploadUrlFactory,
             ChannelStep<FileNameStep<FileIdStep<PublishFileMessage>>> publishFileMessageBuilder,
             UploadFile.Factory sendFileToS3Factory,
             ExecutorService executorService,
             int fileMessagePublishRetryLimit,
             CryptoModule cryptoModule
    ) {
        this.channel = requiredParams.channel();
        this.fileName = requiredParams.fileName();
        this.content = requiredParams.content();
        this.byteContentReadingException = requiredParams.byteReadingException;
        this.executorService = executorService;
        this.fileMessagePublishRetryLimit = fileMessagePublishRetryLimit;
        this.sendFileMultistepAction = sendFileComposedActions(
                generateUploadUrlFactory,
                publishFileMessageBuilder,
                sendFileToS3Factory);
        this.cryptoModule = FileEncryptionUtil.effectiveCryptoModule(cryptoModule, cipherKey);
    }

    public PNFileUploadResult sync() throws PubNubException {
        validate();
        return sendFileMultistepAction.sync();
    }

    public void async(@NotNull PNCallback<PNFileUploadResult> callback) {
        executorService
                .execute(() -> {
                    try {
                        validate();
                        sendFileMultistepAction.async(callback);
                    } catch (PubNubException ex) {
                        callback.onResponse(null,
                                PNStatus.builder()
                                        .error(true)
                                        .errorData(new PNErrorData(ex.getErrormsg(), ex))
                                        .build());
                    }
                });
    }

    private void validate() throws PubNubException {
        if (channel == null || channel.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING).build();
        }

        if (byteContentReadingException != null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS)
                    .errormsg(byteContentReadingException.getMessage()).build();
        }

        if (content == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS)
                    .errormsg("Content cannot be null").build();
        }

        if (fileName == null || fileName.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_INVALID_ARGUMENTS)
                    .errormsg("File name cannot be null nor empty").build();
        }
    }

    private RemoteAction<PNFileUploadResult> sendFileComposedActions(
            GenerateUploadUrl.Factory generateUploadUrlFactory,
            ChannelStep<FileNameStep<FileIdStep<PublishFileMessage>>> publishFileMessageBuilder,
            UploadFile.Factory sendFileToS3Factory) {
        final AtomicReference<FileUploadRequestDetails> result = new AtomicReference<>();
        return ComposableRemoteAction
                .firstDo(generateUploadUrlFactory.create(channel, fileName))
                .then(res -> {
                    result.set(res);
                    return sendToS3(res, sendFileToS3Factory);
                })
                .checkpoint()
                .then(res -> autoRetry(publishFileMessage(publishFileMessageBuilder, result),
                        fileMessagePublishRetryLimit))
                .then(res -> mapPublishFileMessageToFileUpload(result, res));
    }

    private PublishFileMessage publishFileMessage(ChannelStep<FileNameStep<FileIdStep<PublishFileMessage>>> publishFileMessageBuilder,
                                                  AtomicReference<FileUploadRequestDetails> result) {
        return publishFileMessageBuilder.channel(channel)
                .fileName(result.get().getData().getName())
                .fileId(result.get().getData().getId())
                .message(message)
                .meta(meta)
                .ttl(ttl)
                .shouldStore(shouldStore);
    }

    private <T> RemoteAction<T> autoRetry(RemoteAction<T> remoteAction, int maxNumberOfRetries) {
        return RetryingRemoteAction.autoRetry(remoteAction,
                maxNumberOfRetries,
                PNOperationType.PNFileAction,
                executorService);
    }

    @NotNull
    private RemoteAction<PNFileUploadResult> mapPublishFileMessageToFileUpload(AtomicReference<FileUploadRequestDetails> result,
                                                                               PNPublishFileMessageResult res) {
        return MappingRemoteAction.map(res,
                pnPublishFileMessageResult -> new PNFileUploadResult(pnPublishFileMessageResult.getTimetoken(),
                        HttpURLConnection.HTTP_OK,
                        new PNBaseFile(result.get().getData().getId(), result.get().getData().getName())));
    }

    @Override
    public void retry() {
        sendFileMultistepAction.retry();
    }

    @Override
    public void silentCancel() {
        sendFileMultistepAction.silentCancel();
    }

    private RemoteAction<Void> sendToS3(FileUploadRequestDetails result,
                                        UploadFile.Factory sendFileToS3Factory) {
        return sendFileToS3Factory.create(fileName, content, cryptoModule, result);
    }

    public static Builder builder(PubNub pubnub,
                                  TelemetryManager telemetry,
                                  RetrofitManager retrofit,
                                  TokenManager tokenManager) {
        return new Builder(pubnub, telemetry, retrofit, tokenManager);
    }

    public static class Builder implements ChannelStep<FileNameStep<InputStreamStep<SendFile>>> {

        private final PubNub pubnub;
        private final TelemetryManager telemetry;
        private final RetrofitManager retrofit;
        private final TokenManager tokenManager;

        Builder(PubNub pubnub,
                TelemetryManager telemetry,
                RetrofitManager retrofit,
                TokenManager tokenManager) {

            this.pubnub = pubnub;
            this.telemetry = telemetry;
            this.retrofit = retrofit;
            this.tokenManager = tokenManager;
        }

        @Override
        public FileNameStep<InputStreamStep<SendFile>> channel(String channel) {
            return new InnerBuilder(pubnub, telemetry, retrofit, tokenManager).channel(channel);
        }

        public static class InnerBuilder implements
                ChannelStep<FileNameStep<InputStreamStep<SendFile>>>,
                FileNameStep<InputStreamStep<SendFile>>,
                InputStreamStep<SendFile> {
            private final PubNub pubnub;
            private final RetrofitManager retrofit;
            private String channelValue;
            private String fileNameValue;
            private final PublishFileMessage.Builder publishFileMessageBuilder;
            private final UploadFile.Factory uploadFileFactory;
            private final GenerateUploadUrl.Factory generateUploadUrlFactory;

            private InnerBuilder(PubNub pubnub,
                                 TelemetryManager telemetry,
                                 RetrofitManager retrofit,
                                 TokenManager tokenManager) {
                this.pubnub = pubnub;
                this.retrofit = retrofit;
                this.publishFileMessageBuilder = PublishFileMessage.builder(pubnub, telemetry, retrofit, tokenManager);
                this.uploadFileFactory = new UploadFile.Factory(pubnub, retrofit);
                this.generateUploadUrlFactory = new GenerateUploadUrl.Factory(pubnub, telemetry, retrofit, tokenManager);
            }

            @Override
            public FileNameStep<InputStreamStep<SendFile>> channel(String channel) {
                this.channelValue = channel;
                return this;
            }

            @Override
            public InputStreamStep<SendFile> fileName(String fileName) {
                this.fileNameValue = fileName;
                return this;
            }

            @Override
            public SendFile inputStream(InputStream inputStream) {
                try {
                    return new SendFile(new SendFileRequiredParams(channelValue,
                            fileNameValue,
                            readBytes(inputStream),
                            null),
                            generateUploadUrlFactory,
                            publishFileMessageBuilder,
                            uploadFileFactory,
                            retrofit.getTransactionClientExecutorService(),
                            pubnub.getConfiguration().getFileMessagePublishRetryLimit(),
                            pubnub.getCryptoModule());

                } catch (IOException e) {
                    return new SendFile(new SendFileRequiredParams(channelValue,
                            fileNameValue,
                            null,
                            e),
                            generateUploadUrlFactory,
                            publishFileMessageBuilder,
                            uploadFileFactory,
                            retrofit.getTransactionClientExecutorService(),
                            pubnub.getConfiguration().getFileMessagePublishRetryLimit(),
                            pubnub.getCryptoModule()
                    );
                }
            }
        }

        @Data
        static class SendFileRequiredParams {
            private final String channel;
            private final String fileName;
            private final byte[] content;
            private final Exception byteReadingException;
        }
    }
}
