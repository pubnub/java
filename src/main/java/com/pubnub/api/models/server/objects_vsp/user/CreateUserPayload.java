package com.pubnub.api.models.server.objects_vsp.user;

public class CreateUserPayload extends UserPayload {
    public CreateUserPayload(String name, String email, String externalId, String profileUrl, Object custom, String status, String type) {
        super(name, email, externalId, profileUrl, custom, status, type);
    }
}
