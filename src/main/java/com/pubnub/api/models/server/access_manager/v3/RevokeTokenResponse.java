package com.pubnub.api.models.server.access_manager.v3;

import lombok.Data;

@Data
public class RevokeTokenResponse {

    private final int status;
    private final RevokeTokenData data;
    private final String service;

    @Data
    public static class RevokeTokenData {
        private final String message;
        private final String token;
    }
}

