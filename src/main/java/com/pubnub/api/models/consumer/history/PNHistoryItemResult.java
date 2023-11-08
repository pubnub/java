package com.pubnub.api.models.consumer.history;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNubError;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class PNHistoryItemResult {

    private Long timetoken;
    private JsonElement entry;
    private JsonElement meta;
    /**
     * The error associated with message retrieval, if any. Can be null.
     * Currently, the only possible error is {@link com.pubnub.api.builder.PubNubErrorBuilder#PNERROBJ_PNERR_CRYPTO_IS_CONFIGURED_BUT_MESSAGE_IS_NOT_ENCRYPTED}
     * when the message was unencrypted, but PubNub instance is configured with a crypto module. In that case,
     * the unencrypted message content will still be available in {@code entry}.
     */
    private PubNubError error;
}
