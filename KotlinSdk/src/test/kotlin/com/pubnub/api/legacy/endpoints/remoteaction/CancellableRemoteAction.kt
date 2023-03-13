package com.pubnub.apikt.legacy.endpoints.remoteaction

import com.pubnub.apikt.endpoints.remoteaction.ExtendedRemoteAction
import com.pubnub.apikt.models.consumer.PNStatus
import java.util.concurrent.Executors

internal interface CancellableRemoteAction<T> : ExtendedRemoteAction<T> {
    override fun sync(): T? {
        return null
    }

    override fun retry() {}

    fun doAsync(callback: (result: T?, status: PNStatus) -> Unit)
    override fun async(callback: (result: T?, status: PNStatus) -> Unit) {
        Executors.newSingleThreadExecutor()
            .execute { doAsync(callback) }
    }
}
