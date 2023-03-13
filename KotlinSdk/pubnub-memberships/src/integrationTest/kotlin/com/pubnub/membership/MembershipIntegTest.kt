package com.pubnub.membership

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.SpaceId
import com.pubnub.apikt.UserId
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.objects.ResultSortKey
import com.pubnub.membership.models.consumer.Membership
import com.pubnub.membership.models.consumer.MembershipModified
import com.pubnub.membership.models.consumer.MembershipsResult
import com.pubnub.membership.models.consumer.SpaceDetailsLevel
import com.pubnub.membership.models.consumer.SpaceMembershipResultKey
import com.pubnub.membership.models.consumer.UserDetailsLevel
import com.pubnub.membership.models.consumer.UserMembershipsResultKey
import com.pubnub.space.createSpace
import com.pubnub.space.models.consumer.Space
import com.pubnub.space.removeSpace
import com.pubnub.user.createUser
import com.pubnub.user.models.consumer.User
import com.pubnub.user.removeUser
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class MembershipIntegTest {
    private lateinit var pubnub: PubNub

    private val USER_ID = UserId("user_id")
    private val USER_NAME = "user name"
    private val USER_NAME_02 = "user name 02"
    private val USER_ID_02 = UserId("user_id02")
    private val EXTERNAL_ID = "externalId"
    private val PROFILE_URL = "prorileUrl"
    private val EMAIL = "emaiL@mail.com"
    private val USER_CUSTOM = mapOf("user favourite pet" to "mouse")
    private val USER_STATUS = "userStatus"
    private val USER_TYPE = "userType"

    private val SPACE_ID = SpaceId("space_id")
    private val SPACE_ID_02 = SpaceId("space_id02")
    private val SPACE_NAME = "space_name"
    private val SPACE_NAME_02 = "space_name02"
    private val SPACE_DESCRIPTION = "space_description"
    private val SPACE_CUSTOM = mapOf("space favourite pet" to "mouse")
    private val SPACE_STATUS = "spaceStatus"
    private val SPACE_TYPE = "spaceType"
    private val MEMBERSHIP_CUSTOM = mapOf("membership favourite pet" to "mouse")
    private val MEMBERSHIP_STATUS = "membership_status"

    @BeforeEach
    fun setUp() {
        val config = PNConfiguration(userId = UserId(uuid)).apply {
            subscribeKey = IntegTestConf.subscribeKey
            publishKey = IntegTestConf.publishKey
            IntegTestConf.origin?.let {
                origin = it
            }
        }
        pubnub = PubNub(config)

        removeEntities()
    }

    @Test
    internal fun can_addMembershipOfUser_and_fetchMembershipOfUser() {
        createUser(USER_ID, USER_NAME)
        createSpace(SPACE_ID, SPACE_NAME)
        createSpace(SPACE_ID_02, SPACE_NAME_02)
        val partialMembershipsWithSpace: List<Membership.PartialWithSpace> =
            listOf(Membership.Partial(SPACE_ID, MEMBERSHIP_CUSTOM), Membership.Partial(SPACE_ID_02, MEMBERSHIP_CUSTOM))

        val addMembershipsResult =
            pubnub.addMemberships(partialMembershipsWithSpace = partialMembershipsWithSpace, userId = USER_ID).sync()

        assertEquals(200, addMembershipsResult?.status)
        val fetchMembershipsResult = pubnub.fetchMemberships(
            userId = USER_ID,
            includeCustom = true,
            includeSpaceDetails = SpaceDetailsLevel.SPACE_WITH_CUSTOM,
            includeCount = true
        ).sync()
        assertMembershipResultOfUser(fetchMembershipsResult)
    }

    @Test
    internal fun can_addMembershipOfSpace_and_fetchMembershipOfSpace() {
        createUser(USER_ID, USER_NAME)
        createUser(USER_ID_02, USER_NAME_02)
        createSpace(SPACE_ID, SPACE_NAME)
        val partialMembershipsWithUser: List<Membership.PartialWithUser> =
            listOf(Membership.Partial(USER_ID, MEMBERSHIP_CUSTOM), Membership.Partial(USER_ID_02, MEMBERSHIP_CUSTOM))

        val addMembershipsResult =
            pubnub.addMemberships(spaceId = SPACE_ID, partialMembershipsWithUser = partialMembershipsWithUser).sync()

        assertEquals(200, addMembershipsResult?.status)
        val fetchMembershipsResult = pubnub.fetchMemberships(
            spaceId = SPACE_ID,
            includeCustom = true,
            includeUserDetails = UserDetailsLevel.USER_WITH_CUSTOM,
            includeCount = true
        ).sync()
        assertMembershipResultOfSpace(fetchMembershipsResult)
    }

    @Test
    internal fun can_removeMembershipOfUser() {
        val fetchMembershipsResultNoMembership =
            pubnub.fetchMemberships(userId = USER_ID, includeCount = true).sync()
        assertEquals(0, fetchMembershipsResultNoMembership?.totalCount)
        createUser(USER_ID, USER_NAME)
        createSpace(SPACE_ID, SPACE_NAME)
        createSpace(SPACE_ID_02, SPACE_NAME_02)
        val partialMembershipsWithSpace: List<Membership.PartialWithSpace> =
            listOf(Membership.Partial(SPACE_ID, MEMBERSHIP_CUSTOM), Membership.Partial(SPACE_ID_02, MEMBERSHIP_CUSTOM))
        pubnub.addMemberships(partialMembershipsWithSpace = partialMembershipsWithSpace, userId = USER_ID).sync()
        val fetchMembershipsResultAfterAdd = pubnub.fetchMemberships(userId = USER_ID, includeCount = true).sync()
        assertEquals(2, fetchMembershipsResultAfterAdd?.totalCount)

        val removeMembershipsResult =
            pubnub.removeMemberships(spaceIds = listOf(SPACE_ID, SPACE_ID_02), userId = USER_ID).sync()
        assertEquals(200, removeMembershipsResult?.status)

        val fetchMembershipsResultAfterRemove =
            pubnub.fetchMemberships(userId = USER_ID, includeCount = true).sync()
        assertEquals(0, fetchMembershipsResultAfterRemove?.totalCount)
    }

    @Test
    internal fun can_removeMembershipOfSpace() {
        val fetchMembershipsResultNoMembership =
            pubnub.fetchMemberships(spaceId = SPACE_ID, includeCount = true).sync()
        assertEquals(0, fetchMembershipsResultNoMembership?.totalCount)
        createUser(USER_ID, USER_NAME)
        createUser(USER_ID_02, USER_NAME_02)
        createSpace(SPACE_ID, SPACE_NAME)
        val partialMembershipsWithUser: List<Membership.PartialWithUser> =
            listOf(Membership.Partial(USER_ID, MEMBERSHIP_CUSTOM), Membership.Partial(USER_ID_02, MEMBERSHIP_CUSTOM))
        pubnub.addMemberships(spaceId = SPACE_ID, partialMembershipsWithUser = partialMembershipsWithUser).sync()
        val fetchMembershipsResultAfterAdd =
            pubnub.fetchMemberships(spaceId = SPACE_ID, includeCount = true).sync()
        assertEquals(2, fetchMembershipsResultAfterAdd?.totalCount)

        val removeMembershipsResult =
            pubnub.removeMemberships(spaceId = SPACE_ID, userIds = listOf(USER_ID, USER_ID_02)).sync()
        assertEquals(200, removeMembershipsResult?.status)

        val fetchMembershipsResultAfterRemove =
            pubnub.fetchMemberships(spaceId = SPACE_ID, includeCount = true).sync()
        assertEquals(0, fetchMembershipsResultAfterRemove?.totalCount)
    }

    @Test
    internal fun can_updateMembershipOfUser_by_adding_membership() {
        createUser(USER_ID, USER_NAME)
        createSpace(SPACE_ID, SPACE_NAME)
        val partialMembershipsWithSpace = listOf(Membership.Partial(SPACE_ID, MEMBERSHIP_CUSTOM))
        pubnub.addMemberships(partialMembershipsWithSpace = partialMembershipsWithSpace, userId = USER_ID).sync()
        val fetchMembershipsResult = pubnub.fetchMemberships(
            userId = USER_ID,
            includeCustom = true,
            includeSpaceDetails = SpaceDetailsLevel.SPACE_WITH_CUSTOM,
            includeCount = true
        ).sync()
        assertEquals(1, fetchMembershipsResult?.totalCount)

        createSpace(SPACE_ID_02, SPACE_NAME)
        val updateMembershipsResult = pubnub.updateMemberships(
            userId = USER_ID,
            partialMembershipsWithSpace = listOf(Membership.Partial(SPACE_ID_02, MEMBERSHIP_CUSTOM))
        ).sync()
        assertEquals(200, updateMembershipsResult?.status)

        val fetchMembershipsResultAfterUpdate = pubnub.fetchMemberships(
            userId = USER_ID,
            includeCount = true,
            includeCustom = true,
            includeSpaceDetails = SpaceDetailsLevel.SPACE_WITH_CUSTOM
        ).sync()
        assertEquals(2, fetchMembershipsResultAfterUpdate?.totalCount)
        assertEquals(MEMBERSHIP_CUSTOM, fetchMembershipsResultAfterUpdate?.data?.first()?.custom)
    }

    @Test
    internal fun can_updateMembershipOfUser_by_adding_membership_custom() {
        createUser(USER_ID, USER_NAME)
        createSpace(SPACE_ID, SPACE_NAME)
        val partialMembershipsWithSpace = listOf(Membership.Partial(SPACE_ID))
        pubnub.addMemberships(partialMembershipsWithSpace = partialMembershipsWithSpace, userId = USER_ID).sync()

        pubnub.updateMemberships(
            userId = USER_ID,
            partialMembershipsWithSpace = listOf(Membership.Partial(SPACE_ID, MEMBERSHIP_CUSTOM))
        ).sync()

        val fetchMembershipsResultAfterUpdate = pubnub.fetchMemberships(
            userId = USER_ID,
            includeCount = true,
            includeCustom = true,
            includeSpaceDetails = SpaceDetailsLevel.SPACE_WITH_CUSTOM
        ).sync()
        assertEquals(1, fetchMembershipsResultAfterUpdate?.totalCount)
        assertEquals(MEMBERSHIP_CUSTOM, fetchMembershipsResultAfterUpdate?.data?.first()?.custom)
    }

    @Test
    internal fun can_updateMembershipOfSpace() {
        createUser(USER_ID, USER_NAME)
        createSpace(SPACE_ID, SPACE_NAME)
        val partialMembershipsWithUser = listOf(Membership.Partial(userId = USER_ID, custom = MEMBERSHIP_CUSTOM))
        pubnub.addMemberships(spaceId = SPACE_ID, partialMembershipsWithUser = partialMembershipsWithUser).sync()
        val fetchMembershipsResult = pubnub.fetchMemberships(spaceId = SPACE_ID, includeCount = true).sync()
        assertEquals(1, fetchMembershipsResult?.totalCount)

        createUser(USER_ID_02, USER_NAME_02)
        val updateMembershipsResult = pubnub.updateMemberships(
            spaceId = SPACE_ID,
            partialMembershipsWithUser = listOf(Membership.Partial(USER_ID_02, MEMBERSHIP_CUSTOM))
        ).sync()
        assertEquals(200, updateMembershipsResult?.status)

        val fetchMembershipsResultAfterUpdate = pubnub.fetchMemberships(
            spaceId = SPACE_ID,
            includeCount = true,
            includeCustom = true,
            includeUserDetails = UserDetailsLevel.USER_WITH_CUSTOM
        ).sync()
        assertEquals(2, fetchMembershipsResultAfterUpdate?.totalCount)
        assertEquals(MEMBERSHIP_CUSTOM, fetchMembershipsResultAfterUpdate?.data?.first()?.custom)
    }

    @Test
    internal fun can_updateMembershipOfSpace_by_adding_membership_custom() {
        createUser(USER_ID, USER_NAME)
        createSpace(SPACE_ID, SPACE_NAME)
        val partialMembershipsWithUser = listOf(Membership.Partial(userId = USER_ID))
        pubnub.addMemberships(spaceId = SPACE_ID, partialMembershipsWithUser = partialMembershipsWithUser).sync()

        pubnub.updateMemberships(
            spaceId = SPACE_ID,
            partialMembershipsWithUser = listOf(Membership.Partial(USER_ID, MEMBERSHIP_CUSTOM))
        ).sync()

        val fetchMembershipsResultAfterUpdate = pubnub.fetchMemberships(
            spaceId = SPACE_ID,
            includeCount = true,
            includeCustom = true,
            includeUserDetails = UserDetailsLevel.USER_WITH_CUSTOM
        ).sync()
        assertEquals(1, fetchMembershipsResultAfterUpdate?.totalCount)
        assertEquals(MEMBERSHIP_CUSTOM, fetchMembershipsResultAfterUpdate?.data?.first()?.custom)
    }

    @Test
    internal fun can_fetch_sorted_userMembership() {
        createUser(USER_ID, USER_NAME)
        createSpace(SPACE_ID, SPACE_NAME)
        createSpace(SPACE_ID_02, SPACE_NAME_02)
        val partialMembershipsWithSpace: List<Membership.PartialWithSpace> =
            listOf(
                Membership.Partial(SPACE_ID, MEMBERSHIP_CUSTOM, MEMBERSHIP_STATUS),
                Membership.Partial(SPACE_ID_02, MEMBERSHIP_CUSTOM, MEMBERSHIP_STATUS)
            )
        pubnub.addMemberships(partialMembershipsWithSpace = partialMembershipsWithSpace, userId = USER_ID).sync()
        listOf(Membership.Partial(SPACE_ID, MEMBERSHIP_CUSTOM), Membership.Partial(SPACE_ID_02, MEMBERSHIP_CUSTOM))
        pubnub.addMemberships(partialMembershipsWithSpace = partialMembershipsWithSpace, userId = USER_ID).sync()

        val membershipsResultSortSpaceIdAsc = pubnub.fetchMemberships(
            userId = USER_ID,
            sort = listOf(ResultSortKey.Asc(key = UserMembershipsResultKey.SPACE_ID))
        ).sync()
        assertEquals(SPACE_ID, membershipsResultSortSpaceIdAsc?.data?.first()?.space?.id)
        assertEquals(MEMBERSHIP_STATUS, membershipsResultSortSpaceIdAsc?.data?.first()?.status)
        assertEquals(SPACE_ID_02, membershipsResultSortSpaceIdAsc?.data?.elementAt(1)?.space?.id)
        assertEquals(MEMBERSHIP_STATUS, membershipsResultSortSpaceIdAsc?.data?.elementAt(1)?.status)

        val membershipsResultSortSpaceIdDesc = pubnub.fetchMemberships(
            userId = USER_ID,
            sort = listOf(ResultSortKey.Desc(key = UserMembershipsResultKey.SPACE_ID))
        ).sync()
        assertEquals(SPACE_ID_02, membershipsResultSortSpaceIdDesc?.data?.first()?.space?.id)
        assertEquals(SPACE_ID, membershipsResultSortSpaceIdDesc?.data?.elementAt(1)?.space?.id)

        val membershipsResultSortSpaceNmeAsc = pubnub.fetchMemberships(
            userId = USER_ID,
            sort = listOf(ResultSortKey.Asc(key = UserMembershipsResultKey.SPACE_NAME)),
            includeSpaceDetails = SpaceDetailsLevel.SPACE_WITH_CUSTOM
        ).sync()
        assertEquals(SPACE_NAME, membershipsResultSortSpaceNmeAsc?.data?.first()?.space?.name)
        assertEquals(SPACE_NAME_02, membershipsResultSortSpaceNmeAsc?.data?.elementAt(1)?.space?.name)

        val membershipsResultSortSpaceNameDesc = pubnub.fetchMemberships(
            userId = USER_ID,
            sort = listOf(ResultSortKey.Desc(key = UserMembershipsResultKey.SPACE_NAME)),
            includeSpaceDetails = SpaceDetailsLevel.SPACE_WITH_CUSTOM
        ).sync()
        assertEquals(SPACE_NAME_02, membershipsResultSortSpaceNameDesc?.data?.first()?.space?.name)
        assertEquals(SPACE_NAME, membershipsResultSortSpaceNameDesc?.data?.elementAt(1)?.space?.name)
    }

    @Test
    internal fun can_fetch_sorted_spaceMembership() {
        createUser(USER_ID, USER_NAME)
        createUser(USER_ID_02, USER_NAME_02)
        createSpace(SPACE_ID, SPACE_NAME)
        val partialMembershipsWithUser =
            listOf(Membership.Partial(USER_ID, MEMBERSHIP_CUSTOM), Membership.Partial(USER_ID_02, MEMBERSHIP_CUSTOM))
        pubnub.addMemberships(spaceId = SPACE_ID, partialMembershipsWithUser = partialMembershipsWithUser).sync()

        val membershipsResultSortUserIdAsc = pubnub.fetchMemberships(
            spaceId = SPACE_ID,
            sort = listOf(ResultSortKey.Asc(key = SpaceMembershipResultKey.USER_ID))
        ).sync()
        assertEquals(USER_ID, membershipsResultSortUserIdAsc?.data?.first()?.user?.id)
        assertEquals(USER_ID_02, membershipsResultSortUserIdAsc?.data?.elementAt(1)?.user?.id)

        val membershipsResultSortUserIdDesc = pubnub.fetchMemberships(
            spaceId = SPACE_ID,
            sort = listOf(ResultSortKey.Desc(key = SpaceMembershipResultKey.USER_ID))
        ).sync()
        assertEquals(USER_ID_02, membershipsResultSortUserIdDesc?.data?.first()?.user?.id)
        assertEquals(USER_ID, membershipsResultSortUserIdDesc?.data?.elementAt(1)?.user?.id)

        val membershipsResultSortUserNameAsc = pubnub.fetchMemberships(
            spaceId = SPACE_ID,
            sort = listOf(ResultSortKey.Asc(key = SpaceMembershipResultKey.USER_NAME)),
            includeUserDetails = UserDetailsLevel.USER_WITH_CUSTOM
        ).sync()
        assertEquals(USER_NAME, membershipsResultSortUserNameAsc?.data?.first()?.user?.name)
        assertEquals(USER_NAME_02, membershipsResultSortUserNameAsc?.data?.elementAt(1)?.user?.name)

        val membershipsResultSortUserNameDesc = pubnub.fetchMemberships(
            spaceId = SPACE_ID,
            sort = listOf(ResultSortKey.Desc(key = SpaceMembershipResultKey.USER_NAME)),
            includeUserDetails = UserDetailsLevel.USER_WITH_CUSTOM
        ).sync()
        assertEquals(USER_NAME_02, membershipsResultSortUserNameDesc?.data?.first()?.user?.name)
        assertEquals(USER_NAME, membershipsResultSortUserNameDesc?.data?.elementAt(1)?.user?.name)
    }

    @Test
    internal fun can_receiveMembershipEventsForUser() {
        val allUpdatesDone = CountDownLatch(2)
        val lastUpdate = mapOf("this" to "that")
        val created = CountDownLatch(1)
        val user = User(id = USER_ID)
        val space = Space(id = SPACE_ID)
        var membership = Membership(
            user = user,
            space = space,
            custom = mapOf(),
            status = null
        )
        val connected = CountDownLatch(1)
        pubnub.addListener(object : SubscribeCallback() {
            override fun status(pubnub: PubNub, pnStatus: PNStatus) {
                if (pnStatus.category == PNStatusCategory.PNConnectedCategory) {
                    connected.countDown()
                }
            }
        })

        pubnub.addMembershipEventsListener {

            if (it is MembershipModified) {
                if (it.data.custom != lastUpdate) {
                    created.countDown()
                }
                membership = membership.copy(
                    user = it.data.user,
                    space = it.data.space,
                    custom = it.data.custom,
                    status = it.data.status
                )
            }
            allUpdatesDone.countDown()
        }
        pubnub.subscribe(channels = listOf(USER_ID.value))
        if (!connected.await(5, TimeUnit.SECONDS)) {
            fail("Didn't connect")
        }

        pubnub.addMemberships(
            spaceId = SPACE_ID,
            partialMembershipsWithUser = listOf(Membership.Partial(userId = USER_ID))
        ).sync()

        if (!created.await(5, TimeUnit.SECONDS)) {
            fail("Didn't receive created event")
        }
        pubnub.updateMemberships(
            spaceId = SPACE_ID,
            partialMembershipsWithUser = listOf(
                Membership.Partial(
                    userId = USER_ID,
                    custom = lastUpdate
                )
            )
        ).sync()
        if (!allUpdatesDone.await(5, TimeUnit.SECONDS)) {
            fail("Didn't receive enough events")
        }
        assertEquals(
            Membership(
                user = user,
                space = space,
                custom = lastUpdate,
                status = null
            ),
            membership
        )
    }

    @AfterEach
    internal fun tearDown() {
        removeEntities()
    }

    private fun removeEntities() {
        pubnub.removeSpace(spaceId = SPACE_ID).sync()
        pubnub.removeSpace(spaceId = SPACE_ID_02).sync()
        pubnub.removeUser(userId = USER_ID).sync()
        pubnub.removeUser(userId = USER_ID_02).sync()
        pubnub.removeMemberships(spaceId = SPACE_ID, listOf(USER_ID, USER_ID_02)).sync()
        pubnub.removeMemberships(spaceId = SPACE_ID_02, listOf(USER_ID, USER_ID_02)).sync()
    }

    private fun createUser(userId: UserId, userName: String): User? {
        return pubnub.createUser(
            userId = userId,
            name = userName,
            externalId = EXTERNAL_ID,
            profileUrl = PROFILE_URL,
            email = EMAIL,
            custom = USER_CUSTOM,
            includeCustom = true,
            status = USER_STATUS,
            type = USER_TYPE
        ).sync()
    }

    private fun createSpace(spaceId: SpaceId, spaceName: String): Space? {
        return pubnub.createSpace(
            spaceId = spaceId,
            name = spaceName,
            description = SPACE_DESCRIPTION,
            custom = SPACE_CUSTOM,
            includeCustom = true,
            status = SPACE_STATUS,
            type = SPACE_TYPE
        ).sync()
    }

    private fun assertMembershipResultOfUser(fetchMembershipsResult: MembershipsResult?) {
        val membership01 = fetchMembershipsResult?.data?.first()
        assertEquals(USER_ID, membership01?.user?.id)
        assertEquals(SPACE_ID, membership01?.space?.id)
        assertEquals(SPACE_NAME, membership01?.space?.name)
        assertEquals(SPACE_DESCRIPTION, membership01?.space?.description)
        assertEquals(SPACE_CUSTOM, membership01?.space?.custom)
        assertTrue(membership01?.space?.updated != null)
        assertTrue(membership01?.space?.eTag != null)
        assertEquals(MEMBERSHIP_CUSTOM, membership01?.custom)
        assertTrue(membership01?.updated != null)
        assertTrue(membership01?.eTag != null)

        val membership02 = fetchMembershipsResult?.data?.elementAt(1)
        assertEquals(USER_ID, membership02?.user?.id)
        assertEquals(SPACE_ID_02, membership02?.space?.id)
        assertEquals(SPACE_NAME_02, membership02?.space?.name)
        assertEquals(SPACE_DESCRIPTION, membership02?.space?.description)
        assertEquals(SPACE_CUSTOM, membership02?.space?.custom)
        assertTrue(membership02?.space?.updated != null)
        assertTrue(membership02?.space?.eTag != null)
        assertTrue(membership02?.updated != null)
        assertTrue(membership02?.eTag != null)

        assertEquals(2, fetchMembershipsResult?.totalCount)
    }

    private fun assertMembershipResultOfSpace(fetchMembershipsResult: MembershipsResult?) {
        val membership01 = fetchMembershipsResult?.data?.first()
        assertEquals(USER_ID, membership01?.user?.id)
        assertEquals(USER_NAME, membership01?.user?.name)
        assertEquals(EXTERNAL_ID, membership01?.user?.externalId)
        assertEquals(PROFILE_URL, membership01?.user?.profileUrl)
        assertEquals(EMAIL, membership01?.user?.email)
        assertEquals(USER_CUSTOM, membership01?.user?.custom)
        assertTrue(membership01?.user?.updated != null)
        assertTrue(membership01?.user?.eTag != null)
        assertEquals(SPACE_ID, membership01?.space?.id)
        assertEquals(MEMBERSHIP_CUSTOM, membership01?.custom)
        assertTrue(membership01?.updated != null)
        assertTrue(membership01?.eTag != null)

        val membership02 = fetchMembershipsResult?.data?.elementAt(1)
        assertEquals(USER_ID_02, membership02?.user?.id)
        assertEquals(USER_NAME_02, membership02?.user?.name)
        assertEquals(EXTERNAL_ID, membership02?.user?.externalId)
        assertEquals(PROFILE_URL, membership02?.user?.profileUrl)
        assertEquals(EMAIL, membership02?.user?.email)
        assertEquals(USER_CUSTOM, membership02?.user?.custom)
        assertTrue(membership02?.user?.updated != null)
        assertTrue(membership02?.user?.eTag != null)
        assertEquals(SPACE_ID, membership02?.space?.id)
        assertEquals(MEMBERSHIP_CUSTOM, membership02?.custom)
        assertTrue(membership02?.updated != null)
        assertTrue(membership02?.eTag != null)

        assertEquals(2, fetchMembershipsResult?.totalCount)
    }
}
