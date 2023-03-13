package com.pubnub.apikt.endpoints.remoteaction

import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.enums.PNOperationType
import com.pubnub.apikt.models.consumer.PNStatus

interface ExtendedRemoteAction<Output> : RemoteAction<Output> {
    fun operationType(): PNOperationType
    fun retry()
}

interface RemoteAction<Output> : Cancelable {
    @Throws(PubNubException::class)
    fun sync(): Output?
    fun async(callback: (result: Output?, status: PNStatus) -> Unit)
}

interface Cancelable {
    fun silentCancel()
}
