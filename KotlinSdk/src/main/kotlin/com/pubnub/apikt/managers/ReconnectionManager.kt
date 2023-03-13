package com.pubnub.apikt.managers

import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.callbacks.ReconnectionCallback
import com.pubnub.apikt.enums.PNReconnectionPolicy
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.pow

internal class ReconnectionManager(val pubnub: PubNub) {

    private val log = LoggerFactory.getLogger("ReconnectionManager")

    private companion object {
        private const val LINEAR_INTERVAL = 3
        private const val MIN_EXPONENTIAL_BACKOFF = 1
        private const val MAX_EXPONENTIAL_BACKOFF = 32

        private const val MILLISECONDS = 1000
    }

    internal lateinit var reconnectionCallback: ReconnectionCallback

    private var exponentialMultiplier = 1
    private var failedCalls = 0

    private lateinit var pnReconnectionPolicy: PNReconnectionPolicy
    private var maxConnectionRetries = -1

    private var timer: Timer? = null

    internal fun startPolling(pnConfiguration: PNConfiguration) {
        pnReconnectionPolicy = pnConfiguration.reconnectionPolicy
        maxConnectionRetries = pnConfiguration.maximumReconnectionRetries

        if (isReconnectionPolicyUndefined())
            return

        exponentialMultiplier = 1
        failedCalls = 0

        registerHeartbeatTimer()
    }

    private fun registerHeartbeatTimer() {
        // make sure only one timer is running at a time.
        stopHeartbeatTimer()

        if (isReconnectionPolicyUndefined()) {
            return
        }

        if (maxConnectionRetries != -1 && failedCalls >= maxConnectionRetries) {
            reconnectionCallback.onMaxReconnectionExhaustion()
            return
        }
        timer = Timer("Reconnection Manager timer", true)
        timer?.schedule(
            object : TimerTask() {
                override fun run() {
                    callTime()
                }
            },
            getBestInterval() * MILLISECONDS.toLong()
        )
    }

    private fun getBestInterval(): Int {
        var timerInterval = LINEAR_INTERVAL
        if (pnReconnectionPolicy == PNReconnectionPolicy.EXPONENTIAL) {
            timerInterval = (2.0.pow(exponentialMultiplier.toDouble()) - 1).toInt()
            if (timerInterval > MAX_EXPONENTIAL_BACKOFF) {
                timerInterval = MIN_EXPONENTIAL_BACKOFF
                exponentialMultiplier = 1
                log.info("timerInterval > MAXEXPONENTIALBACKOFF at: " + Calendar.getInstance().time.toString())
            } else if (timerInterval < 1) {
                timerInterval = MIN_EXPONENTIAL_BACKOFF
            }
            log.info("timerInterval = " + timerInterval + " at: " + Calendar.getInstance().time.toString())
        }
        if (pnReconnectionPolicy == PNReconnectionPolicy.LINEAR) {
            timerInterval = LINEAR_INTERVAL
        }
        return timerInterval
    }

    private fun stopHeartbeatTimer() {
        timer?.cancel()
    }

    private fun callTime() {
        pubnub.time()
            .async { _, status ->
                if (!status.error) {
                    stopHeartbeatTimer()
                    reconnectionCallback.onReconnection()
                } else {
                    log.info("callTime at ${System.currentTimeMillis()}")
                    exponentialMultiplier++
                    failedCalls++
                    registerHeartbeatTimer()
                }
            }
    }

    private fun isReconnectionPolicyUndefined(): Boolean {
        if (pnReconnectionPolicy == PNReconnectionPolicy.NONE) {
            log.info("reconnection policy is disabled, please handle reconnection manually.")
            return true
        }
        return false
    }
}
