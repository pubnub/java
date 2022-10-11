package com.pubnub.api.models.consumer.objects_api.channel;

import com.pubnub.api.models.consumer.objects_api.PNObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PNChannelMetadata extends PNObject {
    private String name;
    private String description;
    private String type;
    private String status;

    public PNChannelMetadata(String id, String name, String description) {
        super(id);
        this.name = name;
        this.description = description;
    }

    public PNChannelMetadata(String id, String name) {
        this(id, name, null);
    }

    @Override
    public PNChannelMetadata setCustom(Object custom) {
        super.setCustom(custom);
        return this;
    }


}
