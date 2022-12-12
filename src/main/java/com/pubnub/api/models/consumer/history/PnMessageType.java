package com.pubnub.api.models.consumer.history;

import java.util.HashMap;
import java.util.Map;

public enum PnMessageType {

    MESSAGE01(null, "message"),
    MESSAGE02(0, "message"),
    SIGNAL(1, "signal"),   //this value is not expected in history call
    OBJECT(2, "object"),   //this value is not expected in history call
    MESSAGE_ACTION(3, "messageAction"),
    FILES(4, "files");

    private static final Map<Integer, PnMessageType> BY_E_VALUE_FROM_SERVER = new HashMap<>();

    static {
        for (PnMessageType type : values()) {
            BY_E_VALUE_FROM_SERVER.put(type.eValueFromServer, type);
        }
    }

    public static PnMessageType valueByPnMessageType(Integer eValueFromServer) {
        return BY_E_VALUE_FROM_SERVER.get(eValueFromServer);
    }

    private final Integer eValueFromServer;
    private final String name;

    PnMessageType(Integer eValueFromServer, String name) {
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
