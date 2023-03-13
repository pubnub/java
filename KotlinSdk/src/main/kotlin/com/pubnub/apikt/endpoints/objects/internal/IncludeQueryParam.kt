package com.pubnub.apikt.endpoints.objects.internal

import com.pubnub.apikt.models.consumer.objects.member.PNUUIDDetailsLevel
import com.pubnub.apikt.models.consumer.objects.membership.PNChannelDetailsLevel

data class IncludeQueryParam(
    private val includeCustom: Boolean = false,
    private val includeChannelDetails: PNChannelDetailsLevel? = null,
    private val includeUUIDDetails: PNUUIDDetailsLevel? = null,
    private val includeType: Boolean = true,
    private val includeStatus: Boolean = true
) {
    internal fun createIncludeQueryParams(): Map<String, String> {
        val includeList = mutableListOf<String>()
        if (includeCustom) includeList.add("custom")
        when (includeChannelDetails) {
            PNChannelDetailsLevel.CHANNEL -> includeList.add("channel")
            PNChannelDetailsLevel.CHANNEL_WITH_CUSTOM -> includeList.add("channel.custom")
            null -> {}
        }
        when (includeUUIDDetails) {
            PNUUIDDetailsLevel.UUID -> includeList.add("uuid")
            PNUUIDDetailsLevel.UUID_WITH_CUSTOM -> includeList.add("uuid.custom")
            null -> {}
        }
        if (includeType) includeList.add("type")
        if (includeStatus) includeList.add("status")
        return if (includeList.isNotEmpty()) {
            mapOf("include" to includeList.joinToString(","))
        } else {
            mapOf()
        }
    }
}
