package com.pubnub.apikt.endpoints.pubsub

import com.pubnub.apikt.Endpoint
import com.pubnub.apikt.PNConfiguration.Companion.isValid
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNPublishResult
import com.pubnub.apikt.vendor.Crypto
import com.pubnub.extension.numericString
import com.pubnub.extension.valueString
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.publish]
 */
class Publish internal constructor(
    pubnub: PubNub,
    val message: Any,
    val channel: String,
    val meta: Any? = null,
    val shouldStore: Boolean? = null,
    val usePost: Boolean = false,
    val replicate: Boolean = true,
    val ttl: Int? = null
) : Endpoint<List<Any>, PNPublishResult>(pubnub) {

    private val useEncryption: Boolean = pubnub.configuration.cipherKey.isValid()

    override fun validateParams() {
        super.validateParams()
        if (channel.isBlank()) throw PubNubException(PubNubError.CHANNEL_MISSING)
    }

    override fun getAffectedChannels() = listOf(channel)

    override fun doWork(queryParams: HashMap<String, String>): Call<List<Any>> {

        addQueryParams(queryParams)

        return if (usePost) {
            val payload = getBodyMessage(message, useEncryption)

            pubnub.retrofitManager.publishService.publishWithPost(
                pubnub.configuration.publishKey,
                pubnub.configuration.subscribeKey,
                channel,
                payload,
                queryParams
            )
        } else {
            // HTTP GET request
            val stringifiedMessage = getParamMessage(message, useEncryption)

            pubnub.retrofitManager.publishService.publish(
                pubnub.configuration.publishKey,
                pubnub.configuration.subscribeKey,
                channel,
                stringifiedMessage,
                queryParams
            )
        }
    }

    override fun createResponse(input: Response<List<Any>>): PNPublishResult =
        PNPublishResult(
            timetoken = input.body()!![2].toString().toLong()
        )

    override fun operationType() = PNOperationType.PNPublishOperation

    override fun isPubKeyRequired() = true

    // region Parameters
    /**
     * Add query params to passed HashMap
     *
     * @param queryParams hashMap to add parameters
     */
    private fun addQueryParams(queryParams: MutableMap<String, String>) {

        meta?.run { queryParams["meta"] = pubnub.mapper.toJson(this) }

        shouldStore?.run { queryParams["store"] = this.numericString }

        ttl?.run { queryParams["ttl"] = this.toString() }

        if (!replicate) queryParams["norep"] = true.valueString

        queryParams["seqn"] = pubnub.publishSequenceManager.nextSequence().toString()
    }
    // endregion

    // region Message parsers
    private fun getBodyMessage(message: Any, useEncryption: Boolean): Any =
        if (useEncryption) prepareMessage(message, useEncryption)
        else message

    private fun getParamMessage(message: Any, useEncryption: Boolean): String =
        prepareMessage(message, useEncryption, true)

    /**
     * Create stringified message from passed text and apply encryption if needed
     *
     * @param message any object
     * @param useEncryption should encrypt message
     * @param wrapEncrypted should add double quotes to escaped message
     */
    private fun prepareMessage(
        message: Any,
        useEncryption: Boolean,
        wrapEncrypted: Boolean = false
    ): String =
        pubnub.mapper.toJson(message).also { json ->
            if (useEncryption)
                return encryptMessage(json).also { encrypted ->
                    if (wrapEncrypted)
                        return "\"$encrypted\""
                }
        }

    private fun encryptMessage(message: String): String =
        Crypto(pubnub.configuration.cipherKey, pubnub.configuration.useRandomInitializationVector)
            .encrypt(message)
    // endregion
}
