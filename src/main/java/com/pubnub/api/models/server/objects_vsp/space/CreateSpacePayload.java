package com.pubnub.api.models.server.objects_vsp.space;

public class CreateSpacePayload extends SpacePayload {
    public CreateSpacePayload(String name, String description, Object custom, String status, String type) {
        super(name, description, custom, status, type);
    }
}
