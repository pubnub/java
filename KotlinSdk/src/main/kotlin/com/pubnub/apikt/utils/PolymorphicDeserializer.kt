package com.pubnub.apikt.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

internal class PolymorphicDeserializer<T>(private val defaultClass: Class<out T>? = null, private val findTargetClass: (JsonElement, Type) -> Class<out T>?) : JsonDeserializer<T> {
    companion object {
        inline fun <reified T> dispatchByFieldsValues(fields: List<String>, mappingFieldValuesToClass: Map<List<String>, Class<out T>>, defaultClass: Class<out T>? = null): JsonDeserializer<T> {
            val mappingWithList: Map<List<String>, Class<out T>> = mappingFieldValuesToClass.mapKeys { it.key.toList() }
            return PolymorphicDeserializer(defaultClass = defaultClass) { jsonElement, _ ->
                val jsonObject = jsonElement.asJsonObject
                val fieldsValues: List<String> = fields.map { jsonObject[it].asString }.toList()

                mappingWithList[fieldsValues]
            }
        }
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): T {
        val targetClass = findTargetClass(json, typeOfT) ?: defaultClass ?: error("When deserializing to $typeOfT no target class have been found and default class was not provided")
        return context.deserialize(json, targetClass)
    }
}
