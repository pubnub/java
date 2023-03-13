package com.pubnub.apikt.callbacks

internal abstract class ReconnectionCallback {
    abstract fun onReconnection()
    abstract fun onMaxReconnectionExhaustion()
}
