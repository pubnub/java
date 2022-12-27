package com.pubnub.api.integration.managers.subscription;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;

import java.util.UUID;

import static com.pubnub.api.enums.PNLogVerbosity.BODY;
import static com.pubnub.api.enums.PNReconnectionPolicy.NONE;


public class ReconnectionProblemWithoutReconnectionPolicyIT extends AbstractReconnectionProblemIT {
    @Override
    protected PubNub privilegedClientPubNub() {
        PNConfiguration pnConfiguration;
        try {
            pnConfiguration = new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }
        pnConfiguration.setSubscribeKey(itPamTestConfig.pamSubKey());
        pnConfiguration.setPublishKey(itPamTestConfig.pamPubKey());
        pnConfiguration.setSubscribeTimeout(SUBSCRIBE_TIMEOUT);
        pnConfiguration.setLogVerbosity(BODY);
        pnConfiguration.setAuthKey(authKey);
        pnConfiguration.setReconnectionPolicy(NONE);
        return new PubNub(pnConfiguration);
    }
}
