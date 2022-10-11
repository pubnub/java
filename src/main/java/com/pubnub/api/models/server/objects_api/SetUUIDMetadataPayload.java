package com.pubnub.api.models.server.objects_api;

import lombok.*;

@Getter
@ToString
@AllArgsConstructor
public class SetUUIDMetadataPayload {
    private final String name;
    private final String email;
    private final String externalId;
    private final String profileUrl;
    private final Object custom;
    private String status;
    private String type;

}
