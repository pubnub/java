package com.pubnub.api.workers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.crypto.CryptoModuleKt;
import com.pubnub.api.managers.DuplicationManager;
import com.pubnub.api.managers.MapperManager;
import com.pubnub.api.models.consumer.files.PNDownloadableFile;
import com.pubnub.api.models.consumer.message_actions.PNMessageAction;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadata;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadataResult;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembership;
import com.pubnub.api.models.consumer.objects_api.membership.PNMembershipResult;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadataResult;
import com.pubnub.api.models.consumer.pubsub.BasePubSubResult;
import com.pubnub.api.models.consumer.pubsub.PNEvent;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.consumer.pubsub.message_actions.PNMessageActionResult;
import com.pubnub.api.models.consumer.pubsub.objects.ObjectPayload;
import com.pubnub.api.models.server.PresenceEnvelope;
import com.pubnub.api.models.server.PublishMetaData;
import com.pubnub.api.models.server.SubscribeMessage;
import com.pubnub.api.models.server.files.FileUploadNotification;
import com.pubnub.api.services.FilesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@AllArgsConstructor
public class SubscribeMessageProcessor {
    public static final int TYPE_MESSAGE = 0;
    private final int typeSignal = 1;
    private final int typeObject = 2;
    private final int typeMessageAction = 3;
    public static final int TYPE_FILES = 4;

    private final PubNub pubnub;
    private final DuplicationManager duplicationManager;

    @SuppressWarnings("deprecation")
    PNEvent processIncomingPayload(SubscribeMessage message) throws PubNubException {
        MapperManager mapper = this.pubnub.getMapper();

        String channel = message.getChannel();
        String subscriptionMatch = message.getSubscriptionMatch();
        PublishMetaData publishMetaData = message.getPublishMetaData();

        if (channel != null && channel.equals(subscriptionMatch)) {
            subscriptionMatch = null;
        }

        if (this.pubnub.getConfiguration().isDedupOnSubscribe()) {
            if (this.duplicationManager.isDuplicate(message)) {
                return null;
            } else {
                this.duplicationManager.addEntry(message);
            }
        }

        if (message.getChannel().endsWith("-pnpres")) {
            PresenceEnvelope presencePayload = mapper.convertValue(message.getPayload(), PresenceEnvelope.class);

            String strippedPresenceChannel = null;
            String strippedPresenceSubscription = null;

            if (channel != null) {
                strippedPresenceChannel = PubNubUtil.replaceLast(channel, "-pnpres", "");
            }
            if (subscriptionMatch != null) {
                strippedPresenceSubscription = PubNubUtil.replaceLast(subscriptionMatch, "-pnpres", "");
            }

            JsonElement isHereNowRefresh = message.getPayload().getAsJsonObject().get("here_now_refresh");

            PNPresenceEventResult pnPresenceEventResult = PNPresenceEventResult.builder()
                    .event(presencePayload.getAction())
                    // deprecated
                    .actualChannel((subscriptionMatch != null) ? channel : null)
                    .subscribedChannel(subscriptionMatch != null ? subscriptionMatch : channel)
                    // deprecated
                    .channel(strippedPresenceChannel)
                    .subscription(strippedPresenceSubscription)
                    .state(presencePayload.getData())
                    .timetoken(publishMetaData.getPublishTimetoken())
                    .occupancy(presencePayload.getOccupancy())
                    .uuid(presencePayload.getUuid())
                    .timestamp(presencePayload.getTimestamp())
                    .join(getDelta(message.getPayload().getAsJsonObject().get("join")))
                    .leave(getDelta(message.getPayload().getAsJsonObject().get("leave")))
                    .timeout(getDelta(message.getPayload().getAsJsonObject().get("timeout")))
                    .hereNowRefresh(isHereNowRefresh != null && isHereNowRefresh.getAsBoolean())
                    .build();

            return pnPresenceEventResult;
        } else {
            JsonElement extractedMessage = processMessage(message);

            if (extractedMessage == null) {
                log.debug("unable to parse payload on #processIncomingMessages");
            }

            BasePubSubResult result = BasePubSubResult.builder()
                    // deprecated
                    .actualChannel((subscriptionMatch != null) ? channel : null)
                    .subscribedChannel(subscriptionMatch != null ? subscriptionMatch : channel)
                    // deprecated
                    .channel(channel)
                    .subscription(subscriptionMatch)
                    .timetoken(publishMetaData.getPublishTimetoken())
                    .publisher(message.getIssuingClientId())
                    .userMetadata(message.getUserMetadata())
                    .build();

            if (message.getType() == null) {
                return new PNMessageResult(result, extractedMessage);
            } else if (message.getType() == TYPE_MESSAGE) {
                return new PNMessageResult(result, extractedMessage);
            } else if (message.getType() == typeSignal) {
                return new PNSignalResult(result, extractedMessage);
            } else if (message.getType() == typeObject) {
                ObjectPayload objectPayload = mapper.convertValue(extractedMessage, ObjectPayload.class);
                String type = objectPayload.getType();
                if (canHandleObjectCallback(objectPayload)) {
                    switch (type) {
                        case "channel":
                            final PNChannelMetadataResult channelMetadataResult = new PNChannelMetadataResult(result,
                                    objectPayload.getEvent(), mapper.convertValue(objectPayload.getData(),
                                    PNChannelMetadata.class));
                            return channelMetadataResult;
                        case "membership":
                            final PNMembershipResult membershipResult = new PNMembershipResult(result,
                                    objectPayload.getEvent(), mapper.convertValue(objectPayload.getData(),
                                    PNMembership.class));
                            return membershipResult;
                        case "uuid":
                            final PNUUIDMetadataResult uuidMetadataResult = new PNUUIDMetadataResult(result,
                                    objectPayload.getEvent(),
                                    mapper.convertValue(objectPayload.getData(), PNUUIDMetadata.class));
                            return uuidMetadataResult;
                        default:
                    }
                }
            } else if (message.getType() == typeMessageAction) {
                ObjectPayload objectPayload = mapper.convertValue(extractedMessage, ObjectPayload.class);
                JsonObject data = objectPayload.getData().getAsJsonObject();
                if (!data.has("uuid")) {
                    data.addProperty("uuid", result.getPublisher());
                }
                return PNMessageActionResult.actionBuilder()
                        .result(result)
                        .event(objectPayload.getEvent())
                        .data(mapper.convertValue(data, PNMessageAction.class))
                        .build();
            } else if (message.getType() == TYPE_FILES) {
                FileUploadNotification event = mapper.convertValue(extractedMessage, FileUploadNotification.class);
                final JsonElement jsonMessage;
                if (event.getMessage() != null) {
                    jsonMessage = mapper.toJsonTree(event.getMessage());
                } else {
                    jsonMessage = JsonNull.INSTANCE;
                }

                return PNFileEventResult.builder()
                        .file(new PNDownloadableFile(event.getFile().getId(),
                                event.getFile().getName(),
                                buildFileUrl(message.getChannel(),
                                        event.getFile().getId(),
                                        event.getFile().getName())))
                        .message(event.getMessage())
                        .channel(message.getChannel())
                        .publisher(message.getIssuingClientId())
                        .timetoken(publishMetaData.getPublishTimetoken())
                        .jsonMessage(jsonMessage)
                        .build();
            }

        }
        return null;
    }

    private JsonElement processMessage(SubscribeMessage subscribeMessage) throws PubNubException {
        JsonElement input = subscribeMessage.getPayload();

        // if we do not have a crypto module, there is no way to process the node; let's return.
        CryptoModule cryptoModule = pubnub.getCryptoModule();
        if (cryptoModule == null) {
            return input;
        }

        // if the message couldn't possibly be encrypted in the first place, there is no way to process the node; let's
        // return.
        if (!subscribeMessage.supportsEncryption()) {
            return input;
        }

        MapperManager mapper = this.pubnub.getMapper();
        String inputText;
        String outputText;
        JsonElement outputObject;

        if (mapper.isJsonObject(input) && mapper.hasField(input, "pn_other")) {
            inputText = mapper.elementToString(input, "pn_other");
        } else {
            inputText = mapper.elementToString(input);
        }

        outputText = CryptoModuleKt.decryptString(cryptoModule, inputText);

        outputObject = mapper.fromJson(outputText, JsonElement.class);

        // inject the decoded response into the payload
        if (mapper.isJsonObject(input) && mapper.hasField(input, "pn_other")) {
            JsonObject objectNode = mapper.getAsObject(input);
            mapper.putOnObject(objectNode, "pn_other", outputObject);
            outputObject = objectNode;
        }

        return outputObject;
    }

    @SuppressWarnings("RegExpRedundantEscape")
    private final String formatFriendlyGetFileUrl = "%s" + FilesService.GET_FILE_URL.replaceAll("\\{.*?\\}", "%s");

    private String buildFileUrl(String channel, String fileId, String fileName) {
        String basePath = String.format(formatFriendlyGetFileUrl,
                pubnub.getBaseUrl(),
                pubnub.getConfiguration().getSubscribeKey(),
                channel,
                fileId,
                fileName);

        ArrayList<String> queryParams = new ArrayList<>();
        String authKey = pubnub.getConfiguration().getAuthKey();

        if (PubNubUtil.shouldSignRequest(pubnub.getConfiguration())) {
            int timestamp = pubnub.getTimestamp();
            String signature = generateSignature(pubnub.getConfiguration(), basePath, authKey, timestamp);
            queryParams.add(PubNubUtil.TIMESTAMP_QUERY_PARAM_NAME + "=" + timestamp);
            queryParams.add(PubNubUtil.SIGNATURE_QUERY_PARAM_NAME + "=" + signature);
        }

        if (authKey != null) {
            queryParams.add(PubNubUtil.AUTH_QUERY_PARAM_NAME + "=" + authKey);
        }

        if (queryParams.isEmpty()) {
            return basePath;
        } else {
            return basePath + "?" + PubNubUtil.joinString(queryParams, "&");
        }
    }

    private String generateSignature(PNConfiguration configuration, String url, String authKey, int timestamp) {
        HashMap<String, String> queryParams = new HashMap<>();
        if (authKey != null) {
            queryParams.put("auth", authKey);
        }
        return PubNubUtil.generateSignature(configuration,
                url,
                queryParams,
                "get",
                null,
                timestamp
        );
    }

    private boolean canHandleObjectCallback(final ObjectPayload objectPayload) {
        return objectPayload.getVersion().equals("2.0");
    }

    private List<String> getDelta(JsonElement delta) {
        List<String> list = new ArrayList<>();
        if (delta != null) {
            JsonArray jsonArray = delta.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                list.add(jsonArray.get(i).getAsString());
            }
        }

        return list;
    }
}
