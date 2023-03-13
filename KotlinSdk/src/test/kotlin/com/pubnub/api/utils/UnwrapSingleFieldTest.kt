package com.pubnub.apikt.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.JsonAdapter
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

internal class UnwrapSingleFieldTest {
    val gson = GsonBuilder().create()

    @Test
    fun unwrapString() {
        val expectedValue = "The things you own end up owning you."
        val jsonString = """{"f": {"doesn't matter": "$expectedValue"}}"""
        val result = gson.fromJson(jsonString, ClassWithString::class.java)
        assertEquals(ClassWithString(expectedValue), result)
    }

    @Test
    fun unwrapInteger() {
        val expectedValue = 42
        val jsonString = """{"f": {"doesn't matter": $expectedValue}}"""
        val result = gson.fromJson(jsonString, ClassWithNumber::class.java)
        assertEquals(ClassWithNumber(expectedValue), result)
    }

    @Test
    fun unwrapBoolean() {
        val expectedValue = false
        val jsonString = """{"f": {"doesn't matter": $expectedValue}}"""
        val result = gson.fromJson(jsonString, ClassWithBoolean::class.java)
        assertEquals(ClassWithBoolean(expectedValue), result)
    }

    @Test
    fun unwrappingFailsWhenMoreFieldsArePresent() {
        val jsonString = """{"f": {"doesn't matter": "whatIPutHere", "oneTooMany": 17}}"""
        try {
            gson.fromJson(jsonString, ClassWithString::class.java)
        } catch (e: Exception) {
            Assert.assertTrue(e is JsonSyntaxException)
        }
    }
}

data class ClassWithString(@JsonAdapter(UnwrapSingleField::class) val f: String)

data class ClassWithNumber(@JsonAdapter(UnwrapSingleField::class) val f: Int)

data class ClassWithBoolean(@JsonAdapter(UnwrapSingleField::class) val f: Boolean)
