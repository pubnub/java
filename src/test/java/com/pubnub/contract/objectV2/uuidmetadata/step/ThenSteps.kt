package com.pubnub.contract.objectV2.uuidmetadata.step

import com.pubnub.contract.objectV2.uuidmetadata.state.GetAllUUIDMetadataState
import com.pubnub.contract.objectV2.uuidmetadata.state.GetUUIDMetadataState
import com.pubnub.contract.objectV2.uuidmetadata.state.SetUUIDMetadataState
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Assert.assertEquals

class ThenSteps(
    private val getUUIDMetadataState: GetUUIDMetadataState,
    private val setUUIDMetadataState: SetUUIDMetadataState,
    private val getAllUUIDMetadataState: GetAllUUIDMetadataState
) {

    @Then("the UUID metadata for {string} persona")
    fun the_UUID_metadata_for_persona(personaName: String) {
        val expectedPNUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        val actualPNUUIDMetadata = getUUIDMetadataState.result!!.data

        assertEquals(expectedPNUUIDMetadata, actualPNUUIDMetadata)
    }

    @Then("the UUID metadata for {string} persona contains updated")
    fun the_UUID_metadata_for_persona_contains_updated(personaName: String) {
        val expectedPNUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        val actualPNUUIDMetadata = setUUIDMetadataState.result!!.data

        assertEquals(expectedPNUUIDMetadata, actualPNUUIDMetadata)
    }

    @Then("the response contains list with {string} and {string} UUID metadata")
    fun the_UUID_metadata_for_first_and_second_persona(persona01Name: String, persona02Name: String) {
        val expectedPNUUIDMetadataForFirstPersona = loadPersonaUUIDMetadata(persona01Name)
        val expectedPNUUIDMetadataForSecondPersona = loadPersonaUUIDMetadata(persona02Name)

        assertThat(
            getAllUUIDMetadataState.result!!.data,
            Matchers.containsInAnyOrder(expectedPNUUIDMetadataForFirstPersona, expectedPNUUIDMetadataForSecondPersona)
        )
    }
}
