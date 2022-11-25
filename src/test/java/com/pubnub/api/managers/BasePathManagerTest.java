package com.pubnub.api.managers;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

public class BasePathManagerTest {

    private PNConfiguration pnConfiguration;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pnConfiguration =  new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
    }

    @Test
    public void stdOriginNotSecure() {
        pnConfiguration.setSecure(false);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("http://ps.pndsn.com", basePathManager.getBasePath());
    }

    @Test
    public void stdOriginSecure() {
        pnConfiguration.setSecure(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("https://ps.pndsn.com", basePathManager.getBasePath());
    }

    @Test
    public void customOriginNotSecure() {
        pnConfiguration.setOrigin("custom.origin.com");
        pnConfiguration.setSecure(false);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("http://custom.origin.com", basePathManager.getBasePath());
    }

    @Test
    public void customOriginSecure() {
        pnConfiguration.setOrigin("custom.origin.com");
        pnConfiguration.setSecure(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("https://custom.origin.com", basePathManager.getBasePath());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void customOriginNotSecureWithCacheBusting() {
        pnConfiguration.setOrigin("custom.origin.com");
        pnConfiguration.setCacheBusting(true);
        pnConfiguration.setSecure(false);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("http://custom.origin.com", basePathManager.getBasePath());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void customOriginSecureWithCacheBusting() {
        pnConfiguration.setOrigin("custom.origin.com");
        pnConfiguration.setSecure(true);
        pnConfiguration.setCacheBusting(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("https://custom.origin.com", basePathManager.getBasePath());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void cacheBustingNotSecure() {
        pnConfiguration.setCacheBusting(true);
        pnConfiguration.setSecure(false);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("http://ps1.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps2.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps3.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps4.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps5.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps6.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps7.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps8.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps9.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps10.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps11.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps12.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps13.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps14.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps15.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps16.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps17.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps18.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps19.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps20.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps1.pndsn.com", basePathManager.getBasePath());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void cacheBustingSecure() {
        pnConfiguration.setCacheBusting(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("https://ps1.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps2.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps3.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps4.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps5.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps6.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps7.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps8.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps9.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps10.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps11.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps12.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps13.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps14.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps15.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps16.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps17.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps18.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps19.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps20.pndsn.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps1.pndsn.com", basePathManager.getBasePath());
    }

}
