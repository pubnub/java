package com.pubnub.api.workers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.pubnub.api.MessageType;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubRuntimeException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.SpaceId;
import com.pubnub.api.enums.PNMessageType;
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
import com.pubnub.api.models.consumer.pubsub.objects.ObjectResult;
import com.pubnub.api.models.server.PresenceEnvelope;
import com.pubnub.api.models.server.PublishMetaData;
import com.pubnub.api.models.server.SubscribeMessage;
import com.pubnub.api.models.server.files.FileUploadNotification;
import com.pubnub.api.services.FilesService;
import com.pubnub.api.vendor.Crypto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_INVALID_OBJECT_TYPE;

@Slf4j
@AllArgsConstructor
public class SubscribeMessageProcessor {
    private static final String PRESENCE_CHANNEL_SUFFIX = "-pnpres";
    private static final String JSON_KEY_PN_OTHER = "pn_other";

    private final PubNub pubnub;
    private final DuplicationManager duplicationManager;

    PNEvent processIncomingPayload(SubscribeMessage message) throws PubNubException {
        MapperManager mapper = this.pubnub.getMapper();

        String channel = message.getChannel();
        String subscriptionMatch = getSubscriptionMatch(message);
        PublishMetaData publishMetaData = message.getPublishMetaData();

        if (this.pubnub.getConfiguration().isDedupOnSubscribe()) {
            if (this.duplicationManager.isDuplicate(message)) {
                return null;
            } else {
                this.duplicationManager.addEntry(message);
            }
        }

        if (isPresenceMessage(message)) {
            return getPnPresenceEventResult(message, mapper, channel, subscriptionMatch, publishMetaData);
        } else {
            JsonElement extractedMessage = processMessage(message);
            if (extractedMessage == null) {
                log.debug("unable to parse payload on #processIncomingMessages");
            }

            BasePubSubResult basePubSubResult = getBasePubSubResult(message, channel, subscriptionMatch, publishMetaData);
            MessageType messageType = new MessageType(PNMessageType.valueByPnMessageType(message.getPnMessageType()), message.getUserMessageType());
            SpaceId spaceId = message.getSpaceId() != null ? new SpaceId(message.getSpaceId()) : null;
            if (isMessage(message)) {
                return new PNMessageResult(basePubSubResult, extractedMessage, messageType, spaceId);
            } else if (isSignal(message)) {
                return new PNSignalResult(basePubSubResult, extractedMessage, messageType, spaceId);
            } else if (isUserOrSpaceOrMembershipObject(message)) {
                ObjectPayload objectPayload = mapper.convertValue(extractedMessage, ObjectPayload.class);
                if (canHandleObjectCallback(objectPayload)) {
                    String type = objectPayload.getType();
                    return getObjectResult(mapper, basePubSubResult, objectPayload, type);
                }
            } else if (isMessageAction(message)) {
                return getPnMessageActionResult(mapper, extractedMessage, basePubSubResult);
            } else if (isFile(message)) {
                return getPnFileEventResult(message, mapper, publishMetaData, extractedMessage);
            }
        }
        return null;
    }

    @Nullable
    private String getSubscriptionMatch(SubscribeMessage message) {
        String subscriptionMatch = message.getSubscriptionMatch();
        String channel = message.getChannel();
        if (channel != null && channel.equals(subscriptionMatch)) {
            subscriptionMatch = null;
        }
        return subscriptionMatch;
    }

    private boolean isPresenceMessage(SubscribeMessage message) {
        return message.getChannel().endsWith(PRESENCE_CHANNEL_SUFFIX);
    }

    @SuppressWarnings("deprecation")
    private PNPresenceEventResult getPnPresenceEventResult(SubscribeMessage message, MapperManager mapper, String channel, String subscriptionMatch, PublishMetaData publishMetaData) {
        PresenceEnvelope presencePayload = mapper.convertValue(message.getPayload(), PresenceEnvelope.class);

        String strippedPresenceChannel = null;
        String strippedPresenceSubscription = null;

        if (channel != null) {
            strippedPresenceChannel = PubNubUtil.replaceLast(channel, PRESENCE_CHANNEL_SUFFIX, "");
        }
        if (subscriptionMatch != null) {
            strippedPresenceSubscription = PubNubUtil.replaceLast(subscriptionMatch, PRESENCE_CHANNEL_SUFFIX, "");
        }

        JsonElement isHereNowRefresh = message.getPayload().getAsJsonObject().get("here_now_refresh");

        return PNPresenceEventResult.builder()
                .event(presencePayload.getAction())
                // deprecated
                .actualChannel((subscriptionMatch != null) ? channel : null)
                // deprecated
                .subscribedChannel(subscriptionMatch != null ? subscriptionMatch : channel)
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
    }

    @SuppressWarnings("deprecation")
    private BasePubSubResult getBasePubSubResult(SubscribeMessage message, String channel, String subscriptionMatch, PublishMetaData publishMetaData) {
        return BasePubSubResult.builder()
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
    }

    private boolean isMessage(SubscribeMessage message) {
        //should be "==" instead of "equals" for PNMessageType.MESSAGE01.getEValueFromServer()  because its value is null
        return message.getPnMessageType() == PNMessageType.MESSAGE01.getEValueFromServer() || message.getPnMessageType().equals(PNMessageType.MESSAGE02.getEValueFromServer());
    }

    private boolean isSignal(SubscribeMessage message) {
        return message.getPnMessageType().equals(PNMessageType.SIGNAL.getEValueFromServer());
    }

    private boolean isUserOrSpaceOrMembershipObject(SubscribeMessage message) {
        return message.getPnMessageType().equals(PNMessageType.OBJECT.getEValueFromServer());
    }

    private boolean isMessageAction(SubscribeMessage message) {
        return message.getPnMessageType().equals(PNMessageType.MESSAGE_ACTION.getEValueFromServer());
    }

    private PNMessageActionResult getPnMessageActionResult(MapperManager mapper, JsonElement extractedMessage, BasePubSubResult result) {
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
    }

    private boolean isFile(SubscribeMessage message) {
        return message.getPnMessageType().equals(PNMessageType.FILE.getEValueFromServer());
    }

    private PNFileEventResult getPnFileEventResult(SubscribeMessage message, MapperManager mapper, PublishMetaData publishMetaData, JsonElement extractedMessage) {
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

    @NotNull
    private ObjectResult<?> getObjectResult(MapperManager mapper, BasePubSubResult result, ObjectPayload objectPayload, String type) {
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
                throw PubNubRuntimeException.builder().pubnubError(PNERROBJ_INVALID_OBJECT_TYPE).build();
        }
    }

    private JsonElement processMessage(SubscribeMessage subscribeMessage) throws PubNubException {
        JsonElement input = subscribeMessage.getPayload();

        // if we do not have a crypto key, there is no way to process the node; let's return.
        if (pubnub.getConfiguration().getCipherKey() == null) {
            return input;
        }

        // if the message couldn't possibly be encrypted in the first place, there is no way to process the node; let's
        // return.
        if (!subscribeMessage.supportsEncryption()) {
            return input;
        }

        Crypto crypto = new Crypto(pubnub.getConfiguration().getCipherKey(),
                pubnub.getConfiguration().isUseRandomInitializationVector());
        MapperManager mapper = this.pubnub.getMapper();
        String inputText;
        String outputText;
        JsonElement outputObject;

        if (mapper.isJsonObject(input) && mapper.hasField(input, JSON_KEY_PN_OTHER)) {
            inputText = mapper.elementToString(input, JSON_KEY_PN_OTHER);
        } else {
            inputText = mapper.elementToString(input);
        }

        outputText = crypto.decrypt(inputText);

        outputObject = mapper.fromJson(outputText, JsonElement.class);

        // inject the decoded response into the payload
        if (mapper.isJsonObject(input) && mapper.hasField(input, JSON_KEY_PN_OTHER)) {
            JsonObject objectNode = mapper.getAsObject(input);
            mapper.putOnObject(objectNode, JSON_KEY_PN_OTHER, outputObject);
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
