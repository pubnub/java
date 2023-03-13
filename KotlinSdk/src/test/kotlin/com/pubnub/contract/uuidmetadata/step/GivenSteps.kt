package com.pubnub.contract.uuidmetadata.step

import com.pubnub.apikt.UserId
import com.pubnub.apikt.models.consumer.objects.uuid.PNUUIDMetadata
import com.pubnub.contract.loadPersonaUUIDMetadata
import com.pubnub.contract.state.World
import com.pubnub.contract.uuidmetadata.state.UUIDMetadataState
import io.cucumber.java.en.Given

class GivenSteps(
    private val world: World,
    private val uuidMetadataState: UUIDMetadataState
) {

    @Given("the id for {string} persona")
    fun the_uuid_for_persona(personaName: String) {
        val pnUUIDMetadata: PNUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        uuidMetadataState.uuid = pnUUIDMetadata.id
    }

    @Given("current user is {string} persona")
    fun given_current_user_is_persona(personaName: String) {
        val pnUUIDMetadata: PNUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        world.pubnub.configuration.userId = UserId(pnUUIDMetadata.id)
    }

    @Given("the data for {string} persona")
    fun the_data_for_persona(personaName: String) {
        val pnUUIDMetadata: PNUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        uuidMetadataState.uuidMetadata = pnUUIDMetadata
    }
}
