package com.pubnub.api.models.consumer.objects_vsp.space;

import com.google.gson.annotations.JsonAdapter;
import com.pubnub.api.SpaceId;
import com.pubnub.api.models.consumer.objects_vsp.ObjectVsp;
import com.pubnub.api.models.consumer.objects_vsp.util.SpaceIdDeserializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.Map;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@Data
public class Space extends ObjectVsp {

    @EqualsAndHashCode.Include
    @JsonAdapter(SpaceIdDeserializer.class)
    @Setter
    private SpaceId id;
    private String name;
    private String description;
    private String status;
    private String type;

    @Override
    public Space setCustom(Map<String, Object> custom) {
        super.setCustom(custom);
        return this;
    }
}
