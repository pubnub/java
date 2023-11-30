package com.pubnub.api.managers.token_manager

import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.model.ByteString
import co.nstant.`in`.cbor.model.NegativeInteger
import co.nstant.`in`.cbor.model.UnsignedInteger
import com.pubnub.api.PubNubException
import com.pubnub.api.builder.PubNubErrorBuilder
import com.pubnub.api.models.consumer.access_manager.v3.PNToken
import com.pubnub.api.vendor.Base64
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import co.nstant.`in`.cbor.model.Map as CborMap

internal class TokenParser {

    private fun getException(message: String) = PubNubException(
        message, PubNubErrorBuilder.PNERROBJ_INVALID_ACCESS_TOKEN, null, null, 0, null, null
    )

    @Throws(PubNubException::class)
    fun unwrapToken(token: String): PNToken {
        val byteArray = Base64.decode(token.toByteArray(StandardCharsets.UTF_8), Base64.URL_SAFE)
        val firstElement = CborDecoder(byteArray.inputStream()).decode().firstOrNull() ?: throw getException("Empty token")

        val firstLevelMap = (firstElement as? CborMap)?.toJvmMap() ?: throw getException("First element is not a map")
        val version = firstLevelMap[VERSION_KEY]?.toString()?.toInt() ?: throw getException("Couldn't parse version")
        val timestamp = firstLevelMap[TIMESTAMP_KEY]?.toString()?.toLong() ?: throw getException("Couldn't parse timestamp")
        val ttl = firstLevelMap[TTL_KEY]?.toString()?.toLong() ?: throw getException("Couldn't parse ttl")
        val resourcesValue = firstLevelMap[RESOURCES_KEY] as? Map<*, *> ?: throw getException("Resources are not present or are not map")
        val patternsValue = firstLevelMap[PATTERNS_KEY] as? Map<*, *> ?: throw getException("Patterns are not present or are not map")

        return try {
            PNToken.of(
                version,
                timestamp,
                ttl,
                resourcesValue.toPNTokenResources(),
                patternsValue.toPNTokenResources(),
                firstLevelMap[AUTHORIZED_UUID_KEY]?.toString(),
                firstLevelMap[META_KEY],
            )
        } catch (e: Exception) {
            if (e is PubNubException) throw e
            throw getException("Couldn't parse token: ${e.message}")
        }
    }

    private fun CborMap.toJvmMap(depth: Int = 0): MutableMap<String, Any> {
        if (depth > 3) {
            throw getException("Token is too deep")
        }
        val result = mutableMapOf<String, Any>()
        for (key in this.keys) {
            val value = this.get(key)
            val keyString = when (key) {
                is ByteString -> key.bytes.toString(StandardCharsets.UTF_8)
                else -> key.toString()
            }

            when (value) {
                is CborMap -> result[keyString] = value.toJvmMap(depth + 1)
                is ByteString -> result[keyString] = value.bytes
                is List<*> -> result[keyString] = value.map { it.toString() }
                is UnsignedInteger -> result[keyString] = value.value
                is NegativeInteger -> result[keyString] = value.value
                else -> result[keyString] = value.toString()
            }
        }
        return result
    }

    private fun Map<*, *>.toMapOfStringToInt(): Map<String, Int> {
        return mapNotNull { (k, v) ->
            when (v) {
                is BigInteger -> k.toString() to v.toInt()
                else -> v.toString().toIntOrNull()?.let { k.toString() to it }
            }
        }.toMap()
    }

    private fun Map<*, *>.toPNTokenResources(): PNToken.PNTokenResources {
        val channels = (this[CHANNELS_KEY] as? Map<*, *>)?.toMapOfStringToInt() ?: emptyMap()
        val groups = (this[GROUPS_KEY] as? Map<*, *>)?.toMapOfStringToInt() ?: emptyMap()
        val uuids = (this[UUIDS_KEY] as? Map<*, *>)?.toMapOfStringToInt() ?: emptyMap()

        return PNToken.PNTokenResources.of(
            channels.mapValues { (_, v) -> PNToken.PNResourcePermissions.of(v) },
            groups.mapValues { (_, v) -> PNToken.PNResourcePermissions.of(v) },
            uuids.mapValues { (_, v) -> PNToken.PNResourcePermissions.of(v) }
        )
    }

    companion object {
        private const val VERSION_KEY = "v"
        private const val TIMESTAMP_KEY = "t"
        private const val TTL_KEY = "ttl"
        private const val AUTHORIZED_UUID_KEY = "uuid"
        private const val RESOURCES_KEY = "res"
        private const val PATTERNS_KEY = "pat"
        private const val META_KEY = "meta"
        private const val CHANNELS_KEY = "chan"
        private const val GROUPS_KEY = "grp"
        private const val UUIDS_KEY = "uuid"
    }
}