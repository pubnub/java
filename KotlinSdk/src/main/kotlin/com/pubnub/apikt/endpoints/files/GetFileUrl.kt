package com.pubnub.apikt.endpoints.files

import com.pubnub.apikt.*
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import com.pubnub.apikt.models.consumer.files.PNFileUrlResult
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.ExecutorService

/**
 * @see [PubNub.getFileUrl]
 */
class GetFileUrl(
    private val channel: String,
    private val fileName: String,
    private val fileId: String,
    pubNub: PubNub
) : Endpoint<ResponseBody, PNFileUrlResult>(pubNub) {

    private lateinit var cachedCallback: (result: PNFileUrlResult?, status: PNStatus) -> Unit
    private val executorService: ExecutorService = pubNub.retrofitManager.getTransactionClientExecutorService()

    @Throws(PubNubException::class)
    override fun validateParams() {
        super.validateParams()
        if (channel.isEmpty()) {
            throw PubNubException(PubNubError.CHANNEL_MISSING)
        }
    }

    // this code shouldn't call any outside services to return an url, but on the
    // other hand the code that adds signature uses okhttp request. To produce
    // properly constructed url the code creates a request which isn't executed
    @Throws(PubNubException::class)
    override fun sync(): PNFileUrlResult? {
        return try {
            val baseParams: Map<String, String> = createBaseParams()
            val call: Call<ResponseBody> = pubnub.retrofitManager.filesService
                .downloadFile(
                    pubnub.configuration.subscribeKey,
                    channel,
                    fileId,
                    fileName,
                    baseParams
                )
            val signedRequest = PubNubUtil.signRequest(
                call.request(),
                pubnub.configuration,
                pubnub.timestamp()
            )
            PNFileUrlResult(signedRequest.url.toString())
        } catch (e: Exception) {
            throw PubNubException(errorMessage = e.message)
        }
    }

    // Endpoint class uses OkHttp's asynchronous calls to achieve async. Since in this class
    // the code shouldn't call any outside endpoints it's necessary to achieve asynchronous
    // behavior using other means than OkHttp. That's why the code is using executorService
    // to asynchronously call sync()
    override fun async(callback: (result: PNFileUrlResult?, status: PNStatus) -> Unit) {
        cachedCallback = callback
        executorService.execute {
            try {
                val res: PNFileUrlResult? = sync()
                callback(
                    res,
                    PNStatus(
                        category = PNStatusCategory.PNAcknowledgmentCategory,
                        operation = this.operationType(),
                        error = false
                    )
                )
            } catch (ex: PubNubException) {
                callback(
                    null,
                    PNStatus(
                        category = PNStatusCategory.PNUnknownCategory,
                        operation = this.operationType(),
                        error = true,
                        exception = ex,
                        affectedChannels = getAffectedChannels()
                    ).apply { executedEndpoint = this@GetFileUrl }
                )
            }
        }
    }

    @Throws(PubNubException::class)
    override fun doWork(queryParams: HashMap<String, String>): Call<ResponseBody> {
        throw PubNubException(PubNubError.INTERNAL_ERROR)
    }

    @Throws(PubNubException::class)
    override fun createResponse(input: Response<ResponseBody>): PNFileUrlResult {
        throw PubNubException(PubNubError.INTERNAL_ERROR)
    }

    override fun retry() {
        async(cachedCallback)
    }

    override fun getAffectedChannels() = listOf(channel)
    override fun getAffectedChannelGroups(): List<String> = listOf()
    override fun operationType(): PNOperationType = PNOperationType.FileOperation
    override fun isAuthRequired(): Boolean = true
    override fun isSubKeyRequired(): Boolean = true
    override fun isPubKeyRequired(): Boolean = false
}
