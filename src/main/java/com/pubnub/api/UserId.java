package com.pubnub.api;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_USERID_NULL_OR_EMPTY;

public class UserId {

    @Getter
    private final String value;

    public UserId(@NotNull String value) throws PubNubException {
        PubNubUtil.require(value != null && !value.isEmpty(), PNERROBJ_USERID_NULL_OR_EMPTY);
        this.value = value;
    }
}
