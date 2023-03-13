package com.pubnub.apikt.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.JsonAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolymorphicDeserializerTest {
    private val gson = GsonBuilder().create()
    private val expectedFieldValue = "1"

    @Test
    fun deserializationHappyPath() {
        val jsonString = """{"f": "$expectedFieldValue", "type": "1"}"""
        val result = gson.fromJson(jsonString, I::class.java)
        assertEquals(Impl1(expectedFieldValue), result)
    }

    @Test
    fun deserializeWithExtraFieldsInJson() {
        val jsonString = """{"f": "$expectedFieldValue", "type": "2", "extra": 1}"""
        val result = gson.fromJson(jsonString, I::class.java)
        assertEquals(Impl2(expectedFieldValue), result)
    }

    @Test
    fun deserializeToDefaultClass() {
        val gson = GsonBuilder().registerTypeAdapter(I::class.java, DeserializerWithDefaultClass).create()
        val jsonString = """{"f": "$expectedFieldValue", "type": "3"}"""
        val result = gson.fromJson(jsonString, I::class.java)
        assertEquals(DefaultImpl(expectedFieldValue, "3"), result)
    }

    @Test
    fun deserializeUnknownTypeWithoutProvidingDefaultClass() {
        val jsonString = """{"f": "$expectedFieldValue", "type": "3"}"""
        try {
            gson.fromJson(jsonString, I::class.java)
        } catch (e: Exception) {
            assertTrue(e is JsonSyntaxException)
        }
    }
}

object DeserializerWithoutDefaultClass : JsonDeserializer<I> by PolymorphicDeserializer.dispatchByFieldsValues(
    fields = listOf("type"),
    mappingFieldValuesToClass = mapOf(
        listOf("1") to Impl1::class.java,
        listOf("2") to Impl2::class.java
    )
)

object DeserializerWithDefaultClass : JsonDeserializer<I> by PolymorphicDeserializer.dispatchByFieldsValues(
    fields = listOf("type"),
    mappingFieldValuesToClass = mapOf(
        listOf("1") to Impl1::class.java,
        listOf("2") to Impl2::class.java
    ),
    defaultClass = DefaultImpl::class.java
)

@JsonAdapter(DeserializerWithoutDefaultClass::class)
interface I {
    val type: String
    val f: String
}

data class Impl1(override val f: String) : I {
    override val type: String = "1"
}

data class Impl2(override val f: String) : I {
    override val type: String = "2"
}

data class DefaultImpl(override val f: String, override val type: String = "N/A") : I
