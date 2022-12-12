package com.pubnub.api.models.consumer.history;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessageTypeTest {
    private MessageType objectUnderTest;

    @Test
    void should_return_userMessageType_when_userMessageType_is_provided() {
        PnMessageType pnMessageType = PnMessageType.MESSAGE01;
        String userMessageType = "userSpecificMessageType";

        objectUnderTest = new MessageType(pnMessageType, userMessageType);

        assertEquals(userMessageType, objectUnderTest.getType());
    }

    @Test
    void should_return_pnMessageType_when_userMessageType_is_null() {
        PnMessageType pnMessageType = PnMessageType.MESSAGE01;
        String userMessageType = null;

        objectUnderTest = new MessageType(pnMessageType, userMessageType);

        assertEquals(pnMessageType.toString(), objectUnderTest.getType());
    }

    @Test
    void should_return_null_when_userMessageType_is_null_and_pnMessageType_is_null() {
        PnMessageType pnMessageType = null;
        String userMessageType = null;

        objectUnderTest = new MessageType(pnMessageType, userMessageType);

        assertNull(objectUnderTest.getType());
    }
}