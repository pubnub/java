package com.pubnub.api.models.consumer.history;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import com.pubnub.api.PubNubError;
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
    private final PubNubError error;

    @SerializedName("message_type")
    @Getter(AccessLevel.NONE)
    private final String messageType;
    private int getMessageType() {
        if (messageType == null || messageType.isEmpty()) {
            return 0;
        } else {
            return Integer.parseInt(messageType);
        }
    }

    @Data
    public static class Action {
        private final String uuid;
        private final String actionTimetoken;
    }
}
