package com.pubnub.api.models.consumer.objects_api.membership;

import com.google.gson.annotations.JsonAdapter;
import com.pubnub.api.models.consumer.objects_api.channel.PNChannelMetadata;
import com.pubnub.api.models.consumer.objects_api.util.CustomPayloadJsonInterceptor;
import com.pubnub.api.utils.UnwrapSingleField;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class PNMembership {
    @NonNull
    private PNChannelMetadata channel;

    @JsonAdapter(CustomPayloadJsonInterceptor.class)
    protected Object custom;

    @JsonAdapter(UnwrapSingleField.class)
    protected String uuid;

    protected String updated;
    protected String eTag;
    protected String status;
}
