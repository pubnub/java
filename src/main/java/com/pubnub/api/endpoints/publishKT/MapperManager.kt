package com.pubnub.api.endpoints.publishKT


import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Converter
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type

class MapperManager {

    private val objectMapper: Gson
    internal val converterFactory: Converter.Factory
    private val jacksonObjectMapper = jacksonObjectMapper()

    init {
        val booleanAsIntAdapter = object : TypeAdapter<Boolean>() {
            override fun write(out: JsonWriter?, value: Boolean?) {
                if (value == null) {
                    out?.nullValue()
                } else {
                    out?.value(value)
                }
            }

            override fun read(_in: JsonReader): Boolean? {
                val peek: JsonToken = _in.peek()
                return when (peek) {
                    JsonToken.BOOLEAN -> _in.nextBoolean()
                    JsonToken.NUMBER -> _in.nextInt() != 0
                    JsonToken.STRING -> java.lang.Boolean.parseBoolean(_in.nextString())
                    else -> throw IllegalStateException("Expected BOOLEAN or NUMBER but was $peek")
                }
            }
        }

        objectMapper = GsonBuilder()
            .registerTypeAdapter(Boolean::class.javaObjectType, booleanAsIntAdapter)
            .registerTypeAdapter(Boolean::class.javaPrimitiveType, booleanAsIntAdapter)
            .registerTypeAdapter(Boolean::class.java, booleanAsIntAdapter)
            .registerTypeAdapter(JSONObject::class.java, JSONObjectAdapter())
            .registerTypeAdapter(JSONArray::class.java, JSONArrayAdapter())
            .disableHtmlEscaping()
            .create()
        converterFactory = GsonConverterFactory.create(objectMapper)
    }

    @Throws(PubNubException::class)
    fun toJsonUsingJackson(input: Any): String {
        return try {
            this.jacksonObjectMapper.writeValueAsString(input)
        } catch (e: JsonProcessingException) {
            throw PubNubException(PubNubError.JSON_ERROR).copy(
                errorMessage = e.message ?: PubNubError.JSON_ERROR.message
            )
        }
    }

    fun hasField(element: JsonElement, field: String) = element.asJsonObject.has(field)

    fun getField(element: JsonElement?, field: String): JsonElement? {
        if (element?.isJsonObject!!) {
            return element.asJsonObject.get(field)
        }
        return null
    }

    fun getArrayIterator(element: JsonElement?) = element?.asJsonArray?.iterator()

    fun getArrayIterator(element: JsonElement, field: String) = element.asJsonObject.get(field).asJsonArray.iterator()

    fun getObjectIterator(element: JsonElement) = element.asJsonObject.entrySet().iterator()

    fun getObjectIterator(element: JsonElement, field: String) =
        element.asJsonObject.get(field).asJsonObject.entrySet().iterator()

    fun elementToString(element: JsonElement?) = element?.asString

    fun elementToString(element: JsonElement?, field: String) = element?.asJsonObject?.get(field)?.asString

    fun elementToInt(element: JsonElement, field: String) = element.asJsonObject.get(field).asInt

    fun isJsonObject(element: JsonElement) = element.isJsonObject

    fun getAsObject(element: JsonElement) = element.asJsonObject

    fun getAsBoolean(element: JsonElement, field: String) = element.asJsonObject.get(field)?.asBoolean
        .run { this != null }

    fun putOnObject(element: JsonObject, key: String, value: JsonElement) = element.add(key, value)

    fun getArrayElement(element: JsonElement, index: Int) = element.asJsonArray.get(index)

    fun elementToLong(element: JsonElement) = element.asLong

    fun elementToLong(element: JsonElement, field: String) = element.asJsonObject.get(field).asLong

    fun getAsArray(element: JsonElement) = element.asJsonArray

    fun toJsonTree(any: Any?) = objectMapper.toJsonTree(any)

    fun <T> fromJson(input: String?, clazz: Class<T>): T {
        return try {
            this.objectMapper.fromJson<T>(input, clazz)
        } catch (e: JsonParseException) {
            throw PubNubException(
                pubnubError = PubNubError.PARSING_ERROR,
                errorMessage = e.message
            )
        }
    }

    fun <T> fromJson(input: String?, typeOfT: Type): T {
        return try {
            this.objectMapper.fromJson<T>(input, typeOfT)
        } catch (e: JsonParseException) {
            throw PubNubException(
                pubnubError = PubNubError.PARSING_ERROR,
                errorMessage = e.message
            )
        }
    }

    fun <T> convertValue(input: JsonElement?, clazz: Class<T>): T {
        return this.objectMapper.fromJson(input, clazz) as T
    }

    fun <T> convertValue(o: Any?, clazz: Class<T>?): T {
        return this.objectMapper.fromJson(toJson(o), clazz) as T
    }

    fun toJson(input: Any?): String {
        return try {
            this.objectMapper.toJson(input)
        } catch (e: JsonParseException) {
            throw PubNubException(
                pubnubError = PubNubError.JSON_ERROR,
                errorMessage = e.message
            )
        }
    }

    private class JSONObjectAdapter : JsonSerializer<JSONObject?>, JsonDeserializer<JSONObject?> {
        override fun serialize(
            src: JSONObject?,
            typeOfSrc: Type?,
            context: JsonSerializationContext
        ): JsonElement? {
            if (src == null) {
                return null
            }
            val jsonObject = JsonObject()
            val keys: Iterator<String> = src.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val value: Any = src.opt(key)
                val jsonElement = context.serialize(value, value.javaClass)
                jsonObject.add(key, jsonElement)
            }
            return jsonObject
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): JSONObject? {
            return if (json == null) {
                null
            } else try {
                JSONObject(json.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
                throw JsonParseException(e)
            }
        }
    }

    private class JSONArrayAdapter : JsonSerializer<JSONArray?>, JsonDeserializer<JSONArray?> {
        override fun serialize(
            src: JSONArray?,
            typeOfSrc: Type?,
            context: JsonSerializationContext
        ): JsonElement? {
            if (src == null) {
                return null
            }
            val jsonArray = JsonArray()
            for (i in 0 until src.length()) {
                val `object`: Any = src.opt(i)
                val jsonElement = context.serialize(`object`, `object`.javaClass)
                jsonArray.add(jsonElement)
            }
            return jsonArray
        }

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): JSONArray? {
            return if (json == null) {
                null
            } else try {
                JSONArray(json.toString())
            } catch (e: JSONException) {
                e.printStackTrace()
                throw JsonParseException(e)
            }
        }
    }
}
