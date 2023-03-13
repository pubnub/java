package com.pubnub.apikt.managers

import com.pubnub.apikt.enums.PNOperationType
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*
import kotlin.collections.set

class TelemetryManager {

    private companion object {
        private const val MAX_FRACTION_DIGITS = 3
        private const val TIMESTAMP_DIVIDER = 1_000
        private const val MAXIMUM_LATENCY_DATA_AGE = 60.0
    }

    private val latencies: HashMap<String, MutableList<Latency>> = HashMap()
    private val numberFormat by lazy {
        NumberFormat.getNumberInstance(Locale.US).apply {
            maximumFractionDigits = MAX_FRACTION_DIGITS
            roundingMode = RoundingMode.HALF_UP
            isGroupingUsed = false
        }
    }

    @Synchronized
    internal fun operationsLatency(currentDate: Long = Date().time): Map<String, String> {
        cleanUpTelemetryData(currentDate)
        val operationLatencies = HashMap<String, String>()
        latencies.entries.forEach {
            val latencyKey = "l_${it.key}"
            val endpointAverageLatency = averageLatencyFromData(it.value)
            if (endpointAverageLatency > 0.0f) {
                operationLatencies[latencyKey] = numberFormat.format(endpointAverageLatency)
            }
        }
        return operationLatencies
    }

    @Synchronized
    private fun cleanUpTelemetryData(currentDate: Long = Date().time) {
        val date = currentDate / TIMESTAMP_DIVIDER.toDouble()

        // remove outdated latencies
        latencies.forEach { (_, operationLatencies) ->
            val outdated = operationLatencies.filter { it.isOutdated(date) }
            operationLatencies -= outdated
        }

        // remove empty latency list
        latencies.filterValues { it.isNullOrEmpty() }
            .forEach { (endpoint, _) ->
                latencies -= endpoint
            }
    }

    private fun averageLatencyFromData(endpointLatencies: List<Latency>): Double {
        val sumOfLatencies = endpointLatencies.sumOf { it.latency }
        return sumOfLatencies / endpointLatencies.size
    }

    @Synchronized
    internal fun storeLatency(latency: Long, type: PNOperationType, currentDate: Long = Date().time) {
        type.queryParam?.let { queryParam: String ->
            if (latency > 0) {
                val storeDate = currentDate / (TIMESTAMP_DIVIDER.toDouble())
                if (latencies[queryParam] == null) {
                    latencies[queryParam] = ArrayList()
                }

                latencies[queryParam]?.let {
                    val latencyEntry = Latency(
                        date = storeDate,
                        latency = latency.toDouble() / TIMESTAMP_DIVIDER
                    )
                    it.add(latencyEntry)
                }
            }
        }
    }

    private data class Latency(val latency: Double, val date: Double) {
        fun isOutdated(currentDate: Double) = currentDate - date > MAXIMUM_LATENCY_DATA_AGE
    }
}
