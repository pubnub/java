package com.pubnub.api.models.consumer.history;

import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class PNFetchMessageItemTest {
    private PNFetchMessageItem objectUnderTest;


    @Test
    void when_includeMessageType_is_false_then_getMessageType_should_return_null() {
        boolean includeMessageType = false;
        String spaceId = null;
        Integer pnMessageType = null;
        String type = "myType";

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, pnMessageType, type);

        assertNull(objectUnderTest.getMessageType());
    }

    @Test
    void when_server_return_value_for_type_then_SDK_should_also_return_the_value() {
        boolean includeMessageType = false;
        String spaceId = null;
        Integer pnMessageType = null;
        String type = "myType";

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, pnMessageType, type);

        assertEquals(type, objectUnderTest.getType());
    }

    @Test
    void when_server_return_null_value_for_type_then_SDK_should_also_return_null() {
        boolean includeMessageType = false;
        String spaceId = null;
        Integer eValueFromServer = null;
        String type = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, eValueFromServer, type);

        assertNull(objectUnderTest.getType());
    }

    @Test
    void when_includeMessageType_is_true_and_messageType_is_null_then_getMessageType_should_return_MESSAGE() {
        boolean includeMessageType = true;
        String spaceId = null;
        Integer eValueFromServer = null;
        String type = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, eValueFromServer, type);

        assertEquals(HistoryMessageType.MESSAGE, objectUnderTest.getMessageType());
    }

    @Test
    void when_includeMessageType_is_true_and_messageType_is_0_then_getMessageType_should_return_MESSAGE() {
        boolean includeMessageType = true;
        String spaceId = null;
        Integer eValueFromServer = 0;
        String type = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, eValueFromServer, type);

        assertEquals(HistoryMessageType.MESSAGE, objectUnderTest.getMessageType());
    }

    @Test
    void when_includeMessageType_is_true_and_messageType_is_4_then_getMessageType_should_return_FILE() {
        boolean includeMessageType = true;
        String spaceId = null;
        Integer eValueFromServer = 4;
        String type = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, eValueFromServer, type);

        assertEquals(HistoryMessageType.FILE, objectUnderTest.getMessageType());
    }

    @Test
    void when_spaceIdValue_is_null_should_return_null() {
        boolean includeMessageType = false;
        String spaceId = null;
        Integer eValueFromServer = null;
        String type = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, eValueFromServer, type);

        assertNull(objectUnderTest.getSpaceId());
    }

    @Test
    void when_spaceIdValue_is_notNull_should_return_notNull() {
        boolean includeMessageType = false;
        String spaceId = "mySpace";
        Integer eValueFromServer = null;
        String type = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, eValueFromServer, type);

        assertNotNull(objectUnderTest.getSpaceId());
    }


    @NotNull
    private PNFetchMessageItem getPNFetchMessageItem(boolean includeMessageType, String spaceId, Integer eValueFromServer, String type) {
        return new PNFetchMessageItem(new JsonPrimitive("message1"), null, 16739754716727555L, null, "client-5d254772-8845-431b-8a68-cc2902eac4e4", spaceId, includeMessageType, eValueFromServer, type);
    }
}