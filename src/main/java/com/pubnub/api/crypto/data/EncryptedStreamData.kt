package com.pubnub.api.crypto.data

import java.io.InputStream

data class EncryptedStreamData(
    val metadata: ByteArray? = null,
    val stream: InputStream
)
