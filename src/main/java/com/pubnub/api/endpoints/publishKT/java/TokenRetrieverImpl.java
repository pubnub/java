package com.pubnub.api.endpoints.publishKT.java;

import com.pubnub.api.endpoints.publishKT.TokenRetriever;
import com.pubnub.api.managers.token_manager.TokenManager;
import org.jetbrains.annotations.Nullable;

public class TokenRetrieverImpl implements TokenRetriever {
    private final TokenManager tokenManager;

    public TokenRetrieverImpl(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Nullable
    @Override
    public String getToken() {
        return tokenManager.getToken();
    }
}
