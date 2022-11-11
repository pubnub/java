package com.pubnub.api.models.consumer.objects_vsp.user;

import com.google.gson.JsonElement;
import com.pubnub.api.models.server.objects_api.EntityEnvelope;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter(AccessLevel.PACKAGE)
@ToString(callSuper = true)
@NoArgsConstructor
public class RemoveUserResult extends EntityEnvelope<JsonElement> {
    public RemoveUserResult(final EntityEnvelope<JsonElement> envelope) {
        this.status = envelope.getStatus();
    }
}
