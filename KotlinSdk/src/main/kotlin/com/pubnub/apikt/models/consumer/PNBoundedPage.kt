package com.pubnub.apikt.models.consumer

/**
 * The paging object used for pagination
 *
 * @param start Timetoken denoting the start of the range requested
 *              (return values will be less than start).
 * @param end Timetoken denoting the end of the range requested
 *            (return values will be greater than or equal to end).
 * @param limit Specifies the number of items to return in response.
 */
data class PNBoundedPage(
    val start: Long? = null,
    val end: Long? = null,
    val limit: Int? = null
)
