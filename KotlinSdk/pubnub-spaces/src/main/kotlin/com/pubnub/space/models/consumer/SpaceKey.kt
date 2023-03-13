package com.pubnub.space.models.consumer

import com.pubnub.apikt.models.consumer.objects.PNKey
import com.pubnub.apikt.models.consumer.objects.SortableKey

enum class SpaceKey() : SortableKey {
    ID(), NAME(), UPDATED();

    fun toPNSortKey(): PNKey {
        return when (this) {
            ID -> PNKey.ID
            NAME -> PNKey.NAME
            UPDATED -> PNKey.UPDATED
        }
    }
}
