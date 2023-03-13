package com.pubnub.apikt.managers

import com.pubnub.apikt.PubNub
import com.pubnub.apikt.enums.PNLogVerbosity
import com.pubnub.apikt.interceptor.SignatureInterceptor
import com.pubnub.apikt.services.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

class RetrofitManager(val pubnub: PubNub) {

    private lateinit var transactionClientInstance: OkHttpClient

    private lateinit var subscriptionClientInstance: OkHttpClient

    private lateinit var noSignatureClientInstance: OkHttpClient

    private var signatureInterceptor: SignatureInterceptor

    internal val timeService: TimeService
    internal val publishService: PublishService
    internal val historyService: HistoryService
    internal val presenceService: PresenceService
    internal val messageActionService: MessageActionService
    internal val signalService: SignalService
    internal val channelGroupService: ChannelGroupService
    internal val pushService: PushService
    internal val accessManagerService: AccessManagerService

    internal val subscribeService: SubscribeService
    internal val objectsService: ObjectsService
    internal val filesService: FilesService
    internal val s3Service: S3Service

    init {
        signatureInterceptor = SignatureInterceptor(pubnub)

        if (!pubnub.configuration.googleAppEngineNetworking) {
            transactionClientInstance = createOkHttpClient(pubnub.configuration.nonSubscribeRequestTimeout)
            subscriptionClientInstance = createOkHttpClient(pubnub.configuration.subscribeTimeout)
            noSignatureClientInstance = createOkHttpClient(pubnub.configuration.nonSubscribeRequestTimeout, withSignature = false)
        }

        val transactionInstance = createRetrofit(transactionClientInstance)
        val subscriptionInstance = createRetrofit(subscriptionClientInstance)
        val noSignatureInstance = createRetrofit(noSignatureClientInstance)

        timeService = transactionInstance.create(TimeService::class.java)
        publishService = transactionInstance.create(PublishService::class.java)
        historyService = transactionInstance.create(HistoryService::class.java)
        presenceService = transactionInstance.create(PresenceService::class.java)
        messageActionService = transactionInstance.create(MessageActionService::class.java)
        signalService = transactionInstance.create(SignalService::class.java)
        channelGroupService = transactionInstance.create(ChannelGroupService::class.java)
        pushService = transactionInstance.create(PushService::class.java)
        accessManagerService = transactionInstance.create(AccessManagerService::class.java)
        objectsService = transactionInstance.create(ObjectsService::class.java)
        filesService = transactionInstance.create(FilesService::class.java)
        s3Service = noSignatureInstance.create(S3Service::class.java)

        subscribeService = subscriptionInstance.create(SubscribeService::class.java)
    }

    fun getTransactionClientExecutorService(): ExecutorService {
        return transactionClientInstance.dispatcher.executorService
    }

    private fun createOkHttpClient(readTimeout: Int, withSignature: Boolean = true): OkHttpClient {
        val okHttpBuilder = OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
            .connectTimeout(pubnub.configuration.connectTimeout.toLong(), TimeUnit.SECONDS)

        with(pubnub.configuration) {
            if (logVerbosity == PNLogVerbosity.BODY) {
                okHttpBuilder.addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }

            if (httpLoggingInterceptor != null) {
                okHttpBuilder.addInterceptor(httpLoggingInterceptor!!)
            }

            if (sslSocketFactory != null && x509ExtendedTrustManager != null) {
                okHttpBuilder.sslSocketFactory(
                    pubnub.configuration.sslSocketFactory!!,
                    pubnub.configuration.x509ExtendedTrustManager!!
                )
            }
            connectionSpec?.let { okHttpBuilder.connectionSpecs(listOf(it)) }
            hostnameVerifier?.let { okHttpBuilder.hostnameVerifier(it) }
            proxy?.let { okHttpBuilder.proxy(it) }
            proxySelector?.let { okHttpBuilder.proxySelector(it) }
            proxyAuthenticator?.let { okHttpBuilder.proxyAuthenticator(it) }
            certificatePinner?.let { okHttpBuilder.certificatePinner(it) }
        }

        if (withSignature) {
            okHttpBuilder.addInterceptor(signatureInterceptor)
        }

        val okHttpClient = okHttpBuilder.build()

        pubnub.configuration.maximumConnections?.let { okHttpClient.dispatcher.maxRequestsPerHost = it }

        return okHttpClient
    }

    private fun createRetrofit(callFactory: Call.Factory): Retrofit {
        val retrofitBuilder = Retrofit.Builder()
            .callFactory(callFactory)
            .baseUrl(pubnub.baseUrl())
            .addConverterFactory(pubnub.mapper.converterFactory)

        return retrofitBuilder.build()
    }

    fun destroy(force: Boolean = false) {
        closeExecutor(transactionClientInstance, force)
        closeExecutor(subscriptionClientInstance, force)
        closeExecutor(noSignatureClientInstance, force)
    }

    private fun closeExecutor(client: OkHttpClient, force: Boolean) {
        client.dispatcher.cancelAll()
        if (force) {
            client.connectionPool.evictAll()
            val executorService = client.dispatcher.executorService
            executorService.shutdown()
        }
    }
}
