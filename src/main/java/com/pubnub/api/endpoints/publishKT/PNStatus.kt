package com.pubnub.api.endpoints.publishKT

import okhttp3.Request

/**
 * Metadata related to executed PubNub API operations.
 *
 * @property category The [PNStatusCategory] of an executed operation.
 * @property error Is `true` if the operation didn't succeed. Always check for it in [async][Endpoint.async] blocks.
 * @property operation The concrete API operation type that's been executed.
 * @property exception Error information if the request didn't succeed.
 * @property statusCode HTTP status code.
 * @property tlsEnabled Whether the API operation was executed over HTTPS.
 * @property origin Origin of the HTTP request. See more at [PNConfiguration.origin].
 * @property uuid The UUID which requested the API operation to be executed. See more at [PNConfiguration.uuid].
 * @property authKey The authentication key attached to the request, if needed. See more at [PNConfiguration.authKey].
 * @property affectedChannels List of channels affected by the this API operation.
 * @property affectedChannelGroups List of channel groups affected by the this API operation.
 */
data class PNStatus(
    var category: PNStatusCategory,
    var error: Boolean,
    val operation: PNOperationType,

    val exception: PubNubException? = null,

    var statusCode: Int? = null,
    var tlsEnabled: Boolean? = null,
    var origin: String? = null,
    var uuid: String? = null,
    var authKey: String? = null,

    var affectedChannels: List<String?> = emptyList(),
    var affectedChannelGroups: List<String?> = emptyList()

) {
    internal var executedEndpoint: ExtendedRemoteAction<*>? = null

    var clientRequest: Request? = null

    /**
     * Execute the API operation again.
     */
    fun retry() {
        executedEndpoint?.retry()
    }
}
