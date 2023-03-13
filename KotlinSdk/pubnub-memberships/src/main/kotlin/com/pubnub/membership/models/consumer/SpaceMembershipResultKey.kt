package com.pubnub.membership.models.consumer

import com.pubnub.apikt.models.consumer.objects.PNMemberKey
import com.pubnub.apikt.models.consumer.objects.SortableKey

enum class SpaceMembershipResultKey : SortableKey {
    USER_ID, USER_NAME, USER_UPDATED, UPDATED;

    fun toPNMemberKey(): PNMemberKey {
        return when (this) {
            USER_ID -> PNMemberKey.UUID_ID
            USER_NAME -> PNMemberKey.UUID_NAME
            USER_UPDATED -> PNMemberKey.UUID_UPDATED
            UPDATED -> PNMemberKey.UPDATED
        }
    }
}
