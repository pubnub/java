package com.pubnub.apikt.models.server.objects_api

internal data class ChangeMembershipInput(
    val set: List<ServerMembershipInput> = listOf(),
    val delete: List<ServerMembershipInput> = listOf()
)
