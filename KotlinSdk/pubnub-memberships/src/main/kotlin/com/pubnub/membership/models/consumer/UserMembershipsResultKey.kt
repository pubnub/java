package com.pubnub.membership.models.consumer

import com.pubnub.apikt.models.consumer.objects.PNMembershipKey
import com.pubnub.apikt.models.consumer.objects.SortableKey

enum class UserMembershipsResultKey : SortableKey {
    SPACE_ID, SPACE_NAME, SPACE_UPDATED, UPDATED;

    fun toPNMembershipKey(): PNMembershipKey {
        return when (this) {
            SPACE_ID -> PNMembershipKey.CHANNEL_ID
            SPACE_NAME -> PNMembershipKey.CHANNEL_NAME
            SPACE_UPDATED -> PNMembershipKey.CHANNEL_UPDATED
            UPDATED -> PNMembershipKey.UPDATED
        }
    }
}
