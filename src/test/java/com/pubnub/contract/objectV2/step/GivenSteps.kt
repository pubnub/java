package com.pubnub.contract.objectV2.step

import com.pubnub.contract.CONTRACT_TEST_CONFIG
import com.pubnub.contract.objectV2.state.GetUUIDMetadataState
import com.pubnub.contract.objectV2.state.RemoveUUIDMetadataState
import com.pubnub.contract.objectV2.state.SetUUIDMetadataState
import com.pubnub.contract.state.World
import io.cucumber.java.en.Given
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

class GivenSteps(
    private val getUUIDMetadataState: GetUUIDMetadataState,
    private val setUUIDMetadataState: SetUUIDMetadataState,
    private val removeUUIDMetadataState: RemoveUUIDMetadataState,
    private val world: World
) {

    @Given("I have a keyset with Objects V2 enabled")
    fun i_have_a_keyset_with_access_manager_enabled() {
        MatcherAssert.assertThat(CONTRACT_TEST_CONFIG.pubKey(), Matchers.notNullValue())
        MatcherAssert.assertThat(CONTRACT_TEST_CONFIG.subKey(), Matchers.notNullValue())
        world.configuration.apply {
            subscribeKey = CONTRACT_TEST_CONFIG.subKey()
            publishKey = CONTRACT_TEST_CONFIG.pubKey()
        }
    }

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
        val id = pnUUIDMetadata.id
        setUUIDMetadataState.id = id
        setUUIDMetadataState.pnUUIDMetadata.name = pnUUIDMetadata.name
        setUUIDMetadataState.pnUUIDMetadata.email = pnUUIDMetadata.email
        setUUIDMetadataState.pnUUIDMetadata.externalId = pnUUIDMetadata.externalId
        setUUIDMetadataState.pnUUIDMetadata.profileUrl = pnUUIDMetadata.profileUrl
        setUUIDMetadataState.pnUUIDMetadata.custom = pnUUIDMetadata.custom
    }
}
