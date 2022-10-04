package com.pubnub.contract.objectV2.step

import com.google.gson.Gson
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata
import com.pubnub.contract.CONTRACT_TEST_CONFIG
import com.pubnub.contract.objectV2.state.GetUUIDMetadataState
import com.pubnub.contract.objectV2.state.RemoveUUIDMetadataState
import com.pubnub.contract.objectV2.state.SetUUIDMetadataState
import com.pubnub.contract.state.World
import io.cucumber.java.en.Given
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import java.io.File

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
    fun the_id_for_Alice_persona(personaName: String) {
        val pnUUIDMetadata = loadPersona(personaName)  ///"alice.json"  Alice

        //get id
        val uuidId = pnUUIDMetadata.id
        //set uuidId in state for getMetadataForAnUUID
        getUUIDMetadataState.id = uuidId
        removeUUIDMetadataState.id = uuidId  //czy tu tak?
    }

    @Given("current user is 'Bob' persona")
    fun current_user_is_Bob_persona() {
        //read bob.json file
        val personasLocation = CONTRACT_TEST_CONFIG.personasLocation()
        val bobPersonaAsString = File("$personasLocation/bob.json").readText(Charsets.UTF_8)
        //convert json to object
        val pnUUIDMetadata: PNUUIDMetadata = Gson().fromJson(bobPersonaAsString, PNUUIDMetadata::class.java)
        val id = pnUUIDMetadata.id
        // set object state/configuration
        world.configuration.uuid = id
    }

    @Given("current user is 'Alice' persona")
    fun current_user_is_Alice_persona() {
        //read alice.json file
        val personasLocation = CONTRACT_TEST_CONFIG.personasLocation()
        val bobPersonaAsString = File("$personasLocation/alice.json").readText(Charsets.UTF_8)
        //convert json to object
        val pnUUIDMetadata: PNUUIDMetadata = Gson().fromJson(bobPersonaAsString, PNUUIDMetadata::class.java)
        val id = pnUUIDMetadata.id
        // set object state/configuration
        world.configuration.uuid = id
    }

    @Given("the data for 'Alice' persona")
    fun the_data_for_Alice_persona() {
        //read alice.json file
        val personasLocation = CONTRACT_TEST_CONFIG.personasLocation()
        val bobPersonaAsString = File("$personasLocation/alice.json").readText(Charsets.UTF_8)
        //convert json to object
        val pnUUIDMetadata: PNUUIDMetadata = Gson().fromJson(bobPersonaAsString, PNUUIDMetadata::class.java)
        val id = pnUUIDMetadata.id
        // set data in state object
        setUUIDMetadataState.id = id
        setUUIDMetadataState.pnUUIDMetadata.name = pnUUIDMetadata.name
        setUUIDMetadataState.pnUUIDMetadata.email = pnUUIDMetadata.email
        setUUIDMetadataState.pnUUIDMetadata.externalId = pnUUIDMetadata.externalId
        setUUIDMetadataState.pnUUIDMetadata.profileUrl = pnUUIDMetadata.profileUrl
        setUUIDMetadataState.pnUUIDMetadata.custom = pnUUIDMetadata.custom
    }

    private fun loadPersona(personaName: String): PNUUIDMetadata {
        val fileName = personaName.toLowerCase() + ".json"
        val personasLocation = CONTRACT_TEST_CONFIG.personasLocation()
        val personaAsString = File("$personasLocation/$fileName").readText(Charsets.UTF_8)
        return Gson().fromJson(personaAsString, PNUUIDMetadata::class.java)
    }
}
