package com.pubnub.api.endpoints;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.enums.PNLogVerbosity;

import java.util.UUID;

public class TestHarness {
    protected final static int PORT = 8080;

    protected PubNub createPubNubInstance() throws PubNubException {
        PNConfiguration pnConfiguration = new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
        pnConfiguration.setOrigin("localhost" + ":" + PORT);
        pnConfiguration.setSecure(false);
        pnConfiguration.setSubscribeKey("mySubscribeKey");
        pnConfiguration.setPublishKey("myPublishKey");
        pnConfiguration.setUserId(new UserId("myUUID"));
        pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);
        pnConfiguration.setUseRandomInitializationVector(false);

        class MockedTimePubNub extends PubNub {

            public MockedTimePubNub(PNConfiguration initialConfig) {
                super(initialConfig);
            }

            @Override
            public int getTimestamp() {
                return 1337;
            }

            @Override
            public String getVersion() {
                return "suchJava";
            }

            @Override
            public String getInstanceId() {
                return "PubNubInstanceId";
            }

            @Override
            public String getRequestId() {
                return "PubNubRequestId";
            }

        }

        return new MockedTimePubNub(pnConfiguration);
    }
}
