package com.pubnub.api.models.server.objects_vsp.space;

import lombok.Data;

@Data
public abstract class SpacePayload {
    private final String name;
    private final String description;
    private final Object custom;
    private final String status;
    private final String type;
}
