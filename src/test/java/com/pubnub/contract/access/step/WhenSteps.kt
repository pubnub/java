package com.pubnub.contract.access.step

import com.pubnub.contract.access.state.GrantTokenState
import com.pubnub.contract.state.World
import com.pubnub.api.PubNubException
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGrant
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGroupGrant
import com.pubnub.api.models.consumer.access_manager.v3.UUIDGrant
import io.cucumber.java.en.When
import org.junit.Assert

class WhenSteps(
        private val grantTokenState: GrantTokenState,
        private val world: World
) {


    @When("I grant a token specifying those permissions")
    fun grant_token() {
        grantTokenState.result = world.pubnub.grantToken().let {
            grantTokenState.TTL?.let { ttl ->
                it.ttl(ttl.toInt())
            }
            grantTokenState.authorizedUUID?.let { authorizedUUID -> it.authorizedUUID(authorizedUUID) }
            it.channels(grantTokenState.definedGrants.filterIsInstance(ChannelGrant::class.java))
            it.channelGroups(grantTokenState.definedGrants.filterIsInstance(ChannelGroupGrant::class.java))
            it.uuids(grantTokenState.definedGrants.filterIsInstance(UUIDGrant::class.java))

            it.sync()
        }
    }

    @When("I attempt to grant a token specifying those permissions")
    fun i_attempt_to_grant_a_token_specifying_those_permissions() {
        try {
            grant_token()
            Assert.fail("Expected exception")
        } catch (ex: PubNubException) {
            world.pnException = ex
        } catch (ex: AssertionError) {
            throw ex
        } catch (t: Throwable) {
            Assert.fail("Expected PubNubException but got throwable $t")
        }
    }

    @When("I parse the token")
    fun i_parse_the_token() {
        grantTokenState.parsedToken = world.pubnub.parseToken(grantTokenState.result?.token)
    }

    @When("I revoke a token")
    fun i_revoke_the_token() {
        try {
            world.pubnub.revokeToken()
                    .token(world.tokenString!!).sync()
        } catch (e: PubNubException) {
            world.pnException = e
        }
    }

    @When("I publish a message using that auth token with channel {string}")
    fun i_publish_a_message_using_that_auth_token_with_channel(channel: String) {
        world.pubnub.setToken(world.tokenString)
        world.pubnub.publish()
                .channel(channel)
                .message("Message")
                .sync()
    }

    @When("I attempt to publish a message using that auth token with channel {string}")
    fun i_attempt_to_publish_a_message_using_that_auth_token_with_channel(channel: String) {
        world.pubnub.setToken(world.tokenString)

        try {
            world.pubnub.publish()
                    .channel(channel)
                    .message("Message")
                    .sync()
        } catch (e: PubNubException) {
            world.pnException = e
        }
    }
}
