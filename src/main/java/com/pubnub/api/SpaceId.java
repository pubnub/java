package com.pubnub.api;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_SPACEID_NULL_OR_EMPTY;

public class SpaceId {

    @Getter
    private final String value;

    public SpaceId(@NotNull String value) throws PubNubException {
        PubNubUtil.require(value != null && !value.isEmpty(), PNERROBJ_SPACEID_NULL_OR_EMPTY);
        this.value = value;
    }
}
