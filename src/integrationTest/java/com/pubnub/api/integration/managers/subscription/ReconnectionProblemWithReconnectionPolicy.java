package com.pubnub.api.integration.managers.subscription;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.enums.PNLogVerbosity;

import static com.pubnub.api.enums.PNReconnectionPolicy.LINEAR;


public class ReconnectionProblemWithReconnectionPolicy extends AbstractReconnectionProblem {
    @Override
    protected PubNub privilegedClientPubNub() {
        PNConfiguration pnConfiguration = null;
        try {
            pnConfiguration = new PNConfiguration(PubNub.generateUUID());
        } catch (PubNubException e) {
            throw new RuntimeException(e);
        }
        pnConfiguration.setSubscribeKey(itPamTestConfig.pamSubKey());
        pnConfiguration.setPublishKey(itPamTestConfig.pamPubKey());
        pnConfiguration.setSubscribeTimeout(5);
        pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);
        pnConfiguration.setReconnectionPolicy(LINEAR);
        pnConfiguration.setAuthKey(authKey);
        return new PubNub(pnConfiguration);
    }
}
