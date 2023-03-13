package com.pubnub.contract.access.step

import com.pubnub.apikt.models.consumer.access_manager.v3.PNToken
import com.pubnub.contract.access.parameter.PermissionType
import com.pubnub.contract.access.parameter.ResourceType
import com.pubnub.contract.access.parameter.patternPermissionsMap
import com.pubnub.contract.access.parameter.resourcePermissionsMap
import com.pubnub.contract.access.state.GrantTokenState
import com.pubnub.contract.state.World
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

class ThenSteps(
    private val grantTokenState: GrantTokenState,
    private val world: World
) {
    @Then("the authorized UUID {string}")
    fun authorized_uuid(uuid: String) {
        grantTokenState.authorizedUUID = uuid
    }

    @Then("the parsed token output contains the authorized UUID {string}")
    fun the_parsed_token_output_contains_the_authorized_uuid(uuid: String) {
        val token = grantTokenState.parsedToken!!
        MatcherAssert.assertThat(token.authorizedUUID, Matchers.`is`(uuid))
    }

    @Then("the token contains the authorized UUID {string}")
    fun the_token_contains_the_authorized_uuid(uuid: String) {
        val result = grantTokenState.result!!
        val parsedToken = world.pubnub.parseToken(result.token)
        MatcherAssert.assertThat(parsedToken.authorizedUUID, Matchers.`is`(uuid))
    }

    @Then("the token contains the TTL {ttl}")
    fun the_token_contains_the_ttl(ttl: Long) {
        val result = grantTokenState.result!!
        val token = world.pubnub.parseToken(result.token)
        MatcherAssert.assertThat(token.ttl, Matchers.`is`(ttl))
    }

    @Then("the token does not contain an authorized uuid")
    fun the_token_does_not_contain_an_authorized_uuid() {
        val result = grantTokenState.result!!
        val token = world.pubnub.parseToken(result.token)
        MatcherAssert.assertThat(token.authorizedUUID, Matchers.nullValue())
    }

    @Then("the token has {string} {resourceType} resource access permissions")
    fun the_token_has_channel_resource_access_permissions(name: String, resourceType: ResourceType) {
        val token = parsedToken()!!
        val permissions = token.resourcePermissionsMap(resourceType)[name]
        MatcherAssert.assertThat(
            "Token doesn't contain required permissions $token",
            permissions,
            Matchers.notNullValue()
        )
        grantTokenState.currentResourcePermissions = permissions
    }

    @Then("the token has {string} {resourceType} pattern access permissions")
    fun the_token_has_channel_pattern_access_permissions(name: String, resourceType: ResourceType) {
        val token = parsedToken()!!
        val permissions = token.patternPermissionsMap(resourceType)[name]
        MatcherAssert.assertThat(
            "Token doesn't contain required permissions $token",
            permissions,
            Matchers.notNullValue()
        )
        grantTokenState.currentResourcePermissions = permissions
    }

    @Then("token resource permission {permissionType}")
    fun token_resource_permission(permissionType: PermissionType) {
        assertPermissions(permissionType)
    }

    @Then("token pattern permission {permissionType}")
    fun token_pattern_permission(permissionType: PermissionType) {
        assertPermissions(permissionType)
    }

    @Then("I get confirmation that token has been revoked")
    fun i_get_confirmation_that_token_has_been_revoked() {
        MatcherAssert.assertThat(world.pnException, Matchers.nullValue())
    }

    private fun assertPermissions(permissionType: PermissionType) {
        val permissions = grantTokenState.currentResourcePermissions!!
        when (permissionType) {
            PermissionType.READ -> MatcherAssert.assertThat(permissions.read, Matchers.`is`(true))
            PermissionType.WRITE -> MatcherAssert.assertThat(permissions.write, Matchers.`is`(true))
            PermissionType.GET -> MatcherAssert.assertThat(permissions.get, Matchers.`is`(true))
            PermissionType.MANAGE -> MatcherAssert.assertThat(permissions.manage, Matchers.`is`(true))
            PermissionType.UPDATE -> MatcherAssert.assertThat(permissions.update, Matchers.`is`(true))
            PermissionType.JOIN -> MatcherAssert.assertThat(permissions.join, Matchers.`is`(true))
            PermissionType.DELETE -> MatcherAssert.assertThat(permissions.delete, Matchers.`is`(true))
        }
    }

    private fun parsedToken(): PNToken? {
        return grantTokenState.parsedToken ?: grantTokenState.result?.let { world.pubnub.parseToken(it.token) }
    }
}
