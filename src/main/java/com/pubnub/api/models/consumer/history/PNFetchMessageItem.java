package com.pubnub.api.models.consumer.history;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.pubnub.api.SpaceId;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;

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
    private Integer messageType;

    //user defined type
    private final String type;

    public SpaceId getSpaceId() {
        if (spaceId == null) {
            return null;
        }
        return new SpaceId(spaceId);
    }

    public HistoryMessageType getMessageType() {
        return includeMessageType ? HistoryMessageType.of(messageType) : null;
    }

    @Data
    public static class Action {
        private final String uuid;
        private final String actionTimetoken;
    }
}
