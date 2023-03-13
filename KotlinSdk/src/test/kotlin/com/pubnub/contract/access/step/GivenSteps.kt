package com.pubnub.contract.access.step

import com.pubnub.apikt.models.consumer.access_manager.v3.*
import com.pubnub.contract.access.parameter.PermissionType
import com.pubnub.contract.access.parameter.ResourceType
import com.pubnub.contract.access.state.FutureCallGrant
import com.pubnub.contract.access.state.GrantTokenState
import com.pubnub.contract.state.World
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import kotlin.random.Random

class GivenSteps(private val grantTokenState: GrantTokenState, private val world: World) {
    private val tokenWithAll =
        "qEF2AkF0GmEI03xDdHRsGDxDcmVzpURjaGFuoWljaGFubmVsLTEY70NncnChb2NoYW5uZWxfZ3JvdXAtMQVDdXNyoENzcGOgRHV1aWShZnV1aWQtMRhoQ3BhdKVEY2hhbqFtXmNoYW5uZWwtXFMqJBjvQ2dycKF0XjpjaGFubmVsX2dyb3VwLVxTKiQFQ3VzcqBDc3BjoER1dWlkoWpedXVpZC1cUyokGGhEbWV0YaBEdXVpZHR0ZXN0LWF1dGhvcml6ZWQtdXVpZENzaWdYIPpU-vCe9rkpYs87YUrFNWkyNq8CVvmKwEjVinnDrJJc"

    @Given("I have a known token containing UUID pattern Permissions")
    fun i_have_a_known_token_containing_uuid_pattern_permissions() {
        grantTokenState.result = PNGrantTokenResult(tokenWithAll)
    }

    @Given("I have a known token containing UUID resource permissions")
    fun i_have_a_known_token_containing_uuid_resource_permissions() {
        grantTokenState.result = PNGrantTokenResult(tokenWithAll)
    }

    @Given("I have a known token containing an authorized UUID")
    fun i_have_a_known_token_containing_an_authorized_uuid() {
        grantTokenState.result = PNGrantTokenResult(tokenWithAll)
    }

    @Given("the {string} {resourceType} pattern access permissions")
    fun the_channel_pattern_access_permissions(pattern: String, resourceType: ResourceType) {
        when (resourceType) {
            ResourceType.CHANNEL -> grantTokenState.currentGrant = FutureCallGrant(ChannelGrant.pattern(pattern))
            ResourceType.CHANNEL_GROUP ->
                grantTokenState.currentGrant =
                    FutureCallGrant(ChannelGroupGrant.pattern(pattern))
            ResourceType.UUID -> grantTokenState.currentGrant = FutureCallGrant(UUIDGrant.pattern(pattern))
        }
    }

    @Given("the {string} {resourceType} resource access permissions")
    fun the_channel_resource_access_permissions(name: String, resourceType: ResourceType) {
        when (resourceType) {
            ResourceType.CHANNEL -> grantTokenState.currentGrant = FutureCallGrant(ChannelGrant.name(name))
            ResourceType.CHANNEL_GROUP -> grantTokenState.currentGrant = FutureCallGrant(ChannelGroupGrant.id(name))
            ResourceType.UUID -> grantTokenState.currentGrant = FutureCallGrant(UUIDGrant.id(name))
        }
    }

    @Given("the TTL {ttl}")
    fun the_ttl(ttl: Long) {
        grantTokenState.TTL = ttl
    }

    @And("grant resource permission {permissionType}")
    fun grant_resource_permission(permissionType: PermissionType) {
        grant_permission(permissionType)
    }

    @And("grant pattern permission {permissionType}")
    fun grant_pattern_permission(permissionType: PermissionType) {
        grant_permission(permissionType)
    }

    @Given("deny resource permission GET")
    fun deny_resource_permission_get() {
        // in grant token everything is denied by default
    }

    @Given("a token")
    fun a_token() {
        val characters: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '-'
        world.tokenString =
            (1..30).map { Random.nextInt(0, characters.size) }.map { characters[it] }.joinToString("")
    }

    @Given("a valid token with permissions to publish with channel {string}")
    fun a_valid_token_with_permissions_to_publish_with_channel(@Suppress("UNUSED_PARAMETER") string: String?) {
        return a_token()
    }

    @Given("an expired token with permissions to publish with channel {string}")
    fun an_expired_token_with_permissions_to_publish_with_channel(@Suppress("UNUSED_PARAMETER") string: String) {
        return a_token()
    }

    @Given("the token string {string}")
    fun the_token_string(string: String) {
        world.tokenString = string
    }

    private fun grant_permission(permissionType: PermissionType) {
        when (permissionType) {
            PermissionType.READ ->
                grantTokenState.currentGrant?.let { futureCall ->
                    futureCall.addAction {
                        when (it) {
                            is PNChannelPatternGrant -> {
                                it.copy(read = true)
                            }
                            is PNChannelResourceGrant -> {
                                it.copy(read = true)
                            }
                            is PNChannelGroupPatternGrant -> {
                                it.copy(read = true)
                            }
                            is PNChannelGroupResourceGrant -> {
                                it.copy(read = true)
                            }
                            else -> throw RuntimeException()
                        }
                    }
                }
            PermissionType.WRITE -> grantTokenState.currentGrant?.let { futureCall ->
                futureCall.addAction {
                    when (it) {
                        is PNChannelResourceGrant -> {
                            it.copy(write = true)
                        }
                        is PNChannelPatternGrant -> {
                            it.copy(write = true)
                        }
                        else -> throw RuntimeException()
                    }
                }
            }
            PermissionType.GET -> grantTokenState.currentGrant?.let { futureCall ->
                futureCall.addAction {
                    when (it) {
                        is PNChannelResourceGrant -> {
                            it.copy(get = true)
                        }
                        is PNChannelPatternGrant -> {
                            it.copy(get = true)
                        }
                        is PNUUIDResourceGrant -> {
                            it.copy(get = true)
                        }
                        is PNUUIDPatternGrant -> {
                            it.copy(get = true)
                        }
                        else -> throw RuntimeException()
                    }
                }
            }
            PermissionType.MANAGE -> grantTokenState.currentGrant?.let { futureCall ->
                futureCall.addAction {
                    when (it) {
                        is PNChannelPatternGrant -> {
                            it.copy(manage = true)
                        }
                        is PNChannelResourceGrant -> {
                            it.copy(manage = true)
                        }
                        is PNChannelGroupPatternGrant -> {
                            it.copy(manage = true)
                        }
                        is PNChannelGroupResourceGrant -> {
                            it.copy(manage = true)
                        }
                        else -> throw RuntimeException()
                    }
                }
            }
            PermissionType.UPDATE -> grantTokenState.currentGrant?.let { futureCall ->
                futureCall.addAction {
                    when (it) {
                        is PNChannelResourceGrant -> {
                            it.copy(update = true)
                        }
                        is PNChannelPatternGrant -> {
                            it.copy(update = true)
                        }
                        is PNUUIDResourceGrant -> {
                            it.copy(update = true)
                        }
                        is PNUUIDPatternGrant -> {
                            it.copy(update = true)
                        }
                        else -> throw RuntimeException()
                    }
                }
            }
            PermissionType.JOIN -> grantTokenState.currentGrant?.let { futureCall ->
                futureCall.addAction {
                    when (it) {
                        is PNChannelResourceGrant -> {
                            it.copy(join = true)
                        }
                        is PNChannelPatternGrant -> {
                            it.copy(join = true)
                        }
                        else -> throw RuntimeException()
                    }
                }
            }

            PermissionType.DELETE -> grantTokenState.currentGrant?.let { futureCall ->
                futureCall.addAction {
                    when (it) {
                        is PNChannelResourceGrant -> {
                            it.copy(delete = true)
                        }
                        is PNChannelPatternGrant -> {
                            it.copy(delete = true)
                        }
                        is PNUUIDResourceGrant -> {
                            it.copy(delete = true)
                        }
                        is PNUUIDPatternGrant -> {
                            it.copy(delete = true)
                        }
                        else -> throw RuntimeException()
                    }
                }
            }
        }
    }
}
