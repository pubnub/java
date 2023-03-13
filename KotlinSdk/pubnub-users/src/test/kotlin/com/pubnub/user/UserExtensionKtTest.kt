package com.pubnub.user

import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.UserId
import com.pubnub.apikt.endpoints.objects.uuid.GetAllUUIDMetadata
import com.pubnub.apikt.endpoints.objects.uuid.GetUUIDMetadata
import com.pubnub.apikt.endpoints.objects.uuid.RemoveUUIDMetadata
import com.pubnub.apikt.endpoints.objects.uuid.SetUUIDMetadata
import com.pubnub.apikt.endpoints.remoteaction.ExtendedRemoteAction
import com.pubnub.apikt.models.consumer.objects.PNRemoveMetadataResult
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadataArrayResult
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadataResult
import com.pubnub.user.models.consumer.RemoveUserResult
import com.pubnub.user.models.consumer.User
import com.pubnub.user.models.consumer.UsersResult
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

internal class UserExtensionKtTest {
    private lateinit var pubNub: PubNub

    private val USER_ID = UserId("userId")
    private val USER_ID_02 = UserId("userId02")
    private val USER_NAME = "userName"
    private val EXTERNAL_ID = "externalId"
    private val PROFILE_URL = "profileUrl"
    private val EMAIL = "user@user.com"
    private val CUSTOM = mapOf("My favourite sport" to "windsurfing")
    private val INCLUDE_CUSOTM = true
    private val UPDATED = "2022-05-24T08:11:49.398709Z"
    private val E_TAG = "AeWNuf6b3aHYeg"
    private val TYPE = "type"
    private val STATUS = "status"
    private val expectedUser01 = createUser(USER_ID)
    private val expectedUser02 = createUser(USER_ID_02)

    @MockK
    lateinit var setUUIDMetadataEndpoint: SetUUIDMetadata

    @MockK
    lateinit var getUUIDMetadataEndpoint: GetUUIDMetadata

    @MockK
    lateinit var removeUUIDMetadataEndpoint: RemoveUUIDMetadata

    @MockK
    lateinit var getAllUUIDMetadataEndpoint: GetAllUUIDMetadata

    @BeforeEach
    internal fun setUp() {
        MockKAnnotations.init(this)
        val pnConfiguration = PNConfiguration(userId = UserId(PubNub.generateUUID()))
        pubNub = spyk(PubNub(pnConfiguration))
    }

    @Test
    internal fun can_createUser() {
        val pnUUIDMetadata = createPnuuidMetadata(USER_ID)
        val pnUUIDMetadataResult = PNUUIDMetadataResult(status = 200, data = pnUUIDMetadata)
        every {
            pubNub.setUUIDMetadata(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns setUUIDMetadataEndpoint
        every { setUUIDMetadataEndpoint.sync() } returns pnUUIDMetadataResult

        val createUserEndpoint: ExtendedRemoteAction<User?> = pubNub.createUser(
            userId = USER_ID,
            name = USER_NAME,
            externalId = EXTERNAL_ID,
            profileUrl = PROFILE_URL,
            email = EMAIL,
            custom = CUSTOM,
            includeCustom = INCLUDE_CUSOTM
        )

        val user = createUserEndpoint.sync()

        assertEquals(expectedUser01, user)
    }

    @Test
    internal fun can_fetchUser() {
        val pnUUIDMetadata = createPnuuidMetadata(USER_ID)
        val pnUUIDMetadataResult = PNUUIDMetadataResult(status = 200, data = pnUUIDMetadata)
        every { pubNub.getUUIDMetadata(any(), any()) } returns getUUIDMetadataEndpoint
        every { getUUIDMetadataEndpoint.sync() } returns pnUUIDMetadataResult

        val fetchUserEndpoint: ExtendedRemoteAction<User?> = pubNub.fetchUser(userId = USER_ID, includeCustom = true)
        val user = fetchUserEndpoint.sync()

        assertEquals(expectedUser01, user)
    }

    @Test
    internal fun can_updateUser() {
        val pnUUIDMetadata = createPnuuidMetadata(USER_ID)
        val pnUUIDMetadataResult = PNUUIDMetadataResult(status = 200, data = pnUUIDMetadata)
        every {
            pubNub.setUUIDMetadata(
                any(), any(), any(), any(), any(), any(), any()
            )
        } returns setUUIDMetadataEndpoint
        every { setUUIDMetadataEndpoint.sync() } returns pnUUIDMetadataResult

        val updateUserEndpoint: ExtendedRemoteAction<User?> = pubNub.updateUser(
            userId = USER_ID,
            name = USER_NAME,
            externalId = EXTERNAL_ID,
            profileUrl = PROFILE_URL,
            email = EMAIL,
            custom = CUSTOM,
            includeCustom = INCLUDE_CUSOTM
        )
        val user = updateUserEndpoint.sync()

        assertEquals(expectedUser01, user)
    }

    @Test
    internal fun can_removeUser() {
        val pnRemoveMetadataResult = PNRemoveMetadataResult(status = 200)
        every { pubNub.removeUUIDMetadata(any()) } returns removeUUIDMetadataEndpoint
        every { removeUUIDMetadataEndpoint.sync() } returns pnRemoveMetadataResult

        val removeUserEndpoint: ExtendedRemoteAction<RemoveUserResult?> = pubNub.removeUser(userId = USER_ID)
        val removeUserResult = removeUserEndpoint.sync()

        assertEquals(200, removeUserResult?.status)
    }

    @Test
    internal fun can_fetchUsers() {
        val pnUUIDMetadataList = listOf(createPnuuidMetadata(USER_ID), createPnuuidMetadata(USER_ID_02))
        val pnUUIDMetadataArrayResult =
            PNUUIDMetadataArrayResult(status = 200, data = pnUUIDMetadataList, totalCount = 2, next = null, prev = null)
        every { pubNub.getAllUUIDMetadata(any(), any(), any(), any(), any(), any()) } returns getAllUUIDMetadataEndpoint
        every { getAllUUIDMetadataEndpoint.sync() } returns pnUUIDMetadataArrayResult

        val fetchUsersEndpoint: ExtendedRemoteAction<UsersResult?> = pubNub.fetchUsers()
        val usersResult = fetchUsersEndpoint.sync()

        assertEquals(
            UsersResult(
                totalCount = 2, prev = null, next = null, data = listOf(expectedUser01, expectedUser02)
            ),
            usersResult
        )
    }

    @Test
    internal fun can_addListenerForUserEvents() {
        pubNub.addUserEventsListener { }

        verify { pubNub.addListener(any()) }
    }

    @Test
    internal fun can_removeListenerForUserEvents() {
        val listener = pubNub.addUserEventsListener { }
        listener.dispose()
        verify { pubNub.removeListener(any()) }
    }

    @Test
    internal fun can_modify_userId() {
        val modifiedUserId = "NewUserId-${UUID.randomUUID()}"
        pubNub.configuration.userId = UserId(modifiedUserId)

        assertEquals(modifiedUserId, pubNub.configuration.userId.value)
    }

    @Test
    internal fun should_throw_exception_when_userId_is_empty() {
        val exception = assertThrows<PubNubException> {
            pubNub.configuration.userId = UserId("")
        }

        assertEquals("UserId can't have empty value", exception.errorMessage)
    }

    private fun createPnuuidMetadata(userId: UserId): PNUUIDMetadata {
        val pnUUIDMetadata = PNUUIDMetadata(
            id = userId.value,
            name = USER_NAME,
            externalId = EXTERNAL_ID,
            profileUrl = PROFILE_URL,
            email = EMAIL,
            custom = CUSTOM,
            updated = UPDATED,
            eTag = E_TAG,
            type = TYPE,
            status = STATUS
        )
        return pnUUIDMetadata
    }

    private fun createUser(id: UserId) = User(
        id = id,
        externalId = EXTERNAL_ID,
        profileUrl = PROFILE_URL,
        email = EMAIL,
        name = USER_NAME,
        updated = UPDATED,
        eTag = E_TAG,
        custom = CUSTOM,
        type = TYPE,
        status = STATUS
    )
}
