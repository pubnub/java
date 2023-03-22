package com.pubnub.api.endpoints.publishKT.java;

import com.pubnub.api.endpoints.publishKT.PNInstanceIdProvider;
import org.jetbrains.annotations.NotNull;

public class PNInstanceIdProviderImpl implements PNInstanceIdProvider {
    private final String pnInstanceId;

    public PNInstanceIdProviderImpl(String pnInstanceId) {
        this.pnInstanceId = pnInstanceId;
    }

    @NotNull
    @Override
    public String getPNInstanceId() {
        return pnInstanceId;
    }
}
