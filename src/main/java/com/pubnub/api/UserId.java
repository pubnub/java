package com.pubnub.api;

import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERROBJ_USERID_NULL_OR_EMPTY;

@Data
public class UserId {

    private final String value;
    public UserId(@NotNull String value) throws PubNubException {
        PubNubUtil.require(value != null && !value.isEmpty(), PNERROBJ_USERID_NULL_OR_EMPTY);
        this.value = value;
    }
}
