package com.pubnub.apikt.endpoints.remoteaction

import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.enums.PNStatusCategory
import com.pubnub.apikt.models.consumer.PNStatus

class ComposableRemoteAction<T, U>(
    private val remoteAction: ExtendedRemoteAction<T>,
    private val createNextRemoteAction: (T) -> ExtendedRemoteAction<U>,
    private var checkpoint: Boolean
) : ExtendedRemoteAction<U> {
    private var nextRemoteAction: ExtendedRemoteAction<U>? = null
    private var isCancelled = false
    fun <Y> then(factory: (U) -> ExtendedRemoteAction<Y>): ComposableRemoteAction<U, Y> {
        return ComposableRemoteAction(this, factory, false)
    }

    @Synchronized
    fun checkpoint(): ComposableRemoteAction<T, U> {
        checkpoint = true
        return this
    }

    @Throws(PubNubException::class)
    override fun sync(): U? {
        return remoteAction.sync()?.let { result ->
            createNextRemoteAction(result).sync()
        }
    }

    override fun async(callback: (result: U?, status: PNStatus) -> Unit) {
        remoteAction.async { r: T?, s: PNStatus ->
            if (s.error) {
                callback(null, switchRetryReceiver(s))
            } else {
                try {
                    synchronized(this) {
                        if (!isCancelled) {
                            val newNextRemoteAction =
                                createNextRemoteAction(r!!) // if s is not error r shouldn't be null
                            nextRemoteAction = newNextRemoteAction
                            newNextRemoteAction.async { r2: U?, s2: PNStatus ->
                                if (s2.error) {
                                    callback(null, switchRetryReceiver(s2))
                                } else {
                                    callback(r2, switchRetryReceiver(s2))
                                }
                            }
                        }
                    }
                } catch (ex: PubNubException) {
                    callback(
                        null,
                        PNStatus(
                            category = PNStatusCategory.PNBadRequestCategory,
                            error = true,
                            operation = operationType()
                        )
                    )
                }
            }
        }
    }

    private fun switchRetryReceiver(s: PNStatus): PNStatus {
        return s.copy().apply { executedEndpoint = this@ComposableRemoteAction }
    }

    @Synchronized
    override fun retry() {
        if (checkpoint && nextRemoteAction != null) {
            nextRemoteAction!!.retry()
        } else {
            remoteAction.retry()
        }
    }

    @Synchronized
    override fun silentCancel() {
        isCancelled = true
        remoteAction.silentCancel()
        if (nextRemoteAction != null) {
            nextRemoteAction!!.silentCancel()
        }
    }

    override fun operationType(): PNOperationType {
        return nextRemoteAction?.operationType() ?: remoteAction.operationType()
    }

    class ComposableBuilder<T>(private val remoteAction: ExtendedRemoteAction<T>) {
        private var checkpoint = false
        fun <U> then(factory: (T) -> ExtendedRemoteAction<U>): ComposableRemoteAction<T, U> {
            return ComposableRemoteAction(remoteAction, factory, checkpoint)
        }

        fun checkpoint(): ComposableBuilder<T> {
            checkpoint = true
            return this
        }
    }

    companion object {
        fun <T> firstDo(remoteAction: ExtendedRemoteAction<T>): ComposableBuilder<T> {
            return ComposableBuilder(remoteAction)
        }
    }
}
