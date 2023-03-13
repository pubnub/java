package com.pubnub.apikt.models.consumer.objects.member

interface MemberInput {
    val uuid: String
    val custom: Any?
    val status: String?
}
