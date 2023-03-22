package com.pubnub.api.endpoints.publishKT

import java.util.*


interface TelemetryManagerExternal {
    fun operationsLatency(currentDate: Long = Date().time): Map<String, String>
    fun storeLatency(latency: Long, pnOperationType: String, currentDate: Long = Date().time)
}
