package com.pubnub.api.models.consumer.objects_api.uuid;

import com.pubnub.api.models.consumer.objects_api.PNObject;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class PNUUIDMetadata extends PNObject {
    private String name;
    private String email;
    private String externalId;
    private String profileUrl;
    private String type;
    private String status;

    public PNUUIDMetadata(String id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public PNUUIDMetadata setCustom(Object custom) {
        super.setCustom(custom);
        return this;
    }
}

