package com.pubnub.apikt.models.consumer.access_manager.v3

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.pubnub.apikt.models.TokenBitmask

data class PNToken(
    @JsonProperty("v") val version: Int = 0,
    @JsonProperty("t") val timestamp: Long = 0,
    @JsonProperty("ttl") val ttl: Long = 0,
    @JsonProperty("uuid") val authorizedUUID: String? = null,
    @JsonProperty("res") val resources: PNTokenResources,
    @JsonProperty("pat") val patterns: PNTokenResources,
    @JsonProperty("meta") val meta: Any? = null
) {

    data class PNTokenResources(
        @JsonProperty("chan") val channels: Map<String, PNResourcePermissions> = emptyMap(),
        @JsonProperty("grp") val channelGroups: Map<String, PNResourcePermissions> = emptyMap(),
        @JsonProperty("uuid") val uuids: Map<String, PNResourcePermissions> = emptyMap()
    )

    data class PNResourcePermissions(
        val read: Boolean = false,
        val write: Boolean = false,
        val manage: Boolean = false,
        val delete: Boolean = false,
        val get: Boolean = false,
        val update: Boolean = false,
        val join: Boolean = false
    ) {

        @JsonCreator
        constructor(grant: Int) : this(
            grant and TokenBitmask.READ != 0,
            grant and TokenBitmask.WRITE != 0,
            grant and TokenBitmask.MANAGE != 0,
            grant and TokenBitmask.DELETE != 0,
            grant and TokenBitmask.GET != 0,
            grant and TokenBitmask.UPDATE != 0,
            grant and TokenBitmask.JOIN != 0
        )
    }
}
