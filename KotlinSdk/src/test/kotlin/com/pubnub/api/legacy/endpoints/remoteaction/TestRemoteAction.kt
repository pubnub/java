package com.pubnub.apikt.legacy.endpoints.remoteaction

import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.endpoints.remoteaction.ExtendedRemoteAction
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class TestRemoteAction<Output> internal constructor(
    private val output: Output?,
    private val failingStrategy: FailingStrategy
) : ExtendedRemoteAction<Output> {
    private val executor: Executor = Executors.newSingleThreadExecutor()
    private val asyncCallmeter = AtomicInteger(0)
    private val callsToFail: AtomicInteger
    private lateinit var callback: (result: Output?, status: PNStatus) -> Unit

    @Throws(PubNubException::class)
    override fun sync(): Output {
        return if (failingStrategy == FailingStrategy.ALWAYS_FAIL) {
            throw PubNubException(PubNubError.INTERNAL_ERROR)
        } else if (failingStrategy == FailingStrategy.FAIL_FIRST_CALLS && callsToFail.getAndDecrement() > 0) {
            throw PubNubException(PubNubError.INTERNAL_ERROR)
        } else {
            output!!
        }
    }

    override fun async(callback: (result: Output?, status: PNStatus) -> Unit) {
        this.callback = callback
        asyncCallmeter.incrementAndGet()
        executor.execute {
            if (failingStrategy == FailingStrategy.ALWAYS_FAIL) {
                callback(
                    null,
                    PNStatus(
                        error = true,
                        operation = operationType(),
                        category = PNStatusCategory.PNBadRequestCategory
                    )
                )
            } else if (failingStrategy == FailingStrategy.FAIL_FIRST_CALLS && callsToFail.getAndDecrement() > 0) {
                callback(
                    null,
                    PNStatus(
                        error = true,
                        operation = operationType(),
                        category = PNStatusCategory.PNBadRequestCategory
                    )
                )
            } else {
                callback(
                    output,
                    PNStatus(
                        error = false,
                        operation = operationType(),
                        category = PNStatusCategory.PNAcknowledgmentCategory
                    )
                )
            }
        }
    }

    override fun retry() {
        async(callback)
    }

    override fun silentCancel() {}
    fun howManyTimesAsyncCalled(): Int {
        return asyncCallmeter.get()
    }

    internal enum class FailingStrategy(var numberOfCalls: Int) {
        NEVER_FAIL(0), ALWAYS_FAIL(0), FAIL_FIRST_CALLS(1);
    }

    companion object {
        fun <T> failing(): TestRemoteAction<T> {
            return TestRemoteAction(
                null,
                FailingStrategy.ALWAYS_FAIL
            )
        }

        fun <T> failingFirstCall(output: T): TestRemoteAction<T> {
            return TestRemoteAction(
                output,
                FailingStrategy.FAIL_FIRST_CALLS
            )
        }

        fun <T> successful(
            output: T
        ): TestRemoteAction<T> {
            return TestRemoteAction(
                output,
                FailingStrategy.NEVER_FAIL
            )
        }
    }

    init {
        callsToFail = AtomicInteger(failingStrategy.numberOfCalls)
    }

    override fun operationType(): PNOperationType {
        return PNOperationType.FileOperation
    }
}
