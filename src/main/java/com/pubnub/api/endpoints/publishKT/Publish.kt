package com.pubnub.api.endpoints.publishKT

import com.pubnub.extension.numericString
import com.pubnub.extension.valueString
import retrofit2.Call
import retrofit2.Response

/**
 * @see [PubNub.publish]
 */
class Publish internal @JvmOverloads constructor(
//    pubnub: PubNub,
    val message: Any,
    val channel: String,
    val meta: Any? = null,
    val shouldStore: Boolean? = null,
    val usePost: Boolean = false,
    val replicate: Boolean = true,
    val ttl: Int? = null,
    val publishServiceExternal: PublishServiceExternal,
    val configurationNeededForPublish: ConfigurationNeededForPublish,
    val toJsonMapper: ToJsonMapper,
    val sequenceManagerExternal: SequenceManagerExternal,
    telemetryManagerExternal: TelemetryManagerExternal,
    configurationNeededForEndpoint: ConfigurationNeededForEndpoint,
    pnInstanceIdProvider: PNInstanceIdProvider,
    tokenRetriever: TokenRetriever,
    jsonMapperManagerForEndpoint: JsonMapperManagerForEndpoint

) : EndpointWithNoPubNub<List<Any>, com.pubnub.api.endpoints.publishKT.PNPublishResult>(
//    pubnub,
    telemetryManagerExternal,
    configurationNeededForEndpoint,
    pnInstanceIdProvider,
    tokenRetriever,
    jsonMapperManagerForEndpoint
) {

    //    private val useEncryption: Boolean = pubnub.configuration.cipherKey.isValid()
    private val useEncryption: Boolean = configurationNeededForPublish.useEncryption()

    override fun validateParams() {
        super.validateParams()
        if (channel.isBlank()) throw PubNubException(PubNubError.CHANNEL_MISSING)
    }

    override fun getAffectedChannels() = listOf(channel)

    override fun doWork(queryParams: HashMap<String, String>): Call<List<Any>> {

        addQueryParams(queryParams)

        return if (usePost) {
            val payload = getBodyMessage(message, useEncryption)

            publishServiceExternal.publishWithPost(
                configurationNeededForPublish.getPublishKey(),
                configurationNeededForPublish.getSubscribeKey(),
                channel,
                payload,
                queryParams
            )
        } else {
            // HTTP GET request
            val stringifiedMessage = getParamMessage(message, useEncryption)

            publishServiceExternal.publish(
                configurationNeededForPublish.getPublishKey(),
                configurationNeededForPublish.getSubscribeKey(),
                channel,
                stringifiedMessage,
                queryParams
            )
        }
    }

    override fun createResponse(input: Response<List<Any>>): com.pubnub.api.endpoints.publishKT.PNPublishResult =
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

        meta?.run { queryParams["meta"] = toJsonMapper.toJson(this) }

        shouldStore?.run { queryParams["store"] = this.numericString }

        ttl?.run { queryParams["ttl"] = this.toString() }

        if (!replicate) queryParams["norep"] = true.valueString

        queryParams["seqn"] = sequenceManagerExternal.nextSequence().toString()
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
        toJsonMapper.toJson(message).also { json ->
            if (useEncryption)
                return encryptMessage(json).also { encrypted ->
                    if (wrapEncrypted)
                        return "\"$encrypted\""
                }
        }

    private fun encryptMessage(message: String): String =
//        Crypto(pubnub.configuration.cipherKey, pubnub.configuration.useRandomInitializationVector)
//            .encrypt(message)
        Crypto(
            configurationNeededForPublish.getCipherKey(),
            configurationNeededForPublish.useRandomInitializationVector()
        )
            .encrypt(message)

    // endregion
}
