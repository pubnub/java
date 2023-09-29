package com.pubnub.api.crypto.cryptor

class CryptorHeader(
    val sentinel: ByteArray, // 4 bytes
    val version: Byte, // 1 byte
    val cryptorId: ByteArray, // 4 bytes
    val cryptorDataSize: ByteArray, // 1 or 3 bytes
    val cryptorData: ByteArray // 0-65535 bytes
) {

    fun toByteArray(): ByteArray {
        return sentinel + version + cryptorId + cryptorDataSize + cryptorData
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CryptorHeader

        if (!sentinel.contentEquals(other.sentinel)) return false
        if (version != other.version) return false
        if (!cryptorId.contentEquals(other.cryptorId)) return false
        if (!cryptorDataSize.contentEquals(other.cryptorDataSize)) return false
        if (!cryptorData.contentEquals(other.cryptorData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sentinel.contentHashCode()
        result = 31 * result + version
        result = 31 * result + cryptorId.contentHashCode()
        result = 31 * result + cryptorDataSize.contentHashCode()
        result = 31 * result + cryptorData.contentHashCode()
        return result
    }
}
