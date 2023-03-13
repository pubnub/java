package com.pubnub.contract.uuidmetadata.step

import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata
import com.pubnub.contract.loadPersonaUUIDMetadata
import com.pubnub.contract.uuidmetadata.state.UUIDMetadataState
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.core.Is.`is` as iz

class ThenSteps(
    private val uuidMetadataState: UUIDMetadataState
) {

    @Then("the UUID metadata for {string} persona")
    fun the_uuid_metadata_for_persona(personaName: String) {
        val alice: PNUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        assertThat(uuidMetadataState.uuidMetadata, iz(alice))
    }

    @Then("the UUID metadata for {string} persona contains updated")
    fun the_uuid_metadata_for_persona_contains_updated(@Suppress("UNUSED_PARAMETER") personaName: String) {
        assertThat(uuidMetadataState.uuidMetadata?.updated, iz(Matchers.notNullValue()))
    }

    @Then("the response contains list with {string} and {string} UUID metadata")
    fun the_response_contains_list_with_UUID_metadata(personName: String, otherPersonName: String) {
        val firstPersona = loadPersonaUUIDMetadata(personName)
        val secondPersona = loadPersonaUUIDMetadata(otherPersonName)
        assertThat(uuidMetadataState.uuidMetadatas, Matchers.containsInAnyOrder(firstPersona, secondPersona))
    }
}
