package com.pubnub.api;

import com.pubnub.api.enums.PNMessageType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessageTypeTest {
    private MessageType objectUnderTest;

    @Test
    void should_return_userMessageType_when_userMessageType_is_provided() {
        PNMessageType pnMessageType = PNMessageType.MESSAGE01;
        String userMessageType = "userSpecificMessageType";

        objectUnderTest = new MessageType(pnMessageType, userMessageType);

        assertEquals(userMessageType, objectUnderTest.getType());
    }

    @Test
    void should_return_pnMessageType_when_userMessageType_is_null() {
        PNMessageType pnMessageType = PNMessageType.MESSAGE01;
        String userMessageType = null;

        objectUnderTest = new MessageType(pnMessageType, userMessageType);

        assertEquals(pnMessageType.toString(), objectUnderTest.getType());
    }

    @Test
    void should_return_null_when_userMessageType_is_null_and_pnMessageType_is_null() {
        PNMessageType pnMessageType = null;
        String userMessageType = null;

        PubNubRuntimeException pubNubRuntimeException = Assertions.assertThrows(PubNubRuntimeException.class, () -> {
            objectUnderTest = new MessageType(pnMessageType, userMessageType);
        });

        assertEquals("PNMessageType can't be null nor empty.", pubNubRuntimeException.getPubnubError().getMessage());
    }
}