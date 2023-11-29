package com.pubnub.api.endpoints.files;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.crypto.CryptoModuleKt;
import com.pubnub.api.endpoints.BuilderSteps.ChannelStep;
import com.pubnub.api.endpoints.Endpoint;
import com.pubnub.api.endpoints.files.requiredparambuilder.ChannelFileNameFileIdBuilder;
import com.pubnub.api.endpoints.files.requiredparambuilder.FilesBuilderSteps.FileIdStep;
import com.pubnub.api.endpoints.files.requiredparambuilder.FilesBuilderSteps.FileNameStep;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.files.PNBaseFile;
import com.pubnub.api.models.consumer.files.PNPublishFileMessageResult;
import com.pubnub.api.models.server.files.FileUploadNotification;
import com.pubnub.api.services.FilesService;
import lombok.Setter;
import lombok.experimental.Accessors;
import retrofit2.Call;
import retrofit2.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Accessors(chain = true, fluent = true)
public class PublishFileMessage extends Endpoint<List<Object>, PNPublishFileMessageResult> {

    @Setter
    private Object message;
    @Setter
    private Object meta;
    @Setter
    private Integer ttl;
    @Setter
    private Boolean shouldStore;
    private final String channel;
    private final PNBaseFile pnFile;
    private final FilesService filesService;
    private final MapperManager mapper;
    private final PNConfiguration configuration;

    public PublishFileMessage(String channel,
                              String fileName,
                              String fileId,
                              PubNub pubnubInstance,
                              TelemetryManager telemetry,
                              RetrofitManager retrofitInstance,
                              TokenManager tokenManager) {
        super(pubnubInstance, telemetry, retrofitInstance, tokenManager);
        this.channel = channel;
        this.pnFile = new PNBaseFile(fileId, fileName);
        this.filesService = retrofitInstance.getFilesService();
        this.mapper = pubnubInstance.getMapper();
        this.configuration = pubnubInstance.getConfiguration();
    }

    @Override
    protected List<String> getAffectedChannels() {
        return null;
    }

    @Override
    protected List<String> getAffectedChannelGroups() {
        return null;
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
    protected Call<List<Object>> doWork(Map<String, String> baseParams) throws PubNubException {
        String stringifiedMessage = mapper.toJson(new FileUploadNotification(this.message, pnFile));
        String messageAsString;
        CryptoModule cryptoModule = getPubnub().getCryptoModule();
        if (cryptoModule != null) {
            String encryptString = CryptoModuleKt.encryptString(cryptoModule, stringifiedMessage);
            messageAsString = "\"".concat(encryptString).concat("\"");
        } else {
            messageAsString = PubNubUtil.urlEncode(stringifiedMessage);
        }

        final HashMap<String, String> params = new HashMap<>(baseParams);

        if (meta != null) {
            String stringifiedMeta = mapper.toJson(meta);
            stringifiedMeta = PubNubUtil.urlEncode(stringifiedMeta);
            params.put("meta", stringifiedMeta);
        }

        if (shouldStore != null) {
            if (shouldStore) {
                params.put("store", "1");
            } else {
                params.put("store", "0");
            }
        }

        if (ttl != null) {
            params.put("ttl", String.valueOf(ttl));
        }

        return filesService.notifyAboutFileUpload(configuration.getPublishKey(),
                configuration.getSubscribeKey(),
                channel,
                messageAsString,
                params);
    }

    @Override
    protected PNPublishFileMessageResult createResponse(Response<List<Object>> input) throws PubNubException {
        if (input.body() == null) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_INTERNAL_ERROR)
                    .build();
        }
        long timetoken = Long.parseLong(input.body().get(2).toString());
        return new PNPublishFileMessageResult(timetoken);
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNFileAction;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

    public static class Builder
            extends ChannelFileNameFileIdBuilder<PublishFileMessage> {
        private Builder(ChannelStep<FileNameStep<FileIdStep<PublishFileMessage>>> builder) {
            super(builder);
        }
    }

    public static Builder builder(PubNub pubNub,
                                  TelemetryManager telemetryManager,
                                  RetrofitManager retrofitManager,
                                  TokenManager tokenManager) {
        return new Builder(ChannelFileNameFileIdBuilder.create((channel, fileName, fileId) ->
                new PublishFileMessage(channel, fileName, fileId, pubNub, telemetryManager, retrofitManager, tokenManager)));
    }
}
