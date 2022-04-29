package com.pubnub.api.integration.objects;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.integration.util.ITTestConfig;
import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;

public abstract class ObjectsApiBaseIT {
    //See README.md in integrationTest directory for more info on running integration tests
    private ITTestConfig itTestConfig = ConfigFactory.create(ITTestConfig.class, System.getenv());

    protected final PubNub pubNubUnderTest = pubNub();

    private PubNub pubNub() {
        final PNConfiguration pnConfiguration = new PNConfiguration(PubNub.generateUUID());
        pnConfiguration.setSubscribeKey(itTestConfig.subscribeKey());
        pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);

        return new PubNub(pnConfiguration);
    }

    @Before
    public void assumeTestsAreConfiguredProperly() {
        assumeThat("Subscription key must be set in test.properties", itTestConfig.subscribeKey(), not(isEmptyOrNullString()));
    }
}
