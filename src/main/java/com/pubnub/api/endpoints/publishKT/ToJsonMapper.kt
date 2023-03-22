package com.pubnub.api.endpoints.publishKT

interface ToJsonMapper {
    fun toJson(input: Any?): String
}
