package com.pubnub.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserIdTest {


    @Test
    void should_throw_exception_when_UserId_is_empty_string() {
        Assertions.assertThrows(PubNubException.class, () -> new UserId(""));
    }

    @Test
    void should_throw_exception_when_UserId_is_null() {
        Assertions.assertThrows(PubNubException.class, () -> new UserId(null));
    }
}
