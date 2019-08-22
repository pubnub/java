package com.pubnub.api.endpoints.objects_api.users.read;

import com.pubnub.api.endpoints.objects_api.users.PNUser;
import com.pubnub.api.models.server.objects_api.EntityArrayEnvelope;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter(AccessLevel.PACKAGE)
public class PNGetUsersResult extends EntityArrayEnvelope<PNUser> {

    static PNGetUsersResult create(EntityArrayEnvelope<PNUser> envelope) {
        PNGetUsersResult result = new PNGetUsersResult();
        result.totalCount = envelope.getTotalCount();
        result.next = envelope.getNext();
        result.prev = envelope.getPrev();
        result.data = envelope.getData();
        return result;
    }

    static PNGetUsersResult create() {
        return new PNGetUsersResult();
    }
}
