package com.pubnub.user

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.UserId
import com.pubnub.apikt.callbacks.DisposableListener
import com.pubnub.apikt.callbacks.SubscribeCallback
import com.pubnub.apikt.endpoints.remoteaction.ComposableRemoteAction.Companion.firstDo
import com.pubnub.apikt.endpoints.remoteaction.ExtendedRemoteAction
import com.pubnub.apikt.endpoints.remoteaction.MappingRemoteAction.Companion.map
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.objects.PNKey
import com.pubnub.apikt.models.consumer.objects.PNPage
import com.pubnub.apikt.models.consumer.objects.PNSortKey
import com.pubnub.apikt.models.consumer.objects.ResultSortKey
import com.pubnub.apikt.models.consumer.pubsub.objects.PNObjectEventResult
import com.pubnub.user.models.consumer.RemoveUserResult
import com.pubnub.user.models.consumer.User
import com.pubnub.user.models.consumer.UserEvent
import com.pubnub.user.models.consumer.UserKey
import com.pubnub.user.models.consumer.UsersResult
import com.pubnub.user.models.consumer.toRemoveUserResult
import com.pubnub.user.models.consumer.toUser
import com.pubnub.user.models.consumer.toUserEvent
import com.pubnub.user.models.consumer.toUsersResult

/**
 * Returns a paginated list of User metadata, optionally including the custom data object for each.
 *
 * @param limit Number of objects to return in the response.
 *              Default is 100, which is also the maximum value.
 *              Set limit to 0 (zero) and includeCount to true if you want to retrieve only a result count.
 * @param page Use for pagination.
 *              - [PNNext] : Previously-returned cursor bookmark for fetching the next page.
 *              - [PNPrev] : Previously-returned cursor bookmark for fetching the previous page.
 *                           Ignored if you also supply the start parameter.
 * @param filter Expression used to filter the results. Only objects whose properties satisfy the given
 *               expression are returned.
 * @param sort List of properties to sort by. Available options are id, name, and updated.
 *             e.g. listOf(ResultSortKey.Asc(key = ResultKey.ID)) , listOf(ResultSortKey.Desc(key = ResultKey.NAME))
 *             @see [Asc], [Desc]
 * @param includeCount Request totalCount to be included in paginated response. By default, totalCount is omitted.
 *                     Default is `false`.
 * @param includeCustom Include respective additional fields in the response.
 */
fun PubNub.fetchUsers(
    limit: Int? = null,
    page: PNPage? = null,
    filter: String? = null,
    sort: Collection<ResultSortKey<UserKey>> = listOf(),
    includeCount: Boolean = false,
    includeCustom: Boolean = false
): ExtendedRemoteAction<UsersResult?> = firstDo(
    getAllUUIDMetadata(
        limit = limit,
        page = page,
        filter = filter,
        sort = toPNSortKey(sort),
        includeCount = includeCount,
        includeCustom = includeCustom
    )
).then {
    map(
        it, PNOperationType.UserOperation
    ) { pnUuidMetadataArrayResult -> pnUuidMetadataArrayResult.toUsersResult() }
}

private fun toPNSortKey(sort: Collection<ResultSortKey<UserKey>>): Collection<PNSortKey<PNKey>> {
    val pnSortKeyList = sort.map { resultSortKey ->
        when (resultSortKey) {
            is ResultSortKey.Asc -> PNSortKey.PNAsc(key = resultSortKey.key.toPNSortKey())
            is ResultSortKey.Desc -> PNSortKey.PNDesc(key = resultSortKey.key.toPNSortKey())
        }
    }
    return pnSortKeyList
}

/**
 * Returns metadata for the specified User, optionally including the custom data object for each.
 *
 * @param userId Unique user identifier. If not supplied then current user’s userId is used.
 * @param includeCustom Include respective additional fields in the response.
 */
fun PubNub.fetchUser(
    userId: UserId = configuration.userId,
    includeCustom: Boolean = false
): ExtendedRemoteAction<User?> = firstDo(
    getUUIDMetadata(
        uuid = userId.value, includeCustom = includeCustom
    )
).then {
    map(
        it, PNOperationType.UserOperation
    ) { pnUuidMetadataResult -> pnUuidMetadataResult.toUser() }
}

/**
 * Create metadata for a User in the database, optionally including the custom data object for each.
 *
 * @param userId Unique user identifier. If not supplied then current user’s userId is used.
 * @param name Display name for the user. Maximum 200 characters.
 * @param externalId User's identifier in an external system
 * @param profileUrl The URL of the user's profile picture
 * @param email The user's email address. Maximum 80 characters.
 * @param custom Object with supported data types.
 * @param includeCustom Include respective additional fields in the response.
 * @param type Type of the space.
 * @param status Status of the space.
 */
fun PubNub.createUser(
    userId: UserId = configuration.userId,
    name: String? = null,
    externalId: String? = null,
    profileUrl: String? = null,
    email: String? = null,
    custom: Map<String, Any>? = null,
    includeCustom: Boolean = false,
    type: String? = null,
    status: String? = null
): ExtendedRemoteAction<User?> = firstDo(
    setUUIDMetadata(
        uuid = userId.value,
        name = name,
        externalId = externalId,
        profileUrl = profileUrl,
        email = email,
        custom = custom,
        includeCustom = includeCustom,
        type = type,
        status = status
    )
).then {
    map(
        it, PNOperationType.UserOperation
    ) { pnUuidMetadataResult -> pnUuidMetadataResult.toUser() }
}

/**
 * Update existing metadata for a User in the database, optionally including the custom data object for each.
 *
 * @param userId Unique user identifier. If not supplied then current user’s userId is used.
 * @param name Display name for the user. Maximum 200 characters.
 * @param externalId User's identifier in an external system
 * @param profileUrl The URL of the user's profile picture
 * @param email The user's email address. Maximum 80 characters.
 * @param custom Object with supported data types.
 * @param includeCustom Include respective additional fields in the response.
 */
fun PubNub.updateUser(
    userId: UserId = configuration.userId,
    name: String? = null,
    externalId: String? = null,
    profileUrl: String? = null,
    email: String? = null,
    custom: Map<String, Any>? = null,
    includeCustom: Boolean = false
): ExtendedRemoteAction<User?> = firstDo(
    setUUIDMetadata(
        uuid = userId.value,
        name = name,
        externalId = externalId,
        profileUrl = profileUrl,
        email = email,
        custom = custom,
        includeCustom = includeCustom
    )
).then {
    map(
        it, PNOperationType.UserOperation
    ) { pnUuidMetadataResult -> pnUuidMetadataResult.toUser() }
}

/**
 * Removes the metadata from a specified User.
 *
 * @param userId Unique user identifier. If not supplied then current user’s userId is used.
 */
fun PubNub.removeUser(
    userId: UserId = configuration.userId,
): ExtendedRemoteAction<RemoveUserResult?> = firstDo(
    removeUUIDMetadata(uuid = userId.value)
).then {
    map(
        it, PNOperationType.UserOperation
    ) { pnRemoveMetadataResult -> pnRemoveMetadataResult.toRemoveUserResult() }
}

/**
 * Add a listener for all user events @see[UserEvent]. To receive any user event it is required to subscribe to
 * a specific userId (passing it as a channel) @see[PubNub.subscribe]
 *
 * @param block Function that will be called for every user event.
 * @return DisposableListener that can be disposed or passed in to PubNub#removeListener method @see[PubNub.removeListener]
 */
fun PubNub.addUserEventsListener(block: (UserEvent) -> Unit): DisposableListener {
    val listener = object : SubscribeCallback() {
        override fun status(pubnub: PubNub, pnStatus: PNStatus) {
        }

        override fun objects(pubnub: PubNub, objectEvent: PNObjectEventResult) {
            objectEvent.toUserEvent()?.let(block)
        }
    }
    addListener(listener)
    return DisposableListener(this, listener)
}
