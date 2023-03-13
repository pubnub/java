package com.pubnub.contract.access.parameter

import com.pubnub.apikt.models.consumer.access_manager.v3.PNToken
import io.cucumber.java.ParameterType

enum class ResourceType {
    CHANNEL,
    CHANNEL_GROUP,
    UUID
}

@ParameterType(".*")
fun resourceType(name: String): ResourceType {
    return ResourceType.valueOf(name)
}

fun PNToken.resourcePermissionsMap(resourceType: ResourceType): Map<String, PNToken.PNResourcePermissions> {
    return when (resourceType) {
        ResourceType.CHANNEL -> resources.channels
        ResourceType.CHANNEL_GROUP -> resources.channelGroups
        ResourceType.UUID -> resources.uuids
    }
}

fun PNToken.patternPermissionsMap(resourceType: ResourceType): Map<String, PNToken.PNResourcePermissions> {
    return when (resourceType) {
        ResourceType.CHANNEL -> patterns.channels
        ResourceType.CHANNEL_GROUP -> patterns.channelGroups
        ResourceType.UUID -> patterns.uuids
    }
}
