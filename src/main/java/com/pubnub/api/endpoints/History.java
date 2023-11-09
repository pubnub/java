package com.pubnub.api.endpoints;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.crypto.CryptoModuleKt;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.managers.TelemetryManager;
import com.pubnub.api.managers.token_manager.TokenManager;
import com.pubnub.api.models.consumer.history.PNHistoryItemResult;
import com.pubnub.api.models.consumer.history.PNHistoryResult;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import retrofit2.Call;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.pubnub.api.endpoints.FetchMessages.PN_OTHER;

@Slf4j
@Accessors(chain = true, fluent = true)
public class History extends Endpoint<JsonElement, PNHistoryResult> {
    private static final int MAX_COUNT = 100;
    @Setter
    private String channel;
    @Setter
    private Long start;
    @Setter
    private Long end;
    @Setter
    private Boolean reverse;
    @Setter
    private Integer count;
    @Setter
    private Boolean includeTimetoken;
    @Setter
    private Boolean includeMeta;

    public History(PubNub pubnub,
                   TelemetryManager telemetryManager,
                   RetrofitManager retrofit,
                   TokenManager tokenManager) {
        super(pubnub, telemetryManager, retrofit, tokenManager);
    }

    @Override
    protected List<String> getAffectedChannels() {
        return Collections.singletonList(channel);
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
        if (includeMeta == null) {
            includeMeta = false;
        }
        if (includeTimetoken == null) {
            includeTimetoken = false;
        }
    }

    @Override
    protected Call<JsonElement> doWork(Map<String, String> params) {

        if (reverse != null) {
            params.put("reverse", String.valueOf(reverse));
        }

        if (includeTimetoken != null) {
            params.put("include_token", String.valueOf(includeTimetoken));
        }

        if (includeMeta) {
            params.put("include_meta", String.valueOf(includeMeta));
        }

        if (count != null && count > 0 && count <= MAX_COUNT) {
            params.put("count", String.valueOf(count));
        } else {
            params.put("count", "100");
        }

        if (start != null) {
            params.put("start", Long.toString(start).toLowerCase());
        }
        if (end != null) {
            params.put("end", Long.toString(end).toLowerCase());
        }

        return this.getRetrofit().getHistoryService().fetchHistory(this.getPubnub().getConfiguration()
                .getSubscribeKey(), channel, params);
    }

    @Override
    protected PNHistoryResult createResponse(Response<JsonElement> input) throws PubNubException {
        PNHistoryResult.PNHistoryResultBuilder historyData = PNHistoryResult.builder();
        List<PNHistoryItemResult> messages = new ArrayList<>();
        MapperManager mapper = getPubnub().getMapper();

        if (input.body() != null) {
            Long startTimeToken = mapper.elementToLong(mapper.getArrayElement(input.body(), 1));
            Long endTimeToken = mapper.elementToLong(mapper.getArrayElement(input.body(), 2));

            historyData.startTimetoken(startTimeToken);
            historyData.endTimetoken(endTimeToken);


            if (mapper.getArrayElement(input.body(), 0).isJsonArray()) {
                Iterator<JsonElement> it = mapper.getArrayIterator(mapper.getArrayElement(input.body(), 0));
                while (it.hasNext()) {
                    JsonElement historyEntry = it.next();
                    PNHistoryItemResult.PNHistoryItemResultBuilder historyItem = PNHistoryItemResult.builder();
                    JsonElement message;

                    if (includeTimetoken || includeMeta) {
                        JsonElement messageElement = mapper.getField(historyEntry, "message");
                        try {
                            message = processMessage(messageElement);
                        } catch (PubNubException e) {
                            if (e.getPubnubError() == PubNubErrorBuilder.PNERROBJ_PNERR_CRYPTO_IS_CONFIGURED_BUT_MESSAGE_IS_NOT_ENCRYPTED) {
                                message = messageElement;
                                historyItem.error(e.getPubnubError());
                            } else {
                                throw e;
                            }
                        }
                        if (includeTimetoken) {
                            historyItem.timetoken(mapper.elementToLong(historyEntry, "timetoken"));
                        }
                        if (includeMeta) {
                            historyItem.meta(mapper.getField(historyEntry, "meta"));
                        }
                    } else {
                        try {
                            message = processMessage(historyEntry);
                        } catch (PubNubException e) {
                            if (e.getPubnubError() == PubNubErrorBuilder.PNERROBJ_PNERR_CRYPTO_IS_CONFIGURED_BUT_MESSAGE_IS_NOT_ENCRYPTED) {
                                message = historyEntry;
                                historyItem.error(e.getPubnubError());
                            } else {
                                throw e;
                            }
                        }
                    }

                    historyItem.entry(message);
                    messages.add(historyItem.build());
                }
            } else {
                throw PubNubException.builder()
                        .pubnubError(PubNubErrorBuilder.PNERROBJ_HTTP_ERROR)
                        .errormsg("History is disabled")
                        .jso(input.body())
                        .build();
            }

            historyData.messages(messages);
        }

        return historyData.build();
    }

    @Override
    protected PNOperationType getOperationType() {
        return PNOperationType.PNHistoryOperation;
    }

    @Override
    protected boolean isAuthRequired() {
        return true;
    }

    private JsonElement processMessage(JsonElement message) throws PubNubException {
        // if we do not have a crypto module, there is no way to process the node; let's return.
        CryptoModule cryptoModule = this.getPubnub().getCryptoModule();
        if (cryptoModule == null) {
            return message;
        }

        MapperManager mapper = getPubnub().getMapper();
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
            throw new PubNubException(error.getMessage(), error, message, null, 0, null, null);
        }
        outputObject = this.getPubnub().getMapper().fromJson(outputText, JsonElement.class);

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