package com.pubnub.api.integration.objects;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.integration.util.ITTestConfig;
import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assume.assumeThat;

public abstract class ObjectsApiBaseIT {
    //See README.md in integrationTest directory for more info on running integration tests
    private ITTestConfig itTestConfig = ConfigFactory.create(ITTestConfig.class, System.getenv());
    public static final String STATUS_ACTIVE = "Active";
    public static final String TYPE_HUMAN = "Human";

    protected final PubNub pubNubUnderTest = pubNub();

    private PubNub pubNub() {
        PNConfiguration pnConfiguration;
        try {
            pnConfiguration = new PNConfiguration(PubNub.generateUUID());
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }
        pnConfiguration.setSubscribeKey(itTestConfig.subscribeKey());
        pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);

        return new PubNub(pnConfiguration);
    }

    @Before
    public void assumeTestsAreConfiguredProperly() {
        assumeThat("Subscription key must be set in test.properties", itTestConfig.subscribeKey(), not(isEmptyOrNullString()));
    }

    protected String randomName() {
        return RandomStringUtils.randomAlphabetic(5, 10) + " " + RandomStringUtils.randomAlphabetic(5, 10);
    }

    protected Map<String, Object> customUserObject() {
        return getCustomObject();
    }

    protected Map<String, Object> customSpaceObject() {
        return getCustomObject();
    }

    private Map<String, Object> getCustomObject(){
        final Map<String, Object> customMap = new HashMap<>();
        customMap.putIfAbsent("param1", "val1");
        customMap.putIfAbsent("param2", "val2");
        return customMap;
    }

    protected Map<String, Object> updatedCustomUserObject() {
        return updatedCustomObject();
    }

    protected Map<String, Object> updatedCustomSpaceObject() {
        return updatedCustomObject();
    }

    private Map<String, Object> updatedCustomObject() {
        final Map<String, Object> customMap = new HashMap<>();
        customMap.putIfAbsent("param1", "val1_updated");
        customMap.putIfAbsent("param2", "val2_updated");
        customMap.putIfAbsent("param3", "added");
        return customMap;
    }
}
