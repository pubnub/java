package com.pubnub.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserIdTest {


    @Test
    void should_throw_exception_when_UserId_is_empty_string() {
        Assertions.assertThrows(PubNubRuntimeException.class, () -> new UserId(""));
    }

    @Test
    void should_throw_exception_when_UserId_is_null() {
        Assertions.assertThrows(PubNubRuntimeException.class, () -> new UserId(null));
    }
}
