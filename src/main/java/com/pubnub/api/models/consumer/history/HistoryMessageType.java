package com.pubnub.api.models.consumer.history;

import com.pubnub.api.PubNubException;

public enum HistoryMessageType {
    Message,
    File;

    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_FILE = 4;

    public static HistoryMessageType of(Integer messageType) throws PubNubException {
        if (messageType == null) {
            return Message;
        }
        switch (messageType) {
            case TYPE_MESSAGE: return Message;
            case TYPE_FILE: return File;
            default: throw new PubNubException("Unknown message type value $value", null, null, null, 0, null, null);
        }
    }
}
