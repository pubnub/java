package com.pubnub.api.models.consumer.history;


public class MessageType {
    private PnMessageType pnMessageType;
    private final String userMessageType;

    public MessageType(String userMessageType) {
        this.userMessageType = userMessageType;
    }

    MessageType(PnMessageType pnMessageType, String userMessageType) {
        this.pnMessageType = pnMessageType;
        this.userMessageType = userMessageType;
    }

    public String getType() {
        if (userMessageType != null && !userMessageType.isEmpty()) {
            return userMessageType;
        } else {
           return pnMessageType != null ? pnMessageType.toString() : null;
        }
    }
}
