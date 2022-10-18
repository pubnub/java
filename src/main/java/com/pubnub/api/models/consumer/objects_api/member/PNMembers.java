package com.pubnub.api.models.consumer.objects_api.member;

import com.google.gson.annotations.JsonAdapter;
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata;
import com.pubnub.api.models.consumer.objects_api.util.CustomPayloadJsonInterceptor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class PNMembers {
    private PNUUIDMetadata uuid;

    @JsonAdapter(CustomPayloadJsonInterceptor.class)
    protected Object custom;

    protected String updated;
    protected String eTag;
    protected String status;
}
