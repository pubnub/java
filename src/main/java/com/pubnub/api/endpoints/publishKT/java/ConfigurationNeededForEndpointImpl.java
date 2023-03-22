package com.pubnub.api.endpoints.publishKT.java;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.endpoints.publishKT.ConfigurationNeededForEndpoint;
import org.jetbrains.annotations.NotNull;

public class ConfigurationNeededForEndpointImpl implements ConfigurationNeededForEndpoint {
    private PNConfiguration pnConfiguration;
    private String pubNubVersion;

    public ConfigurationNeededForEndpointImpl(PNConfiguration pnConfiguration, String pubNubVersion) {
        this.pnConfiguration = pnConfiguration;
        this.pubNubVersion = pubNubVersion;
    }

    @NotNull
    @Override
    public String getSubscribeKey() {
        return pnConfiguration.getSubscribeKey();
    }

    @NotNull
    @Override
    public String getPublishKey() {
        return pnConfiguration.getPublishKey();
    }

    @NotNull
    @Override
    public String generatePnsdk() {
        return "PubNub-Java-Unified/".concat(pubNubVersion);
    }

    @NotNull
    @Override
    public String getUserIdValue() {
        return pnConfiguration.getUserId().getValue();
    }

    @Override
    public boolean includeInstanceIdentifier() {
        return false;
    }

    @Override
    public boolean includeRequestIdentifier() {
        return false;
    }

    @Override
    public boolean isAuthKeyValid() {
        return false;
    }

    @NotNull
    @Override
    public String getAuthKey() {
        return null;
    }
}
