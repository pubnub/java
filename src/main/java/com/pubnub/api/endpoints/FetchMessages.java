package com.pubnub.api.endpoints;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.crypto.CryptoModuleKt;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.PNBoundedPage;
import com.pubnub.api.models.consumer.history.PNFetchMessageItem;
import com.pubnub.api.models.consumer.history.PNFetchMessagesResult;
import com.pubnub.api.models.server.FetchMessagesEnvelope;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.VisibleForTesting;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_HISTORY_MESSAGE_ACTIONS_MULTIPLE_CHANNELS;

@Slf4j
@Accessors(chain = true, fluent = true)
public class FetchMessages extends Endpoint<FetchMessagesEnvelope, PNFetchMessagesResult> {
    private static final int SINGLE_CHANNEL_DEFAULT_MESSAGES = 100;
    private static final int SINGLE_CHANNEL_MAX_MESSAGES = 100;
    private static final int MULTIPLE_CHANNEL_DEFAULT_MESSAGES = 25;
    private static final int MULTIPLE_CHANNEL_MAX_MESSAGES = 25;
    private static final int DEFAULT_MESSAGES_WITH_ACTIONS = 25;
    private static final int MAX_MESSAGES_WITH_ACTIONS = 25;
    static final String PN_OTHER = "pn_other";

    @Setter
    private List<String> channels;
    @Setter
    private Integer maximumPerChannel;
    @Setter
    private Long start;
    @Setter
    private Long end;

    @Setter
    private Boolean includeMeta;
    @Setter
    private Boolean includeMessageActions;
    @Setter
    private boolean includeMessageType = true;
    @Setter
    private boolean includeUUID = true;

    public FetchMessages(PubNub pubnub,
                         TelemetryManager telemetryManager,
                         RetrofitManager retrofit,
                         TokenManager tokenManager) {
        super(pubnub, telemetryManager, retrofit, tokenManager);
        channels = new ArrayList<>();
    }

    @Override
    protected List<String> getAffectedChannels() {
        return channels;
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

        if (channels == null || channels.size() == 0) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_CHANNEL_MISSING).build();
        }

        if (includeMeta == null) {
            includeMeta = false;
        }

        if (includeMessageActions == null) {
            includeMessageActions = false;
        }

        if (!includeMessageActions) {
            if (channels.size() == 1) {
                if (maximumPerChannel == null || maximumPerChannel < 1) {
                    maximumPerChannel = SINGLE_CHANNEL_DEFAULT_MESSAGES;
                    log.info("maximumPerChannel param defaulting to " + maximumPerChannel);
                } else if (maximumPerChannel > SINGLE_CHANNEL_MAX_MESSAGES) {
                    maximumPerChannel = SINGLE_CHANNEL_MAX_MESSAGES;
                    log.info("maximumPerChannel param defaulting to " + maximumPerChannel);
                }
            } else {
                if (maximumPerChannel == null || maximumPerChannel < 1) {
                    maximumPerChannel = MULTIPLE_CHANNEL_DEFAULT_MESSAGES;
                    log.info("maximumPerChannel param defaulting to " + maximumPerChannel);
                } else if (maximumPerChannel > MULTIPLE_CHANNEL_MAX_MESSAGES) {
                    maximumPerChannel = MULTIPLE_CHANNEL_MAX_MESSAGES;
                    log.info("maximumPerChannel param defaulting to " + maximumPerChannel);
                }
            }
        } else {
            if (maximumPerChannel == null || maximumPerChannel < 1 || maximumPerChannel > MAX_MESSAGES_WITH_ACTIONS) {
                maximumPerChannel = DEFAULT_MESSAGES_WITH_ACTIONS;
                log.info("maximumPerChannel param defaulting to " + maximumPerChannel);
            }
        }
    }

    @Override
    protected Call<FetchMessagesEnvelope> doWork(Map<String, String> params) throws PubNubException {
        params.put("max", String.valueOf(maximumPerChannel));

        if (start != null) {
            params.put("start", Long.toString(start).toLowerCase());
        }
        if (end != null) {
            params.put("end", Long.toString(end).toLowerCase());
        }

        if (includeMeta) {
            params.put("include_meta", String.valueOf(includeMeta));
        }
        params.put("include_uuid", Boolean.toString(includeUUID));
        params.put("include_message_type", Boolean.toString(includeMessageType));

        if (!includeMessageActions) {
            return this.getRetrofit().getHistoryService().fetchMessages(
                    this.getPubnub().getConfiguration().getSubscribeKey(), PubNubUtil.joinString(channels, ","),
                    params);
        } else {
            if (channels.size() > 1) {
                throw PubNubException.builder().pubnubError(PNERROBJ_HISTORY_MESSAGE_ACTIONS_MULTIPLE_CHANNELS).build();
            }
            return this.getRetrofit().getHistoryService().fetchMessagesWithActions(
                    this.getPubnub().getConfiguration().getSubscribeKey(), channels.get(0), params);
        }
    }

    @Override
    protected PNFetchMessagesResult createResponse(Response<FetchMessagesEnvelope> input) throws PubNubException {
        if (input.body() == null) {
            throw PubNubException.builder().pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR).build();
        }

        HashMap<String, List<PNFetchMessageItem>> channelsMap = new HashMap<>();

        for (Map.Entry<String, List<PNFetchMessageItem>> entry : input.body().getChannels().entrySet()) {
            List<PNFetchMessageItem> items = new ArrayList<>();

            for (PNFetchMessageItem item : entry.getValue()) {
                PNFetchMessageItem.PNFetchMessageItemBuilder messageItemBuilder = item.toBuilder();

                try {
                    messageItemBuilder.message(processMessage(item.getMessage()));
                } catch (PubNubException e) {
                    if (e.getPubnubError() == PubNubErrorBuilder.PNERROBJ_PNERR_CRYPTO_IS_CONFIGURED_BUT_MESSAGE_IS_NOT_ENCRYPTED) {
                        messageItemBuilder.message(item.getMessage());
                        messageItemBuilder.error(e.getPubnubError());
                    } else {
                        throw e;
                    }
                }
                if (includeMessageActions) {
                    if (item.getActions() != null) {
                        messageItemBuilder.actions(item.getActions());
                    } else {
                        messageItemBuilder.actions(new HashMap<>());
                    }
                } else {
                    messageItemBuilder.actions(null);
                }
                items.add(messageItemBuilder.build());
            }

            channelsMap.put(entry.getKey(), items);
        }

        PNBoundedPage page = null;
        FetchMessagesEnvelope.FetchMessagesPage more = input.body().getMore();
        if (more != null) {
            page = new PNBoundedPage(more.getStart(), more.getEnd(), more.getMax());
        }

        return PNFetchMessagesResult.builder()
                .channels(channelsMap)
                .page(page)
                .build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNFetchMessagesOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

    @VisibleForTesting
    JsonElement processMessage(JsonElement message) throws PubNubException {
        // if we do not have a crypto module, there is no way to process the node; let's return.
        CryptoModule cryptoModule = this.getPubnub().getCryptoModule();
        if (cryptoModule == null) {
            return message;
        }

        MapperManager mapper = this.getPubnub().getMapper();
        String inputText;
        String outputText;
        JsonElement outputObject;

        if (mapper.isJsonObject(message)) {
            if (mapper.hasField(message, PN_OTHER)) {
                inputText = mapper.elementToString(message, PN_OTHER);
            } else {
                PubNubError error = logAndReturnDecryptionError();
                throw new PubNubException(error.getMessage(), error, null, null, 0, null, null);
            }
        } else {
            inputText = mapper.elementToString(message);
        }

        try {
            outputText = CryptoModuleKt.decryptString(cryptoModule, inputText);
        } catch (com.pubnub.api.crypto.exception.PubNubException e) {
            PubNubError error = logAndReturnDecryptionError();
            throw new PubNubException(error.getMessage(), error, null, null, 0, null, null);
        }
        outputObject = mapper.fromJson(outputText, JsonElement.class);

        // inject the decoded response into the payload
        if (mapper.isJsonObject(message) && mapper.hasField(message, PN_OTHER)) {
            JsonObject objectNode = mapper.getAsObject(message);
            mapper.putOnObject(objectNode, PN_OTHER, outputObject);
            outputObject = objectNode;
        }

        return outputObject;
    }

    private PubNubError logAndReturnDecryptionError() {
        PubNubError error = PubNubErrorBuilder.PNERROBJ_PNERR_CRYPTO_IS_CONFIGURED_BUT_MESSAGE_IS_NOT_ENCRYPTED;
        log.warn(error.getMessage());
        return error;
    }
}
