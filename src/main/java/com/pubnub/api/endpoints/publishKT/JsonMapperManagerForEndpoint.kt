package com.pubnub.api.endpoints.publishKT

import com.google.gson.JsonElement

interface JsonMapperManagerForEndpoint {
    fun isJsonObject(element: JsonElement): Boolean
    fun hasField(element: JsonElement, field: String): Boolean
    fun getField(element: JsonElement?, field: String): JsonElement?
    fun elementToString(element: JsonElement?): String?
    fun getArrayIterator(element: JsonElement, field: String): MutableIterator<JsonElement>
    fun <T> fromJson(input: String?, clazz: Class<T>): T
    fun toJson(input: Any?): String
}
