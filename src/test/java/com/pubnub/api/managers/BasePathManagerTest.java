package com.pubnub.api.managers;

import com.pubnub.api.PNConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class BasePathManagerTest {

    PNConfiguration pnConfiguration;

    @Before
    public void beforeEach() throws IOException {
        pnConfiguration = new PNConfiguration();
    }

    @Test
    public void stdOriginNotSecure(){
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("http://pubsub.pubnub.com", basePathManager.getBasePath());
    }

    @Test
    public void stdOriginSecure(){
        pnConfiguration.setSecure(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("https://pubsub.pubnub.com", basePathManager.getBasePath());
    }

    @Test
    public void customOriginNotSecure(){
        pnConfiguration.setOrigin("custom.origin.com");
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("http://custom.origin.com", basePathManager.getBasePath());
    }

    @Test
    public void customOriginSecure(){
        pnConfiguration.setOrigin("custom.origin.com");
        pnConfiguration.setSecure(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("https://custom.origin.com", basePathManager.getBasePath());
    }

    @Test
    public void customOriginNotSecureWithCacheBusting(){
        pnConfiguration.setOrigin("custom.origin.com");
        pnConfiguration.setCacheBusting(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("http://custom.origin.com", basePathManager.getBasePath());
    }

    @Test
    public void customOriginSecureWithCacheBusting(){
        pnConfiguration.setOrigin("custom.origin.com");
        pnConfiguration.setSecure(true);
        pnConfiguration.setCacheBusting(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("https://custom.origin.com", basePathManager.getBasePath());
    }

    @Test
    public void cacheBustingNotSecure(){
        pnConfiguration.setCacheBusting(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("http://ps1.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps2.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps3.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps4.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps5.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps6.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps7.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps8.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps9.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps10.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps11.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps12.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps13.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps14.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps15.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps16.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps17.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps18.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps19.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps20.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("http://ps1.pubnub.com", basePathManager.getBasePath());
    }

    @Test
    public void cacheBustingSecure(){;
        pnConfiguration.setSecure(true);
        pnConfiguration.setCacheBusting(true);
        BasePathManager basePathManager = new BasePathManager(pnConfiguration);
        Assert.assertEquals("https://ps1.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps2.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps3.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps4.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps5.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps6.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps7.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps8.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps9.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps10.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps11.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps12.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps13.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps14.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps15.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps16.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps17.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps18.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps19.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps20.pubnub.com", basePathManager.getBasePath());
        Assert.assertEquals("https://ps1.pubnub.com", basePathManager.getBasePath());
    }

}
