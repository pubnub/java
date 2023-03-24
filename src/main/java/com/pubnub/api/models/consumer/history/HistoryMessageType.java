package com.pubnub.api.models.consumer.history;

import com.pubnub.api.PubNubRuntimeException;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_UNKNOWN_HISTORY_MESSAGE_TYPE;

public enum HistoryMessageType {
    MESSAGE(0), // if includeMessageType is true then null from server also indicates MESSAGE
    FILE(4);

    private final Integer eValueFromServer;

    HistoryMessageType(Integer eValueFromServer) {
        this.eValueFromServer = eValueFromServer;
    }

    public Integer geteValueFromServer() {
        return eValueFromServer;
    }

    public static HistoryMessageType of(Integer eValueFromServer) {
        if (eValueFromServer == null || eValueFromServer == MESSAGE.eValueFromServer) {
            return MESSAGE;
        } else if (eValueFromServer == FILE.eValueFromServer) {
            return FILE;
        } else {
            throw PubNubRuntimeException.builder().pubnubError(PNERROBJ_UNKNOWN_HISTORY_MESSAGE_TYPE).build();
        }
    }
}
