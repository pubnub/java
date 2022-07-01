package com.pubnub.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PNConfigurationTest {

    @Test
    void should_set_uuid_creating_PNConfiguration() throws PubNubException {
        String userId01value = "userId01";
        PNConfiguration pnConfiguration = new PNConfiguration(new UserId(userId01value));

        assertEquals(userId01value, pnConfiguration.getUuid());
    }

    @Test
    void can_setUserId() throws PubNubException {
        PNConfiguration pnConfiguration = new PNConfiguration(new UserId("userId01"));
        UserId newUserId = new UserId("newUserId");
        pnConfiguration.setUserId(newUserId);

        assertEquals(newUserId.getValue(), pnConfiguration.getUserId().getValue());
    }

    @Test
    void can_getUserId() throws PubNubException {
        String userId01value = "userId01";
        PNConfiguration pnConfiguration = new PNConfiguration(new UserId(userId01value));
        UserId retrievedUserId = pnConfiguration.getUserId();

        assertEquals(userId01value, retrievedUserId.getValue());
    }

    @Test
    void should_throw_exception_when_UserId_is_empty_string() {
        Assertions.assertThrows(PubNubException.class, () -> new PNConfiguration(new UserId("")));
    }

    @Test
    void should_throw_exception_when_setting_userId_to_empty_string() {
        Assertions.assertThrows(PubNubException.class, () -> {
            PNConfiguration pnConfiguration = new PNConfiguration(new UserId("userId01"));
            pnConfiguration.setUserId(new UserId(""));
        });
    }

    @Test
    void can_reset_userId_to_non_empty_string() throws PubNubException {
        PNConfiguration pnConfiguration = new PNConfiguration(new UserId("userId01"));
        pnConfiguration.setUserId(new UserId("newUserId"));
    }

    @Test
    void should_throw_exception_when_uuid_is_empty_string() {
        Assertions.assertThrows(PubNubException.class, () -> new PNConfiguration(""));
    }

    @Test
    void should_throw_exception_when_uuid_is_null_string() {
        Assertions.assertThrows(PubNubException.class, () -> {
            new PNConfiguration((String) null);
        });
    }
}