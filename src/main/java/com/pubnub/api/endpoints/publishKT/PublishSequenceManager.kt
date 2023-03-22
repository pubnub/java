package com.pubnub.api.endpoints.publishKT

import java.util.concurrent.atomic.AtomicInteger

internal class PublishSequenceManager(private val maxSequence: Int) {

    private val atomicSeq = AtomicInteger(1)

    internal fun nextSequence(): Int = atomicSeq.getAndUpdate {
        if (maxSequence == it) {
            1
        } else {
            it + 1
        }
    }
}