package com.pubnub.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PubNubTest {
    private PubNub pubnub;
    private PNConfiguration pnConfiguration;

    @Before
    public void beforeEach() throws IOException {
        pnConfiguration = new PNConfiguration();
        pnConfiguration.setSecure(true);
        pnConfiguration.setSubscribeKey("demo");
        pnConfiguration.setPublishKey("demo");
    }

    @org.junit.Test
    public void testCreateSuccess() throws IOException, PubNubException {
        pubnub = new PubNub(pnConfiguration);
        Assert.assertNotNull("pubnub object is null", pubnub);
        Assert.assertNotNull(pubnub.getConfiguration());
        Assert.assertEquals("https://pubsub.pubnub.com", pubnub.getBaseUrl());
    }

    @Test
    public void testEncryptCustomKey() throws PubNubException {
        pubnub = new PubNub(pnConfiguration);
        Assert.assertEquals("iALQtn3PfIXe74CT/wrS7g==", pubnub.encrypt("test1", "cipherKey").trim());

    }

    @Test
    public void testEncryptConfigurationKey() throws PubNubException {
        pnConfiguration.setCipherKey("cipherKey");
        pubnub = new PubNub(pnConfiguration);
        Assert.assertEquals("iALQtn3PfIXe74CT/wrS7g==", pubnub.encrypt("test1").trim());

    }

    @Test
    public void testDecryptCustomKey() throws PubNubException {
        pubnub = new PubNub(pnConfiguration);
        Assert.assertEquals("test1", pubnub.decrypt("iALQtn3PfIXe74CT/wrS7g==", "cipherKey").trim());

    }

    @Test
    public void testDecryptConfigurationKey() throws PubNubException {
        pnConfiguration.setCipherKey("cipherKey");
        pubnub = new PubNub(pnConfiguration);
        Assert.assertEquals("test1", pubnub.decrypt("iALQtn3PfIXe74CT/wrS7g==").trim());

    }

}