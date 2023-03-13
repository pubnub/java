package com.pubnub.apikt.integration.pam

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.UserId
import com.pubnub.apikt.enums.PNLogVerbosity
import com.pubnub.apikt.integration.BaseIntegrationTest
import com.pubnub.apikt.models.consumer.access_manager.sum.SpacePermissions
import com.pubnub.apikt.models.consumer.access_manager.sum.UserPermissions
import com.pubnub.apikt.models.consumer.access_manager.v3.ChannelGrant
import com.pubnub.apikt.models.consumer.access_manager.v3.ChannelGroupGrant
import com.pubnub.apikt.models.consumer.access_manager.v3.PNToken.PNResourcePermissions
import org.junit.Assert.assertEquals
import org.junit.Test

class GrantTokenIntegrationTest : BaseIntegrationTest() {
    private val pubNubUnderTest: PubNub = server

    @Test
    fun happyPath_SUM() {
        // given
        pubNubUnderTest.configuration.logVerbosity = PNLogVerbosity.BODY
        val expectedTTL = 1337
        val expectedAuthorizedUserId = UserId("authorizedUser01")
        val expectedSpaceIdValue = "mySpace01"
        val expectedSpaceIdPattern = "mySpace.*"
        val expectedUserIdValue = "myUser01"
        val expectedUserIdPattern = "myUser.*"

        // when
        val grantTokenEndpoint = pubNubUnderTest.grantToken(
            ttl = expectedTTL,
            authorizedUserId = expectedAuthorizedUserId,
            spacesPermissions = listOf(
                SpacePermissions.id(spaceId = SpaceId(expectedSpaceIdValue), read = true, delete = true),
                SpacePermissions.pattern(pattern = expectedSpaceIdPattern, write = true, manage = true)
            ),
            usersPermissions = listOf(
                UserPermissions.id(userId = UserId(expectedUserIdValue), delete = true),
                UserPermissions.pattern(pattern = expectedUserIdPattern, update = true)
            )
        )

        val token = grantTokenEndpoint.sync()!!.token

        // then
        val (_, _, ttl, _, resources, patterns) = pubNubUnderTest.parseToken(token)
        assertEquals(expectedTTL.toLong(), ttl)

        assertEquals(expectedTTL.toLong(), ttl)
        assertEquals(
            PNResourcePermissions(
                read = true,
                delete = true
            ),
            resources.channels[expectedSpaceIdValue]
        )
        assertEquals(
            PNResourcePermissions(
                write = true,
                manage = true
            ),
            patterns.channels[expectedSpaceIdPattern]
        )
        assertEquals(PNResourcePermissions(delete = true), resources.uuids[expectedUserIdValue])
        assertEquals(PNResourcePermissions(update = true), patterns.uuids[expectedUserIdPattern])
    }

    @Test
    fun happyPath() {
        // given
        pubNubUnderTest.configuration.logVerbosity = PNLogVerbosity.BODY
        val expectedTTL = 1337
        val expectedChannelResourceName = "channelResource"
        val expectedChannelPattern = "channel.*"
        val expectedChannelGroupResourceId = "channelGroup"
        val expectedChannelGroupPattern = "channelGroup.*"

        // when
        val token = pubNubUnderTest
            .grantToken(
                ttl = expectedTTL,
                channels = listOf(
                    ChannelGrant.name(name = expectedChannelResourceName, delete = true),
                    ChannelGrant.pattern(pattern = expectedChannelPattern, write = true)
                ),
                channelGroups = listOf(
                    ChannelGroupGrant.id(id = expectedChannelGroupResourceId, read = true),
                    ChannelGroupGrant.pattern(pattern = expectedChannelGroupPattern, manage = true)
                )
            )
            .sync()!!
            .token
        val (_, _, ttl, _, resources, patterns) = pubNubUnderTest.parseToken(token)

        println(pubNubUnderTest.parseToken(token))
        // then
        assertEquals(expectedTTL.toLong(), ttl)
        assertEquals(
            PNResourcePermissions(
                delete = true
            ),
            resources.channels[expectedChannelResourceName]
        )
        assertEquals(
            PNResourcePermissions(
                read = true
            ),
            resources.channelGroups[expectedChannelGroupResourceId]
        )
        assertEquals(
            PNResourcePermissions(
                write = true
            ),
            patterns.channels[expectedChannelPattern]
        )
        assertEquals(
            PNResourcePermissions(
                manage = true
            ),
            patterns.channelGroups[expectedChannelGroupPattern]
        )
    }
}
