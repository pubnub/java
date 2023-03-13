package com.pubnub.space

import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.UserId
import com.pubnub.apikt.endpoints.objects.channel.GetAllChannelMetadata
import com.pubnub.apikt.endpoints.objects.channel.GetChannelMetadata
import com.pubnub.apikt.endpoints.objects.channel.RemoveChannelMetadata
import com.pubnub.apikt.endpoints.objects.channel.SetChannelMetadata
import com.pubnub.apikt.endpoints.remoteaction.ExtendedRemoteAction
import com.pubnub.apikt.models.consumer.objects.PNRemoveMetadataResult
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadata
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadataArrayResult
import com.pubnub.apikt.models.consumer.objects.channel.PNChannelMetadataResult
import com.pubnub.space.models.consumer.RemoveSpaceResult
import com.pubnub.space.models.consumer.Space
import com.pubnub.space.models.consumer.SpacesResult
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SpaceExtensionKtTest {
    private lateinit var pubNub: PubNub

    private val SPACE_ID = SpaceId("unitTestKT_spaceId1")
    private val SPACE_ID_02 = SpaceId("unitTestKT_spaceId2")
    private val SPACE_NAME = "unitTestKT_spaceName"
    private val SPACE_DESCRIPTION = "space description"
    private val CUSTOM = mapOf("My favourite car" to "Syrena")
    private val UPDATED = "2022-05-24T08:11:49.398709Z"
    private val E_TAG = "AeWNuf6b3aHYeg"
    private val TYPE = "type"
    private val STATUS = "status"
    private val expectedSpace01 = createSpace(SPACE_ID)
    private val expectedSpace02 = createSpace(SPACE_ID_02)

    @MockK
    lateinit var getChannelMetadataEndpoint: GetChannelMetadata

    @MockK
    lateinit var setChannelMetadataEndpoint: SetChannelMetadata

    @MockK
    lateinit var removeChannelMetadataEndpoint: RemoveChannelMetadata

    @MockK
    lateinit var getAllChannelMetadataEndpoint: GetAllChannelMetadata

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        val pnConfiguration = PNConfiguration(userId = UserId(PubNub.generateUUID()))
        pubNub = spyk(PubNub(pnConfiguration))
    }

    @Test
    internal fun can_createSpace() {
        val pnChannelMetadataResult = PNChannelMetadataResult(200, createPnChannelMetadata(SPACE_ID))
        every {
            pubNub.setChannelMetadata(
                any(), any(), any(), any(), any(), any(), any()
            )
        } returns setChannelMetadataEndpoint
        every { setChannelMetadataEndpoint.sync() } returns pnChannelMetadataResult

        val createSpaceEndpoint: ExtendedRemoteAction<Space?> = pubNub.createSpace(
            spaceId = SPACE_ID,
            name = SPACE_NAME,
            description = SPACE_DESCRIPTION,
            custom = CUSTOM,
            includeCustom = true,
            type = TYPE,
            status = STATUS
        )

        val space = createSpaceEndpoint.sync()
        assertEquals(expectedSpace01, space)
    }

    @Test
    internal fun can_fetchSpace() {
        val pnChannelMetadataResult = PNChannelMetadataResult(200, createPnChannelMetadata(SPACE_ID))
        every { pubNub.getChannelMetadata(any(), any()) } returns getChannelMetadataEndpoint
        every { getChannelMetadataEndpoint.sync() } returns pnChannelMetadataResult

        val fetchSpaceEndpoint: ExtendedRemoteAction<Space?> = pubNub.fetchSpace(spaceId = SPACE_ID)
        val space: Space? = fetchSpaceEndpoint.sync()

        assertEquals(expectedSpace01, space)
    }

    @Test
    internal fun can_updateSpace() {
        val pnChannelMetadataResult = PNChannelMetadataResult(200, createPnChannelMetadata(SPACE_ID))
        every { pubNub.setChannelMetadata(any(), any(), any(), any(), any()) } returns setChannelMetadataEndpoint
        every { setChannelMetadataEndpoint.sync() } returns pnChannelMetadataResult

        val updateSpaceEndpoint: ExtendedRemoteAction<Space?> = pubNub.updateSpace(
            spaceId = SPACE_ID, name = SPACE_NAME, custom = CUSTOM, description = SPACE_DESCRIPTION
        )
        val space = updateSpaceEndpoint.sync()
        assertEquals(expectedSpace01, space)
    }

    @Test
    internal fun can_removeSpace() {
        every { pubNub.removeChannelMetadata(any()) } returns removeChannelMetadataEndpoint
        every { removeChannelMetadataEndpoint.sync() } returns PNRemoveMetadataResult(200)

        val removeSpaceEndpoint: ExtendedRemoteAction<RemoveSpaceResult?> = pubNub.removeSpace(spaceId = SPACE_ID)
        val removeSpaceResult = removeSpaceEndpoint.sync()

        assertEquals(200, removeSpaceResult?.status)
    }

    @Test
    internal fun can_fetchSpaces() {
        val pnChannelMetadataList = listOf(createPnChannelMetadata(SPACE_ID), createPnChannelMetadata(SPACE_ID_02))
        val pnChannelMetadataArrayResult =
            PNChannelMetadataArrayResult(status = 200, data = pnChannelMetadataList, totalCount = 2, null, null)
        every {
            pubNub.getAllChannelMetadata(
                any(), any(), any(), any(), any(), any()
            )
        } returns getAllChannelMetadataEndpoint
        every { getAllChannelMetadataEndpoint.sync() } returns pnChannelMetadataArrayResult

        val fetchSpacesEndpoint = pubNub.fetchSpaces(limit = 10)
        val spacesResult = fetchSpacesEndpoint.sync()

        assertEquals(
            SpacesResult(
                totalCount = 2, data = listOf(expectedSpace01, expectedSpace02), next = null, prev = null
            ),
            spacesResult
        )
    }

    @Test
    internal fun can_addListenerForSpaceEvents() {
        pubNub.addSpaceEventsListener { }

        verify { pubNub.addListener(any()) }
    }

    @Test
    internal fun can_removeListenerForSpaceEvents() {
        val listener = pubNub.addSpaceEventsListener { }
        listener.dispose()
        verify { pubNub.removeListener(any()) }
    }

    private fun createPnChannelMetadata(id: SpaceId): PNChannelMetadata {
        return PNChannelMetadata(
            id = id.value,
            name = SPACE_NAME,
            description = SPACE_DESCRIPTION,
            custom = CUSTOM,
            updated = UPDATED,
            eTag = E_TAG,
            type = TYPE,
            status = STATUS
        )
    }

    private fun createSpace(id: SpaceId) = Space(
        id = id,
        name = SPACE_NAME,
        description = SPACE_DESCRIPTION,
        updated = UPDATED,
        eTag = E_TAG,
        custom = CUSTOM,
        type = TYPE,
        status = STATUS
    )
}
