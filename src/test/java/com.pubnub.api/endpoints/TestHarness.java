package com.pubnub.api.endpoints;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

public class TestHarness {

    protected PubNub createPubNubInstance(int port) {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setOrigin("localhost" + ":" + port);
        pnConfiguration.setSecure(false);
        pnConfiguration.setSubscribeKey("mySubscribeKey");
        pnConfiguration.setPublishKey("myPublishKey");
        pnConfiguration.setUuid("myUUID");

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

        }

        return new MockedTimePubNub(pnConfiguration);
    }
}
