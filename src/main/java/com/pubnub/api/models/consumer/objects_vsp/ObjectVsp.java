package com.pubnub.api.models.consumer.objects_vsp;

import com.google.gson.annotations.JsonAdapter;
import com.pubnub.api.models.consumer.objects_api.util.CustomPayloadJsonInterceptor;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class ObjectVsp {

    @JsonAdapter(CustomPayloadJsonInterceptor.class)
    @Setter
    protected Map<String, Object> custom;

    protected String updated;
    protected String eTag;
}
