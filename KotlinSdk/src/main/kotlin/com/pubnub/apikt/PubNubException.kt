package com.pubnub.apikt

import retrofit2.Call

/**
 * Custom exception wrapper for errors occurred during execution or processing of a PubNub API operation.
 *
 * @property errorMessage The error message received from the server, if any.
 * @property pubnubError The appropriate matching PubNub error.
 * @property jso The error json received from the server, if any.
 * @property statusCode HTTP status code.
 * @property affectedCall A reference to the affected call. Useful for calling [retry][Endpoint.retry].
 */
data class PubNubException(
    val errorMessage: String? = null,
    val pubnubError: PubNubError? = null,
    val jso: String? = null,
    val statusCode: Int = 0,
    val affectedCall: Call<*>? = null
) : Exception(errorMessage) {

    internal constructor(pubnubError: PubNubError) : this(
        errorMessage = pubnubError.message,
        pubnubError = pubnubError
    )
}
