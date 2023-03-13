package com.pubnub.apikt.models.consumer.pubsub.objects

interface ObjectResult<T> {
    val event: String
    val data: T
}
