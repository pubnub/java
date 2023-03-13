package com.pubnub.user.models.consumer

import com.pubnub.apikt.models.consumer.objects.PNPage
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadataArrayResult
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadataResult

data class UsersResult(
    val data: Collection<User>,
    val totalCount: Int?,
    val next: PNPage?,
    val prev: PNPage?
)

internal fun PNUUIDMetadataArrayResult.toUsersResult(): UsersResult {
    val users = data.map { pnUserMetadata ->
        pnUserMetadata.toUser()
    }
    return UsersResult(data = users, totalCount = totalCount, next = next, prev = prev)
}

internal fun PNUUIDMetadataResult.toUser(): User? {
    return data?.toUser()
}
