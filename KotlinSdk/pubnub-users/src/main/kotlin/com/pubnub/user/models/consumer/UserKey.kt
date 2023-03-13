package com.pubnub.user.models.consumer

import com.pubnub.apikt.models.consumer.objects.PNKey
import com.pubnub.apikt.models.consumer.objects.SortableKey

enum class UserKey : SortableKey {
    ID, NAME, UPDATED;

    fun toPNSortKey(): PNKey {
        return when (this) {
            ID -> PNKey.ID
            NAME -> PNKey.NAME
            UPDATED -> PNKey.UPDATED
        }
    }
}
