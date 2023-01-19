package com.pubnub.api.models.consumer.history;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.pubnub.api.MessageType;
import com.pubnub.api.PubNubRuntimeException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.enums.PNMessageType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_TO_GET_MESSAGE_TYPE_INCLUDEMESSAGETYPE_SHOULD_NOT_BE_SET_TO_FALSE;

@Builder(toBuilder = true)
@Data
public class PNFetchMessageItem {

    private final JsonElement message;
    private final JsonElement meta;
    private final Long timetoken;
    private final HashMap<String, HashMap<String, List<Action>>> actions;
    private final String uuid;
    @SerializedName("space_id")
    private final String spaceId;

    @Getter(AccessLevel.PRIVATE)
    private final boolean includeMessageType;

    @Getter(AccessLevel.NONE)
    @SerializedName("message_type")
    private final Integer pnMessageType;

    @Getter(AccessLevel.NONE)
    @SerializedName("type")
    private final String userMessageType;

    public SpaceId getSpaceId() {
        if (spaceId == null) {
            return null;
        }
        return new SpaceId(spaceId);
    }

    /**
     * Get message type of MessageItem.
     * <p>
     * Caution:
     * When includeMessageType is set to true then pnMessageType can be null
     * informing that MessageItem is of type "message".
     * When includeMessageType is set to false then pnMessageType == null means that pnMessageType is not present.
     * When includeMessageType is set to false you shouldn't call getMessageType()
     *
     * @return MessageType of PNFetchMessageItem
     * @throws PubNubRuntimeException if includeMessageType is set to false
     */
    public MessageType getMessageType() {
        if (includeMessageType) {
            return new MessageType(PNMessageType.valueByPnMessageType(pnMessageType), userMessageType);
        } else {
            throw PubNubRuntimeException.builder().pubnubError(PNERROBJ_TO_GET_MESSAGE_TYPE_INCLUDEMESSAGETYPE_SHOULD_NOT_BE_SET_TO_FALSE).build();
        }
    }

    @Data
    public static class Action {
        private final String uuid;
        private final String actionTimetoken;
    }
}
