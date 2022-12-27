package com.pubnub.contract.access.step

import com.pubnub.api.models.consumer.access_manager.v3.ChannelGrant
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGroupGrant
import com.pubnub.api.models.consumer.access_manager.v3.PNGrantTokenResult
import com.pubnub.api.models.consumer.access_manager.v3.UUIDGrant
import com.pubnub.contract.access.parameter.PermissionType
import com.pubnub.contract.access.parameter.ResourceType
import com.pubnub.contract.access.state.GrantTokenState
import com.pubnub.contract.state.World
import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import kotlin.random.Random

class GivenSteps(private val grantTokenState: GrantTokenState,
                 private val world: World) {
    private val tokenWithAll = "qEF2AkF0GmEI03xDdHRsGDxDcmVzpURjaGFuoWljaGFubmVsLTEY70NncnChb2NoYW5uZWxfZ3JvdXAtMQVDdXNyoENzcGOgRHV1aWShZnV1aWQtMRhoQ3BhdKVEY2hhbqFtXmNoYW5uZWwtXFMqJBjvQ2dycKF0XjpjaGFubmVsX2dyb3VwLVxTKiQFQ3VzcqBDc3BjoER1dWlkoWpedXVpZC1cUyokGGhEbWV0YaBEdXVpZHR0ZXN0LWF1dGhvcml6ZWQtdXVpZENzaWdYIPpU-vCe9rkpYs87YUrFNWkyNq8CVvmKwEjVinnDrJJc"

    @Given("I have a known token containing UUID pattern Permissions")
    fun i_have_a_known_token_containing_uuid_pattern_permissions() {
        // Write code here that turns the phrase above into concrete actions
        grantTokenState.result = PNGrantTokenResult(tokenWithAll)
    }

    @Given("I have a known token containing UUID resource permissions")
    fun i_have_a_known_token_containing_uuid_resource_permissions() {
        // Write code here that turns the phrase above into concrete actions
        grantTokenState.result = PNGrantTokenResult(tokenWithAll)
    }

    @Given("I have a known token containing an authorized UUID")
    fun i_have_a_known_token_containing_an_authorized_uuid() {
        // Write code here that turns the phrase above into concrete actions
        grantTokenState.result = PNGrantTokenResult(tokenWithAll)
    }

    @Given("the {string} {resourceType} pattern access permissions")
    fun the_channel_pattern_access_permissions(pattern: String, resourceType: ResourceType) {
        when (resourceType) {
            ResourceType.CHANNEL -> grantTokenState.currentGrant = ChannelGrant.pattern(pattern)
            ResourceType.CHANNEL_GROUP -> grantTokenState.currentGrant = ChannelGroupGrant.pattern(pattern)
            ResourceType.UUID -> grantTokenState.currentGrant = UUIDGrant.pattern(pattern)
        }
    }


    @Given("the {string} {resourceType} resource access permissions")
    fun the_channel_resource_access_permissions(name: String, resourceType: ResourceType) {
        when (resourceType) {
            ResourceType.CHANNEL -> grantTokenState.currentGrant = ChannelGrant.name(name)
            ResourceType.CHANNEL_GROUP -> grantTokenState.currentGrant = ChannelGroupGrant.id(name)
            ResourceType.UUID -> grantTokenState.currentGrant = UUIDGrant.id(name)
        }

    }

    @Given("the TTL {ttl}")
    fun the_ttl(ttl: Long) {
        grantTokenState.TTL = ttl
    }

    @Given("deny resource permission GET")
    fun deny_resource_permission_get() {
        //in grant token everything is denied by default
    }

    @And("grant resource permission {permissionType}")
    fun grant_resource_permission(permissionType: PermissionType) {
        grant_permission(permissionType)
    }

    @And("grant pattern permission {permissionType}")
    fun grant_pattern_permission(permissionType: PermissionType) {
        grant_permission(permissionType)
    }

    @Given("a token")
    fun a_token() {
        val characters: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9') + '_'
        world.tokenString =
                (1..30).map { Random.nextInt(0, characters.size) }.map { characters[it] }.joinToString("")
        world.tokenString = "A8TlaF6egF9dE6a0DzVpnEyftEt7Nl"
    }

    @Given("a valid token with permissions to publish with channel {string}")
    fun a_valid_token_with_permissions_to_publish_with_channel(@Suppress("UNUSED_PARAMETER")string: String?) {
        return a_token()
    }

    @Given("an expired token with permissions to publish with channel {string}")
    fun an_expired_token_with_permissions_to_publish_with_channel(@Suppress("UNUSED_PARAMETER")string: String) {
        return a_token()
    }

    @Given("the token string {string}")
    fun the_token_string(string: String) {
        world.tokenString = string
    }

    private fun grant_permission(permissionType: PermissionType) {
        when (permissionType) {
            PermissionType.READ -> when (val currentGrant = grantTokenState.currentGrant) {
                is ChannelGrant -> {
                    currentGrant.read()
                }
                is ChannelGroupGrant -> {
                    currentGrant.read()
                }
            }
            PermissionType.WRITE -> when (val currentGrant = grantTokenState.currentGrant) {
                is ChannelGrant -> {
                    currentGrant.write()
                }
            }
            PermissionType.GET -> when (val currentGrant = grantTokenState.currentGrant) {
                is ChannelGrant -> {
                    currentGrant.get()
                }
                is UUIDGrant -> {
                    currentGrant.get()
                }
            }
            PermissionType.MANAGE -> when (val currentGrant = grantTokenState.currentGrant) {
                is ChannelGrant -> {
                    currentGrant.manage()
                }
                is ChannelGroupGrant -> {
                    currentGrant.manage()
                }
            }
            PermissionType.UPDATE -> when (val currentGrant = grantTokenState.currentGrant) {
                is ChannelGrant -> {
                    currentGrant.update()
                }
                is UUIDGrant -> {
                    currentGrant.update()
                }
            }
            PermissionType.JOIN -> when (val currentGrant = grantTokenState.currentGrant) {
                is ChannelGrant -> {
                    currentGrant.join()
                }
            }
            PermissionType.DELETE -> when (val currentGrant = grantTokenState.currentGrant) {
                is ChannelGrant -> {
                    currentGrant.delete()
                }
                is UUIDGrant -> {
                    currentGrant.delete()
                }
            }
        }
    }
}
