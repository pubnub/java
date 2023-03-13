package com.pubnub.apikt.managers

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.cbor.CBORFactory
import com.pubnub.apikt.PubNubError
import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.models.consumer.access_manager.v3.PNToken
import com.pubnub.apikt.vendor.Base64
import java.io.IOException
import java.nio.charset.StandardCharsets

internal class TokenParser {
    private val mapper = ObjectMapper(CBORFactory()).apply {
        configOverride(Map::class.java).setterInfo = JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    fun unwrapToken(token: String): PNToken {
        return try {
            val byteArray = Base64.decode(token.toByteArray(StandardCharsets.UTF_8), Base64.URL_SAFE)
            mapper.readValue(byteArray, PNToken::class.java)
        } catch (e: IOException) {
            throw PubNubException(pubnubError = PubNubError.INVALID_ACCESS_TOKEN, errorMessage = e.message)
        }
    }
}
