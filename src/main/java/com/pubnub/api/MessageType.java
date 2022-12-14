package com.pubnub.api;


import com.pubnub.api.enums.PNMessageType;
import org.jetbrains.annotations.NotNull;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_PNMESSAGETYPE_NULL;
import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_USERMESSAGETYPE_NULL_OR_EMPTY;

public class MessageType {
    private PNMessageType pnMessageType;
    private final String userMessageType;

    public MessageType(@NotNull final String userMessageType) {
        PubNubUtil.require(userMessageType != null && !userMessageType.isEmpty(), PNERROBJ_USERMESSAGETYPE_NULL_OR_EMPTY);
        this.userMessageType = userMessageType;
    }

    MessageType(@NotNull PNMessageType pnMessageType, String userMessageType) {
        PubNubUtil.require(pnMessageType != null, PNERROBJ_PNMESSAGETYPE_NULL);
        this.pnMessageType = pnMessageType;
        this.userMessageType = userMessageType;
    }

    @NotNull
    public String getType() {
        if (userMessageType != null && !userMessageType.isEmpty()) {
            return userMessageType;
        } else {
            return pnMessageType.toString();
        }
    }
}
