package com.pubnub.api.enums;

import com.pubnub.api.PubNubRuntimeException;

import java.util.HashMap;
import java.util.Map;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_UNKNOWN_MESSAGE_TYPE;

public enum PNMessageType {

    MESSAGE01(null, "pn_message"),
    MESSAGE02(0, "pn_message"),
    SIGNAL(1, "pn_signal"),   //this value is not expected in history call
    OBJECT(2, "pn_object"),   //this value is not expected in history call
    MESSAGE_ACTION(3, "pn_messageAction"),
    FILE(4, "pn_file");

    private static final Map<Integer, PNMessageType> BY_E_VALUE_FROM_SERVER = new HashMap<>();

    static {
        for (PNMessageType type : values()) {
            BY_E_VALUE_FROM_SERVER.put(type.eValueFromServer, type);
        }
    }

    public static PNMessageType valueByPnMessageType(Integer eValueFromServer) {
        if (null == BY_E_VALUE_FROM_SERVER.get(eValueFromServer)) {
            throw PubNubRuntimeException.builder().pubnubError(PNERROBJ_UNKNOWN_MESSAGE_TYPE).build();
        }
        return BY_E_VALUE_FROM_SERVER.get(eValueFromServer);
    }

    private final Integer eValueFromServer;
    private final String name;

    PNMessageType(Integer eValueFromServer, String name) {
        this.eValueFromServer = eValueFromServer;
        this.name = name;
    }

    public Integer getEValueFromServer() {
        return eValueFromServer;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
