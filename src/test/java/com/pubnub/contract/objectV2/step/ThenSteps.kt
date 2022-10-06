package com.pubnub.contract.objectV2.step

import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata
import com.pubnub.contract.objectV2.state.GetAllUUIDMetadataState
import com.pubnub.contract.objectV2.state.GetUUIDMetadataState
import com.pubnub.contract.objectV2.state.SetUUIDMetadataState
import com.pubnub.contract.state.World
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
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
        val expectedPNUUIDMetadata: PNUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        val actualPNUUIDMetadata = getUUIDMetadataState.result!!.data

        assertEquals(expectedPNUUIDMetadata, actualPNUUIDMetadata)
    }

    @Then("the UUID metadata for {string} persona contains updated")
    fun the_UUID_metadata_for_persona_contains_updated(personaName: String) {
        val expectedPNUUIDMetadata: PNUUIDMetadata = loadPersonaUUIDMetadata(personaName)

        val actualPNUUIDMetadata = setUUIDMetadataState.result!!.data

        assertEquals(expectedPNUUIDMetadata, actualPNUUIDMetadata)
    }

    @Then("the UUID metadata for {string} and {string} persona")
    fun the_UUID_metadata_for_first_and_second_persona(persona01Name: String, persona02Name: String) {
        val expectedPNUUIDMetadataForFirstPersona: PNUUIDMetadata = loadPersonaUUIDMetadata(persona01Name)
        val expectedPNUUIDMetadataForSecondPersona: PNUUIDMetadata = loadPersonaUUIDMetadata(persona02Name)

        assertThat(getAllUUIDMetadataState.result!!.data, Matchers.containsInAnyOrder(expectedPNUUIDMetadataForFirstPersona, expectedPNUUIDMetadataForSecondPersona))
    }
}
