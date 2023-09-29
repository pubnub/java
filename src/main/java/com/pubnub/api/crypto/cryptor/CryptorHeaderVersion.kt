package com.pubnub.api.crypto.cryptor

enum class CryptorHeaderVersion(val value: Int) {
    One(1);

    companion object {
        fun fromValue(value: Int): CryptorHeaderVersion? {
            return values().find { it.value == value }
        }
    }
}
