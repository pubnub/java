package com.pubnub.contract.objectV2.uuidmetadata.step

import com.pubnub.contract.objectV2.uuidmetadata.state.GetUUIDMetadataState
import com.pubnub.contract.objectV2.uuidmetadata.state.RemoveUUIDMetadataState
import com.pubnub.contract.objectV2.uuidmetadata.state.SetUUIDMetadataState
import com.pubnub.contract.state.World
import io.cucumber.java.en.Given

class GivenSteps(
    private val getUUIDMetadataState: GetUUIDMetadataState,
    private val setUUIDMetadataState: SetUUIDMetadataState,
    private val removeUUIDMetadataState: RemoveUUIDMetadataState,
    private val world: World
) {

    @Given("the id for {string} persona")
    fun the_id_for_persona(personaName: String) {
        val pnUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        val uuidId = pnUUIDMetadata.id
        getUUIDMetadataState.id = uuidId
        removeUUIDMetadataState.id = uuidId
    }

    @Given("current user is {string} persona")
    fun current_user_is_persona(personaName: String) {
        val pnUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        val id = pnUUIDMetadata.id
        world.configuration.uuid = id
    }

    @Given("the data for {string} persona")
    fun the_data_for_persona(personaName: String) {
        val pnUUIDMetadata = loadPersonaUUIDMetadata(personaName)
        setUUIDMetadataState.id = pnUUIDMetadata.id
        setUUIDMetadataState.pnUUIDMetadata.name = pnUUIDMetadata.name
        setUUIDMetadataState.pnUUIDMetadata.email = pnUUIDMetadata.email
        setUUIDMetadataState.pnUUIDMetadata.externalId = pnUUIDMetadata.externalId
        setUUIDMetadataState.pnUUIDMetadata.profileUrl = pnUUIDMetadata.profileUrl
        setUUIDMetadataState.pnUUIDMetadata.custom = pnUUIDMetadata.custom
        setUUIDMetadataState.pnUUIDMetadata.status = pnUUIDMetadata.status
        setUUIDMetadataState.pnUUIDMetadata.type = pnUUIDMetadata.type
    }
}
