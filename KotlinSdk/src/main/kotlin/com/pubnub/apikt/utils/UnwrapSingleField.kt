package com.pubnub.apikt.utils

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

internal class UnwrapSingleField<T> : JsonDeserializer<T> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): T {
        val jsonObject = json.asJsonObject
        if (jsonObject.keySet().size != 1)
            error("Couldn't unwrap field for object containing more than 1 field. Actual number of fields: ${jsonObject.keySet().size}")
        val element = jsonObject[jsonObject.keySet().first()]
        return context.deserialize(element, typeOfT)
    }
}
