package com.pubnub.space

import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.UserId
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.enums.PNLogVerbosity
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.objects.ResultSortKey
import com.pubnub.space.models.consumer.Space
import com.pubnub.space.models.consumer.SpaceKey
import com.pubnub.space.models.consumer.SpaceModified
import com.pubnub.space.models.consumer.SpacesResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.fail
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class SpaceIntegTest {
    private lateinit var pubnub: PubNub

    private val SPACE_ID = SpaceId("spaceIntegId")
    private val SPACE_ID_01 = SpaceId(SPACE_ID.value + "1")
    private val SPACE_ID_02 = SpaceId(SPACE_ID.value + "2")
    private val SPACE_NAME = "spaceIntegName"
    private val DESCRIPTION = "space description"
    private val CUSTOM: Map<String, String> = mapOf("favouritePet" to "mouse")
    private val STATUS = "Status"
    private val TYPE = "Type"

    @BeforeEach
    fun setUp() {
        val config = PNConfiguration(userId = UserId("kotlin")).apply {
            subscribeKey = IntegTestConf.subscribeKey
            publishKey = IntegTestConf.publishKey
            IntegTestConf.origin?.let {
                origin = it
            }
            logVerbosity = PNLogVerbosity.BODY
        }
        pubnub = PubNub(config)

        pubnub.removeSpace(spaceId = SPACE_ID_01).sync()
        pubnub.removeSpace(spaceId = SPACE_ID_02).sync()
    }

    @Test
    internal fun can_createSpace() {
        val spaceId = SPACE_ID_01
        val space: Space? = createSpace(spaceId)

        assertEquals(spaceId, space?.id)
        assertEquals(SPACE_NAME, space?.name)
        assertEquals(DESCRIPTION, space?.description)
        assertEquals(CUSTOM, space?.custom)
        assertTrue(space?.updated != null)
        assertTrue(space?.eTag != null)
    }

    @Test
    internal fun can_fetchSpace() {
        val spaceId = SPACE_ID_01
        createSpace(spaceId)

        val space: Space? = pubnub.fetchSpace(spaceId = spaceId, includeCustom = true).sync()

        assertEquals(spaceId, space?.id)
        assertEquals(SPACE_NAME, space?.name)
        assertEquals(DESCRIPTION, space?.description)
        assertEquals(CUSTOM, space?.custom)
        assertEquals(STATUS, space?.status)
        assertEquals(TYPE, space?.type)
        assertTrue(space?.updated != null)
        assertTrue(space?.eTag != null)
    }

    @Test
    internal fun can_fetchSpaces() {
        val spaceId01 = SPACE_ID_01
        createSpace(spaceId01)
        val spaceId02 = SPACE_ID_02
        createSpace(spaceId02)

        val spacesResult: SpacesResult? = pubnub.fetchSpaces(limit = 100, includeCount = true).sync()

        assertEquals(2, spacesResult?.data?.size)
        assertEquals(2, spacesResult?.totalCount)
        assertEquals(spaceId01, spacesResult?.data?.first()?.id)
        assertEquals(spaceId02, spacesResult?.data?.elementAt(1)?.id)
    }

    @Test
    internal fun can_removeSpace() {
        val spaceId01 = SPACE_ID_01
        createSpace(spaceId01)

        pubnub.removeSpace(spaceId = spaceId01).sync()

        val exception = assertThrows<PubNubException> {
            pubnub.fetchSpace(spaceId01).sync()
        }
        assertTrue(exception.errorMessage?.contains("Requested object was not found") == true)
    }

    @Test
    internal fun can_updateSpace() {
        val spaceId01 = SPACE_ID_01
        createSpace(spaceId01)
        val newName = "NewName"
        val newDescription = "NewDescription"
        pubnub.updateSpace(spaceId = spaceId01, name = newName, description = newDescription).sync()

        val space: Space? = pubnub.fetchSpace(spaceId01).sync()
        assertEquals(newName, space?.name)
        assertEquals(newDescription, space?.description)
    }

    @Test
    internal fun can_fetch_sorted_spaces() {
        val spaceId01 = SPACE_ID_01
        createSpace(spaceId01)
        val spaceId02 = SPACE_ID_02
        createSpace(spaceId02)

        val spacesResultAsc: SpacesResult? = pubnub.fetchSpaces(
            limit = 100,
            includeCount = true,
            sort = listOf(
                ResultSortKey.Asc(key = SpaceKey.ID)
            )
        ).sync()

        assertEquals(SPACE_ID_01, spacesResultAsc?.data?.first()?.id)
        assertEquals(SPACE_ID_02, spacesResultAsc?.data?.elementAt(1)?.id)

        val spacesResultDesc: SpacesResult? = pubnub.fetchSpaces(
            limit = 100,
            includeCount = true,
            sort = listOf(
                ResultSortKey.Desc(key = SpaceKey.ID)
            )
        ).sync()

        assertEquals(SPACE_ID_02, spacesResultDesc?.data?.first()?.id)
        assertEquals(SPACE_ID_01, spacesResultDesc?.data?.elementAt(1)?.id)
    }

    @Test
    internal fun can_receiveSpaceEvents() {
        val allUpdatesDone = CountDownLatch(2)
        val lastUpdate = "lastUpdate"
        val created = CountDownLatch(1)
        var space = Space(id = SPACE_ID_01)
        val connected = CountDownLatch(1)
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNConnectedCategory) {
                    connected.countDown()
                }
            }
        })

        pubnub.addSpaceEventsListener {
            if (it is SpaceModified) {
                space = space.copy(name = it.data.name)
                if (it.data.name == null) {
                    created.countDown()
                }
            }
            allUpdatesDone.countDown()
        }

        pubnub.subscribe(channels = listOf(SPACE_ID_01.value))
        if (!connected.await(5, TimeUnit.SECONDS)) {
            fail("Didn't connect")
        }

        pubnub.createSpace(SPACE_ID_01).sync()
        if (!created.await(5, TimeUnit.SECONDS)) {
            fail("Didn't receive created event")
        }
        pubnub.updateSpace(spaceId = SPACE_ID_01, name = lastUpdate).sync()
        if (!allUpdatesDone.await(5, TimeUnit.SECONDS)) {
            fail("Didn't receive enough events")
        }
        assertEquals(Space(id = SPACE_ID_01, name = lastUpdate), space)
    }

    @AfterEach
    internal fun tearDown() {
        pubnub.removeSpace(spaceId = SPACE_ID_01).sync()
        pubnub.removeSpace(spaceId = SPACE_ID_02).sync()
    }

    private fun createSpace(spaceId: SpaceId): Space? {
        return pubnub.createSpace(
            spaceId = spaceId,
            name = SPACE_NAME,
            description = DESCRIPTION,
            custom = CUSTOM,
            includeCustom = true,
            type = TYPE,
            status = STATUS
        ).sync()
    }
}
