package com.pubnub.api.models.consumer.history;

import com.google.gson.JsonPrimitive;
import com.pubnub.api.PubNubRuntimeException;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PNFetchMessageItemTest {
    private PNFetchMessageItem objectUnderTest;


    @Test
    void when_includeMessageType_is_false_then_getMessageType_should_return_null() {
        boolean includeMessageType = false;
        String spaceId = null;
        Integer pnMessageType = null;
        String userMessageType = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, pnMessageType, userMessageType);

        assertNull(objectUnderTest.getMessageType());
    }

    @Test
    void when_includeMessageType_is_true_then_getMessageType_should_return_notNull() {
        boolean includeMessageType = true;
        String spaceId = null;
        Integer pnMessageType = null;
        String userMessageType = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, pnMessageType, userMessageType);

        assertNotNull(objectUnderTest.getMessageType());
    }

    @Test
    void when_spaceIdValue_is_null_should_return_null() {
        boolean includeMessageType = false;
        String spaceId = null;
        Integer pnMessageType = null;
        String userMessageType = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, pnMessageType, userMessageType);

        assertNull(objectUnderTest.getSpaceId());
    }

    @Test
    void when_spaceIdValue_is_notNull_should_return_notNull() {
        boolean includeMessageType = false;
        String spaceId = "mySpace";
        Integer pnMessageType = null;
        String userMessageType = null;

        objectUnderTest = getPNFetchMessageItem(includeMessageType, spaceId, pnMessageType, userMessageType);

        assertNotNull(objectUnderTest.getSpaceId());
    }


    @NotNull
    private PNFetchMessageItem getPNFetchMessageItem(boolean includeMessageType, String spaceId, Integer pnMessageType, String userMessageType) {
        return new PNFetchMessageItem(new JsonPrimitive("message1"), null, 16739754716727555L, null, "client-5d254772-8845-431b-8a68-cc2902eac4e4", spaceId, includeMessageType, pnMessageType, userMessageType);
    }
}