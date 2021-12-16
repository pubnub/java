package com.pubnub.api.managers;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNReconnectionPolicy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReconnectionManagerTest {
    @Test
    public void reconnectionIntervalsEqualsForLinear() throws PubNubException {
        PNConfiguration pnConfiguration = new PNConfiguration(PubNub.generateUUID());
        PubNub pubNub = new PubNub(pnConfiguration);
        pnConfiguration.setReconnectionPolicy(PNReconnectionPolicy.LINEAR);
        final ReconnectionManager reconnectionManagerUnderTest = new ReconnectionManager(pubNub);

        int firstInterval = reconnectionManagerUnderTest.getNextInterval();
        int secondInterval = reconnectionManagerUnderTest.getNextInterval();

        assertEquals(secondInterval, firstInterval);
    }

    @Test
    public void reconnectionIntervalsIncreaseForExponential() throws PubNubException {
        PNConfiguration pnConfiguration = new PNConfiguration(PubNub.generateUUID());
        pnConfiguration.setReconnectionPolicy(PNReconnectionPolicy.EXPONENTIAL);
        PubNub pubNub = new PubNub(pnConfiguration);
        final ReconnectionManager reconnectionManagerUnderTest = new ReconnectionManager(pubNub);

        int firstInterval = reconnectionManagerUnderTest.getNextInterval();
        int secondInterval = reconnectionManagerUnderTest.getNextInterval();
        int thirdInterval = reconnectionManagerUnderTest.getNextInterval();

        assertTrue(firstInterval < secondInterval);
        assertTrue(secondInterval < thirdInterval);
    }
}