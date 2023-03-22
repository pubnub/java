package com.pubnub.api.endpoints.publishKT

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
