package com.pubnub.api;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_SPACEID_NULL_OR_EMPTY;

@Data
public class SpaceId {

    @NotNull
    private final String value;

    public SpaceId(@NotNull String value) {
        PubNubUtil.require(value != null && !value.isEmpty(), PNERROBJ_SPACEID_NULL_OR_EMPTY);
        this.value = value;
    }
}
