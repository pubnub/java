package com.pubnub.api.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PNMessageTypeTest {

    @Test
    void message_of_type_message_should_be_indicated_by_messageType_being_null_or_0() {
        assertEquals(PNMessageType.MESSAGE01.toString(), PNMessageType.MESSAGE02.toString());
    }
}