package com.pubnub.extension

import com.google.gson.JsonElement
import com.pubnub.apikt.PNConfiguration
import com.pubnub.apikt.PNConfiguration.Companion.isValid
import com.pubnub.apikt.managers.MapperManager
import com.pubnub.apikt.vendor.Crypto

internal fun JsonElement.processHistoryMessage(configuration: PNConfiguration, mapper: MapperManager): JsonElement {
    if (!configuration.cipherKey.isValid())
        return this

    val crypto = Crypto(configuration.cipherKey, configuration.useRandomInitializationVector)

    val inputText =
        if (mapper.isJsonObject(this) && mapper.hasField(
                this,
                "pn_other"
            )
        ) {
            mapper.elementToString(this, "pn_other")
        } else {
            mapper.elementToString(this)
        }

    val outputText = crypto.decrypt(inputText!!)

    var outputObject = mapper.fromJson(outputText, JsonElement::class.java)

    mapper.getField(this, "pn_other")?.let {
        val objectNode = mapper.getAsObject(this)
        mapper.putOnObject(objectNode, "pn_other", outputObject)
        outputObject = objectNode
    }

    return outputObject
}
