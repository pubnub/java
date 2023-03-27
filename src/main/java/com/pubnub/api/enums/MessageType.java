package com.pubnub.api.enums;

import com.pubnub.api.PubNubRuntimeException;

import java.util.HashMap;
import java.util.Map;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_UNKNOWN_MESSAGE_TYPE;

public enum MessageType {

    MESSAGE01(null),
    MESSAGE02(0),
    SIGNAL(1),
    OBJECT(2),
    MESSAGE_ACTION(3),
    FILE(4);

    private static final Map<Integer, MessageType> BY_E_VALUE_FROM_SERVER = new HashMap<>();

    static {
        for (MessageType type : values()) {
            BY_E_VALUE_FROM_SERVER.put(type.eValueFromServer, type);
        }
    }

    public static MessageType valueByPnMessageType(Integer eValueFromServer) {
        if (null == BY_E_VALUE_FROM_SERVER.get(eValueFromServer)) {
            throw PubNubRuntimeException.builder().pubnubError(PNERROBJ_UNKNOWN_MESSAGE_TYPE).build();
        }
        return BY_E_VALUE_FROM_SERVER.get(eValueFromServer);
    }

    private final Integer eValueFromServer;

    MessageType(Integer eValueFromServer) {
        this.eValueFromServer = eValueFromServer;
    }
}
