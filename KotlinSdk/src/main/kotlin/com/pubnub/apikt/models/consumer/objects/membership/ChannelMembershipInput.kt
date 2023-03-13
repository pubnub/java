package com.pubnub.apikt.models.consumer.objects.membership

interface ChannelMembershipInput {
    val channel: String
    val custom: Any?
    val status: String?
}
