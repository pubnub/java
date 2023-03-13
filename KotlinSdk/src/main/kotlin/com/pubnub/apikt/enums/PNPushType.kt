package com.pubnub.apikt.enums

import java.util.*

enum class PNPushType(s: String) {

    APNS("apns"),
    MPNS("mpns"),
    FCM("gcm"),
    APNS2("apns2");

    private val value: String = s

    fun toParamString(): String {
        return value.lowercase(Locale.US)
    }
}
