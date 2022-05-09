package com.pubnub.api.integration.managers.subscription;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;

import static com.pubnub.api.enums.PNLogVerbosity.BODY;
import static com.pubnub.api.enums.PNReconnectionPolicy.NONE;


public class ReconnectionProblemWithoutReconnectionPolicy extends AbstractReconnectionProblem {
    @Override
    protected PubNub privilegedClientPubNub() {
        PNConfiguration pnConfiguration;
        try {
            pnConfiguration = new PNConfiguration(PubNub.generateUUID());
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
