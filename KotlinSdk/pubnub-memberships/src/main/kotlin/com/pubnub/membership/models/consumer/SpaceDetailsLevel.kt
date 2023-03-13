package com.pubnub.membership.models.consumer

import com.pubnub.apikt.models.consumer.objects.membership.PNChannelDetailsLevel

enum class SpaceDetailsLevel {
    SPACE,
    SPACE_WITH_CUSTOM
}

internal fun SpaceDetailsLevel.toPNChannelDetailsLevel(): PNChannelDetailsLevel {
    return when (this) {
        SpaceDetailsLevel.SPACE -> PNChannelDetailsLevel.CHANNEL
        SpaceDetailsLevel.SPACE_WITH_CUSTOM -> PNChannelDetailsLevel.CHANNEL_WITH_CUSTOM
    }
}
