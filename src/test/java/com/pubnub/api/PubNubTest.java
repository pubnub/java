package com.pubnub.api;

import com.pubnub.api.enums.PNReconnectionPolicy;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class PubNubTest {
    private PubNub pubnub;
    private PNConfiguration pnConfiguration;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pnConfiguration = new PNConfiguration(PubNub.generateUUID());
        pnConfiguration.setSubscribeKey("demo");
        pnConfiguration.setPublishKey("demo");
        pnConfiguration.setUseRandomInitializationVector(false);
    }

    @After
    public void cleanup() {
        pubnub.forceDestroy();
        pubnub = null;
    }

    @Test
    public void testCreateSuccess() {
        pubnub = new PubNub(pnConfiguration);
        Assert.assertEquals(true, pubnub.getConfiguration().isSecure());
        Assert.assertNotNull("pubnub object is null", pubnub);
        Assert.assertNotNull(pubnub.getConfiguration());
        Assert.assertEquals("https://ps.pndsn.com", pubnub.getBaseUrl());
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

    @Test
    public void testPNConfiguration() {
        pnConfiguration.setSubscribeTimeout(3000);
        pnConfiguration.setConnectTimeout(4000);
        pnConfiguration.setNonSubscribeRequestTimeout(5000);
        pnConfiguration.setReconnectionPolicy(PNReconnectionPolicy.NONE);
        pubnub = new PubNub(pnConfiguration);

        Assert.assertNotNull("pubnub object is null", pubnub);
        Assert.assertNotNull(pubnub.getConfiguration());
        Assert.assertEquals("https://ps.pndsn.com", pubnub.getBaseUrl());
        Assert.assertEquals(3000, pnConfiguration.getSubscribeTimeout());
        Assert.assertEquals(4000, pnConfiguration.getConnectTimeout());
        Assert.assertEquals(5000, pnConfiguration.getNonSubscribeRequestTimeout());
    }

    @Test(expected = PubNubException.class)
    public void testDecryptNull() throws PubNubException {
        pnConfiguration.setCipherKey("cipherKey");
        pubnub = new PubNub(pnConfiguration);
        Assert.assertEquals("test1", pubnub.decrypt(null).trim());
    }

    @Test(expected = PubNubException.class)
    public void testDecryptNull_B() throws PubNubException {
        pubnub = new PubNub(pnConfiguration);
        Assert.assertEquals("test1", pubnub.decrypt(null, "cipherKey").trim());
    }

    @Test
    public void getVersionAndTimeStamp() {
        pubnub = new PubNub(pnConfiguration);
        String version = pubnub.getVersion();
        int timeStamp = pubnub.getTimestamp();
        Assert.assertEquals("6.2.0", version);
        Assert.assertTrue(timeStamp > 0);
    }

    @Test(expected = PubNubException.class)
    public void testEcryptNull() throws PubNubException {
        pubnub = new PubNub(pnConfiguration);
        Assert.assertEquals("test1", pubnub.encrypt(null));
    }

    @Test(expected = PubNubException.class)
    public void testEcryptNull_B() throws PubNubException {
        pubnub = new PubNub(pnConfiguration);
        Assert.assertEquals("test1", pubnub.encrypt(null, "chiperKey"));
    }
}
