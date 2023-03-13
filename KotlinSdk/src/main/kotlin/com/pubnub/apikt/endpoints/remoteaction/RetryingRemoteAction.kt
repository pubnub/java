package com.pubnub.apikt.endpoints.remoteaction

import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicReference

internal class RetryingRemoteAction<T>(
    private val remoteAction: ExtendedRemoteAction<T>,
    private val maxNumberOfAutomaticRetries: Int,
    private val operationType: PNOperationType,
    private val executorService: ExecutorService
) : ExtendedRemoteAction<T> {
    private lateinit var cachedCallback: (result: T?, status: PNStatus) -> Unit

    @Throws(PubNubException::class)
    override fun sync(): T? {
        validate()
        var thrownException: PubNubException? = null
        for (i in 0 until maxNumberOfAutomaticRetries) {
            thrownException = try {
                return remoteAction.sync()
            } catch (ex: PubNubException) {
                ex
            }
        }
        throw thrownException!!
    }

    override fun async(callback: (result: T?, status: PNStatus) -> Unit) {
        cachedCallback = callback
        executorService.execute(
            Runnable {
                try {
                    validate()
                } catch (ex: PubNubException) {
                    callback(
                        null,
                        PNStatus(
                            operation = operationType,
                            error = true,
                            exception = ex,
                            category = PNStatusCategory.PNBadRequestCategory
                        ).apply {
                            executedEndpoint = this@RetryingRemoteAction
                        }
                    )
                    return@Runnable
                }
                var lastResultAndStatus: ResultAndStatus<T>? = null
                for (i in 0 until maxNumberOfAutomaticRetries) {
                    lastResultAndStatus = syncAsync()
                    if (!lastResultAndStatus.status.error) {
                        callback(lastResultAndStatus.result, lastResultAndStatus.status)
                        return@Runnable
                    }
                }
                callback(lastResultAndStatus!!.result, lastResultAndStatus.status)
            }
        )
    }

    override fun retry() {
        async(cachedCallback)
    }

    override fun silentCancel() {
        remoteAction.silentCancel()
    }

    override fun operationType(): PNOperationType {
        return remoteAction.operationType()
    }

    private data class ResultAndStatus<T>(
        val result: T? = null,
        val status: PNStatus
    )

    private fun syncAsync(): ResultAndStatus<T> {
        val latch = CountDownLatch(1)
        val atomicResultAndStatus = AtomicReference<ResultAndStatus<T>>()
        remoteAction.async { result: T?, status: PNStatus ->
            atomicResultAndStatus.set(
                ResultAndStatus(
                    result,
                    status.copy().apply { executedEndpoint = this@RetryingRemoteAction }
                )
            )
            latch.countDown()
        }

        return try {
            latch.await()
            atomicResultAndStatus.get()
        } catch (e: InterruptedException) {
            remoteAction.silentCancel()
            ResultAndStatus(
                null,
                PNStatus(
                    category = PNStatusCategory.PNUnknownCategory,
                    operation = operationType,
                    error = true,
                    exception = PubNubException(errorMessage = e.message)
                ).apply { executedEndpoint = this@RetryingRemoteAction }
            )
        }
    }

    @Throws(PubNubException::class)
    private fun validate() {
        if (maxNumberOfAutomaticRetries < 1) {
            throw PubNubException(PubNubError.INVALID_ARGUMENTS)
        }
    }

    companion object {
        fun <T> autoRetry(
            remoteAction: ExtendedRemoteAction<T>,
            maxNumberOfAutomaticRetries: Int,
            operationType: PNOperationType,
            executorService: ExecutorService
        ): RetryingRemoteAction<T> {
            return RetryingRemoteAction(remoteAction, maxNumberOfAutomaticRetries, operationType, executorService)
        }
    }
}
