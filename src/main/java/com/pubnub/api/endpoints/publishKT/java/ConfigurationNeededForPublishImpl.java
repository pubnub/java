package com.pubnub.api.endpoints.publishKT.java;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.endpoints.publishKT.ConfigurationNeededForPublish;
import org.jetbrains.annotations.NotNull;

public class ConfigurationNeededForPublishImpl implements ConfigurationNeededForPublish {
    private PNConfiguration pnConfiguration;

    public ConfigurationNeededForPublishImpl(PNConfiguration pnConfiguration) {
        this.pnConfiguration = pnConfiguration;
    }

    @Override
    public boolean useEncryption() {
        return pnConfiguration.getCipherKey() != null && !pnConfiguration.getCipherKey().isEmpty();
    }

    @NotNull
    @Override
    public String getPublishKey() {
        return pnConfiguration.getPublishKey();
    }

    @NotNull
    @Override
    public String getSubscribeKey() {
        return pnConfiguration.getSubscribeKey();
    }

    @NotNull
    @Override
    public String getCipherKey() {
        return pnConfiguration.getCipherKey();
    }

    @Override
    public boolean useRandomInitializationVector() {
        return pnConfiguration.isUseRandomInitializationVector();
    }
}
