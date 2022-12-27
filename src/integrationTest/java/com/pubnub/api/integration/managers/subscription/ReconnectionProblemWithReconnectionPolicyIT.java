package com.pubnub.api.integration.managers.subscription;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.enums.PNLogVerbosity;

import static com.pubnub.api.enums.PNReconnectionPolicy.LINEAR;


public class ReconnectionProblemWithReconnectionPolicyIT extends AbstractReconnectionProblemIT {
    @Override
    protected PubNub privilegedClientPubNub() {
        PNConfiguration pnConfiguration = getPNConfiguration();
        pnConfiguration.setSubscribeKey(itPamTestConfig.pamSubKey());
        pnConfiguration.setPublishKey(itPamTestConfig.pamPubKey());
        pnConfiguration.setSubscribeTimeout(5);
        pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);
        pnConfiguration.setReconnectionPolicy(LINEAR);
        pnConfiguration.setAuthKey(authKey);
        return new PubNub(pnConfiguration);
    }
}
