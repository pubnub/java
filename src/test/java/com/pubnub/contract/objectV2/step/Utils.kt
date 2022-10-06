package com.pubnub.contract.objectV2.step

import com.google.gson.Gson
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata
import com.pubnub.contract.CONTRACT_TEST_CONFIG
import java.io.File

fun loadPersonaUUIDMetadata(personaName: String): PNUUIDMetadata {
    val fileName = personaName.toLowerCase() + ".json"
    val personasLocation = CONTRACT_TEST_CONFIG.dataFileLocation()
    val personaAsString = File("$personasLocation/$fileName").readText(Charsets.UTF_8)
    return Gson().fromJson(personaAsString, PNUUIDMetadata::class.java)
}
