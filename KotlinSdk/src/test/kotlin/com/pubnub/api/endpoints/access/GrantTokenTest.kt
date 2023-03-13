package com.pubnub.apikt.endpoints.access

import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.UserId
import com.pubnub.apikt.managers.RetrofitManager
import com.pubnub.apikt.models.consumer.access_manager.sum.SpacePermissions
import com.pubnub.apikt.models.consumer.access_manager.v3.PNGrantTokenResult
import com.pubnub.apikt.models.server.access_manager.v3.GrantTokenData
import com.pubnub.apikt.models.server.access_manager.v3.GrantTokenRequestBody
import com.pubnub.apikt.models.server.access_manager.v3.GrantTokenResponse
import com.pubnub.apikt.services.AccessManagerService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Call
import retrofit2.Response

internal class GrantTokenTest {
    private lateinit var pubnub: PubNub

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        val pnConfiguration = PNConfiguration(userId = UserId("myUserId")).apply {
            subscribeKey = "something"
            secretKey = "something"
        }
        pubnub = spyk(PubNub(configuration = pnConfiguration))
    }

    @MockK
    private lateinit var grantTokenEndpointMock: GrantToken

    @Test
    fun can_createGrantTokenSimple() {
        val expectedTTL = 1337
        val authorizedUserId = UserId("authorizedUserId")
        val expectedToken = "token_value"
        val grantTokenResult = PNGrantTokenResult(token = expectedToken)
        every {
            pubnub.grantToken(
                ttl = any(),
                authorizedUserId = any(),
                spacesPermissions = any(),
                usersPermissions = any()
            )
        } returns grantTokenEndpointMock
        every { grantTokenEndpointMock.sync() } returns grantTokenResult

        val grantTokenEndpoint = pubnub.grantToken(
            ttl = expectedTTL,
            authorizedUserId = authorizedUserId,
            spacesPermissions = listOf(SpacePermissions.id(spaceId = SpaceId("mySpaceId"), read = true, delete = true))
        )
        val actualGrantTokenResult: PNGrantTokenResult? = grantTokenEndpoint.sync()
        val token = actualGrantTokenResult!!.token

        assertEquals(expectedToken, token)
    }

    @Test
    fun can_createGrantToken() {
        val expectedTTL = 1337
        val authorizedUserId = UserId("authorizedUserId")
        val expectedToken = "token_value"
        val spaceIdValue = "mySpaceId"

        val retrofitManager = mockk<RetrofitManager>(relaxed = true)
        val accessManagerService = mockk<AccessManagerService>(relaxed = true)
        val capturedBodies = mutableListOf<Any>()
        val call = mockk<Call<GrantTokenResponse>>()

        every { pubnub.retrofitManager } returns retrofitManager
        every { retrofitManager.accessManagerService } returns accessManagerService
        every { accessManagerService.grantToken(any(), capture(capturedBodies), any()) } returns call
        every { call.execute() } returns Response.success(GrantTokenResponse(GrantTokenData(expectedToken)))

        val actualGrantTokenResult: PNGrantTokenResult? = pubnub.grantToken(
            ttl = expectedTTL,
            authorizedUserId = authorizedUserId,
            spacesPermissions = listOf(
                SpacePermissions.id(
                    spaceId = SpaceId(spaceIdValue),
                    read = true,
                    delete = true
                )
            )
        ).sync()

        val capturedBody = capturedBodies[0]

        assertEquals(expectedToken, actualGrantTokenResult!!.token)

        val ttl: Int = (capturedBody as GrantTokenRequestBody).ttl
        val permissions = capturedBody.permissions

        assertEquals(expectedTTL, ttl)
        assertTrue(permissions.resources.channels.containsKey(spaceIdValue))
        assertEquals(authorizedUserId.value, permissions.uuid)
    }
}
