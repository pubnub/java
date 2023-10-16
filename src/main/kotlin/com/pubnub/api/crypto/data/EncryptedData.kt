package com.pubnub.api.crypto.data

data class EncryptedData(
    val metadata: ByteArray? = null,
    val data: ByteArray
)
