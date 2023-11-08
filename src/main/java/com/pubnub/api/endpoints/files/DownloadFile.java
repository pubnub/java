package com.pubnub.api.endpoints.files;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.endpoints.BuilderSteps.ChannelStep;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.endpoints.files.requiredparambuilder.ChannelFileNameFileIdBuilder;
import com.pubnub.api.endpoints.files.requiredparambuilder.FilesBuilderSteps.FileIdStep;
import com.pubnub.api.endpoints.files.requiredparambuilder.FilesBuilderSteps.FileNameStep;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.files.PNDownloadFileResult;
import lombok.Setter;
import lombok.experimental.Accessors;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.pubnub.api.vendor.FileEncryptionUtil.effectiveCryptoModule;

@Accessors(chain = true, fluent = true)
public class DownloadFile extends Endpoint<ResponseBody, PNDownloadFileResult> {
    private final String channel;
    private final String fileName;
    private final String fileId;

    @Setter
    private String cipherKey;

    public DownloadFile(String channel,
                        String fileName,
                        String fileId,
                        PubNub pubnubInstance,
                        TelemetryManager telemetry,
                        RetrofitManager retrofitInstance,
                        TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, tokenManager);
        this.channel = channel;
        this.fileName = fileName;
        this.fileId = fileId;
    }

    @Override
    protected List<String> getAffectedChannels() {
        return Collections.singletonList(channel);
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return Collections.emptyList();
    }

    @Override
    protected void validateParams() throws PubNubException {
        if (this.getPubnub().getConfiguration().getSubscribeKey() == null
                || this.getPubnub().getConfiguration().getSubscribeKey().isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_SUBSCRIBE_KEY_MISSING).build();
        }

        if (channel == null || channel.isEmpty()) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING).build();
        }
    }

    @Override
    protected Call<ResponseBody> doWork(Map<String, String> baseParams) throws PubNubException {
        return getRetrofit().getFilesService().downloadFile(getPubnub().getConfiguration().getSubscribeKey(),
                channel,
                fileId,
                fileName,
                baseParams);
    }

    @Override
    protected PNDownloadFileResult createResponse(Response<ResponseBody> input) throws PubNubException {
        if (input.body() == null) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_INTERNAL_ERROR)
                    .build();
        }
        CryptoModule cryptoModule = effectiveCryptoModule(getPubnub(), cipherKey);
        InputStream byteStream = input.body().byteStream();
        if (cryptoModule == null) {
            return new PNDownloadFileResult(fileName, byteStream);
        } else {
            InputStream decryptedByteStream = cryptoModule.decryptStream(byteStream);
            return new PNDownloadFileResult(fileName, decryptedByteStream);
        }
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNFileAction;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

    public static class Builder extends ChannelFileNameFileIdBuilder<DownloadFile> {
        private Builder(ChannelStep<FileNameStep<FileIdStep<DownloadFile>>> builder) {
            super(builder);
        }
    }

    public static Builder builder(PubNub pubNub,
                                  TelemetryManager telemetryManager,
                                  RetrofitManager retrofitManager,
                                  TokenManager tokenManager) {
        return new Builder(ChannelFileNameFileIdBuilder.create((channel, fileName, fileId) ->
                new DownloadFile(channel, fileName, fileId, pubNub, telemetryManager, retrofitManager, tokenManager)));
    }
}
