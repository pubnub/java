package com.pubnub.contract.objectV2.step

import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata
import com.pubnub.contract.objectV2.state.GetAllUUIDMetadataState
import com.pubnub.contract.objectV2.state.GetUUIDMetadataState
import com.pubnub.contract.objectV2.state.SetUUIDMetadataState
import com.pubnub.contract.state.World
import io.cucumber.java.en.Then
import org.junit.Assert.assertEquals

class ThenSteps(
    private val getUUIDMetadataState: GetUUIDMetadataState,
    private val setUUIDMetadataState: SetUUIDMetadataState,
    private val getAllUUIDMetadataState: GetAllUUIDMetadataState,
    private val world: World
) {

    @Then("I receive a successful response")
    fun I_receive_a_successful_response() {
        val status = world.responseStatus
        assertEquals(status, 200)
    }

    @Then("the UUID metadata for {string} persona")
    fun the_UUID_metadata_for_persona(personaName: String) {
        val expectedPNUUIDMetadata: PNUUIDMetadata = loadPersona(personaName)
        val actualPNUUIDMetadata = getUUIDMetadataState.result!!.data

        assertEquals(expectedPNUUIDMetadata.id, actualPNUUIDMetadata.id)
        assertEquals(expectedPNUUIDMetadata.name, actualPNUUIDMetadata.name)
        assertEquals(expectedPNUUIDMetadata.email, actualPNUUIDMetadata.email)
        assertEquals(expectedPNUUIDMetadata.externalId, actualPNUUIDMetadata.externalId)
        assertEquals(expectedPNUUIDMetadata.profileUrl, actualPNUUIDMetadata.profileUrl)
        assertEquals(expectedPNUUIDMetadata.custom, actualPNUUIDMetadata.custom)
        assertEquals(expectedPNUUIDMetadata.updated, actualPNUUIDMetadata.updated)
        assertEquals(expectedPNUUIDMetadata.eTag, actualPNUUIDMetadata.eTag)
    }

    @Then("the UUID metadata for {string} persona contains updated")
    fun the_UUID_metadata_for_persona_contains_updated(personaName: String) {
        val expectedPNUUIDMetadata: PNUUIDMetadata = loadPersona(personaName)

        val actualPNUUIDMetadata = setUUIDMetadataState.result!!.data

        assertEquals(expectedPNUUIDMetadata.id, actualPNUUIDMetadata.id)
        assertEquals(expectedPNUUIDMetadata.name, actualPNUUIDMetadata.name)
        assertEquals(expectedPNUUIDMetadata.email, actualPNUUIDMetadata.email)
        assertEquals(expectedPNUUIDMetadata.externalId, actualPNUUIDMetadata.externalId)
        assertEquals(expectedPNUUIDMetadata.profileUrl, actualPNUUIDMetadata.profileUrl)
        assertEquals(expectedPNUUIDMetadata.custom, actualPNUUIDMetadata.custom)
        assertEquals(expectedPNUUIDMetadata.updated, actualPNUUIDMetadata.updated)
        assertEquals(expectedPNUUIDMetadata.eTag, actualPNUUIDMetadata.eTag)
    }

    @Then("the UUID metadata for {string} and {string} persona")
    fun the_UUID_metadata_for_first_and_second_persona(persona01Name: String, persona02Name: String) {
        val expectedPNUUIDMetadataForAlice: PNUUIDMetadata = loadPersona(persona01Name)
        val expectedPNUUIDMetadataForJames: PNUUIDMetadata = loadPersona(persona02Name)

        val actualPNUUIDMetadataForFirstPersona = getAllUUIDMetadataState.result!!.data[0]
        val actualPNUUIDMetadataForSecondPersona = getAllUUIDMetadataState.result!!.data[1]

        assertEquals(expectedPNUUIDMetadataForAlice.id, actualPNUUIDMetadataForFirstPersona.id)
        assertEquals(expectedPNUUIDMetadataForAlice.name, actualPNUUIDMetadataForFirstPersona.name)
        assertEquals(expectedPNUUIDMetadataForAlice.email, actualPNUUIDMetadataForFirstPersona.email)
        assertEquals(expectedPNUUIDMetadataForAlice.externalId, actualPNUUIDMetadataForFirstPersona.externalId)
        assertEquals(expectedPNUUIDMetadataForAlice.profileUrl, actualPNUUIDMetadataForFirstPersona.profileUrl)
        assertEquals(expectedPNUUIDMetadataForAlice.custom, actualPNUUIDMetadataForFirstPersona.custom)
        assertEquals(expectedPNUUIDMetadataForAlice.updated, actualPNUUIDMetadataForFirstPersona.updated)
        assertEquals(expectedPNUUIDMetadataForAlice.eTag, actualPNUUIDMetadataForFirstPersona.eTag)

        assertEquals(expectedPNUUIDMetadataForJames.id, actualPNUUIDMetadataForSecondPersona.id)
        assertEquals(expectedPNUUIDMetadataForJames.name, actualPNUUIDMetadataForSecondPersona.name)
        assertEquals(expectedPNUUIDMetadataForJames.email, actualPNUUIDMetadataForSecondPersona.email)
        assertEquals(expectedPNUUIDMetadataForJames.externalId, actualPNUUIDMetadataForSecondPersona.externalId)
        assertEquals(expectedPNUUIDMetadataForJames.profileUrl, actualPNUUIDMetadataForSecondPersona.profileUrl)
        assertEquals(expectedPNUUIDMetadataForJames.custom, actualPNUUIDMetadataForSecondPersona.custom)
        assertEquals(expectedPNUUIDMetadataForJames.updated, actualPNUUIDMetadataForSecondPersona.updated)
        assertEquals(expectedPNUUIDMetadataForJames.eTag, actualPNUUIDMetadataForSecondPersona.eTag)
    }
}
