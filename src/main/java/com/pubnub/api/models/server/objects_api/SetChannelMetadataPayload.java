package com.pubnub.api.models.server.objects_api;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
public class SetChannelMetadataPayload {
    private final String name;
    private final String description;
    private final Object custom;
    private final String status;
    private final String type;
}
