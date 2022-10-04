package com.pubnub.contract.objectV2.step

import com.google.gson.Gson
import com.pubnub.api.models.consumer.objects_api.uuid.PNUUIDMetadata
import com.pubnub.contract.CONTRACT_TEST_CONFIG
import com.pubnub.contract.objectV2.state.GetAllUUIDMetadataState
import com.pubnub.contract.objectV2.state.GetUUIDMetadataState
import com.pubnub.contract.objectV2.state.SetUUIDMetadataState
import com.pubnub.contract.state.World
import io.cucumber.java.en.Then
import org.junit.Assert.assertEquals
import java.io.File

class ThenSteps(
    private val getUUIDMetadataState: GetUUIDMetadataState,
    private val setUUIDMetadataState: SetUUIDMetadataState,
    private val getAllUUIDMetadataState: GetAllUUIDMetadataState,
    private val world: World
) {

    @Then("I receive a successful response")  //to trzeba chyba zrobić per operacja tzn. osobno dla getUUID, setUUID, removeUUID. Chyba, żeby zrobić generyczne uuidMetadataOperationState
    fun I_receive_a_successful_response() {
        val status = world.responseStatus
        assertEquals(status, 200)
    }

    @Then("the UUID metadata for 'Alice' persona")
    fun the_UUID_metadata_for_Alice_persona() {
        //read alice.json" file
        val personasLocation = CONTRACT_TEST_CONFIG.personasLocation()
        val alicePersonaAsString = File("$personasLocation/alice.json").readText(Charsets.UTF_8)
        //convert json to object(PNGetUUIDMetadataResult)
        val expectedPNUUIDMetadata: PNUUIDMetadata =
            Gson().fromJson(alicePersonaAsString, PNUUIDMetadata::class.java)
        //make assertion
        // e.g.  assert( id(fromJson) == id(from mockServer response)       )
        // e.g.  assert( name(fromJson) == name(from mockServer response)   )
        // e.g.  assert( email(fromJson) == email(from mockServer response) )

        val actualPNUUIDMetadata = getUUIDMetadataState.result!!.data

        assertEquals(expectedPNUUIDMetadata.id, actualPNUUIDMetadata.id)
        assertEquals(expectedPNUUIDMetadata.name, actualPNUUIDMetadata.name)
        assertEquals(expectedPNUUIDMetadata.email, actualPNUUIDMetadata.email)
        assertEquals(expectedPNUUIDMetadata.externalId, actualPNUUIDMetadata.externalId)
        assertEquals(expectedPNUUIDMetadata.profileUrl, actualPNUUIDMetadata.profileUrl)
        assertEquals(expectedPNUUIDMetadata.updated, actualPNUUIDMetadata.updated)
        assertEquals(expectedPNUUIDMetadata.eTag, actualPNUUIDMetadata.eTag)
    }

    @Then("the UUID metadata for 'Bob' persona")
    fun the_UUID_metadata_for_Bob_persona() {
        //read bow.json file
        val personasLocation = CONTRACT_TEST_CONFIG.personasLocation()
        val bobPersonaAsString = File("$personasLocation/bob.json").readText(Charsets.UTF_8)
        //convert json to object(PNGetUUIDMetadataResult)
        val expectedPNUUIDMetadata: PNUUIDMetadata = Gson().fromJson(bobPersonaAsString, PNUUIDMetadata::class.java)
        //make assertion
        // e.g.  assert( id(fromJson) == id(from mockServer response)       )
        // e.g.  assert( name(fromJson) == name(from mockServer response)   )
        // e.g.  assert( email(fromJson) == email(from mockServer response) )

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

    @Then("the UUID metadata for 'Alice' persona contains updated")
    fun the_UUID_metadata_for_Alice_persona_contains_updated() {
        //read bow.json file
        val personasLocation = CONTRACT_TEST_CONFIG.personasLocation()
        val bobPersonaAsString = File("$personasLocation/alice.json").readText(Charsets.UTF_8)
        //convert json to object(PNGetUUIDMetadataResult)
        val expectedPNUUIDMetadata: PNUUIDMetadata = Gson().fromJson(bobPersonaAsString, PNUUIDMetadata::class.java)

        val actualPNUUIDMetadata = setUUIDMetadataState.result!!.data

        assertEquals(expectedPNUUIDMetadata.id, actualPNUUIDMetadata.id)
        assertEquals(expectedPNUUIDMetadata.name, actualPNUUIDMetadata.name)
        assertEquals(expectedPNUUIDMetadata.email, actualPNUUIDMetadata.email)
        assertEquals(expectedPNUUIDMetadata.externalId, actualPNUUIDMetadata.externalId)
        assertEquals(expectedPNUUIDMetadata.profileUrl, actualPNUUIDMetadata.profileUrl)
        assertEquals(null, actualPNUUIDMetadata.custom)
        assertEquals(expectedPNUUIDMetadata.updated, actualPNUUIDMetadata.updated)
        assertEquals(expectedPNUUIDMetadata.eTag, actualPNUUIDMetadata.eTag)
    }

    @Then("the UUID metadata for 'Alice' and 'James' persona")
    fun the_UUID_metadata_for_Alice_and_James_persona() {
        val personasLocation = CONTRACT_TEST_CONFIG.personasLocation()
        val alicePersonaAsString = File("$personasLocation/alice.json").readText(Charsets.UTF_8)
        val jamesPersonaAsString = File("$personasLocation/james.json").readText(Charsets.UTF_8)

        val expectedPNUUIDMetadataForAlice: PNUUIDMetadata = Gson().fromJson(alicePersonaAsString, PNUUIDMetadata::class.java)
        val expectedPNUUIDMetadataForJames: PNUUIDMetadata = Gson().fromJson(jamesPersonaAsString, PNUUIDMetadata::class.java)

        val actualPNUUIDMetadataForAlice = getAllUUIDMetadataState.result!!.data[0]
        val actualPNUUIDMetadataForJames = getAllUUIDMetadataState.result!!.data[1]

        assertEquals(expectedPNUUIDMetadataForAlice.id, actualPNUUIDMetadataForAlice.id)
        assertEquals(expectedPNUUIDMetadataForAlice.name, actualPNUUIDMetadataForAlice.name)
        assertEquals(expectedPNUUIDMetadataForAlice.email, actualPNUUIDMetadataForAlice.email)
        assertEquals(expectedPNUUIDMetadataForAlice.externalId, actualPNUUIDMetadataForAlice.externalId)
        assertEquals(expectedPNUUIDMetadataForAlice.profileUrl, actualPNUUIDMetadataForAlice.profileUrl)
        assertEquals(expectedPNUUIDMetadataForAlice.custom, actualPNUUIDMetadataForAlice.custom)
        assertEquals(expectedPNUUIDMetadataForAlice.updated, actualPNUUIDMetadataForAlice.updated)
        assertEquals(expectedPNUUIDMetadataForAlice.eTag, actualPNUUIDMetadataForAlice.eTag)

        assertEquals(expectedPNUUIDMetadataForJames.id, actualPNUUIDMetadataForJames.id)
        assertEquals(expectedPNUUIDMetadataForJames.name, actualPNUUIDMetadataForJames.name)
        assertEquals(expectedPNUUIDMetadataForJames.email, actualPNUUIDMetadataForJames.email)
        assertEquals(expectedPNUUIDMetadataForJames.externalId, actualPNUUIDMetadataForJames.externalId)
        assertEquals(expectedPNUUIDMetadataForJames.profileUrl, actualPNUUIDMetadataForJames.profileUrl)
        assertEquals(expectedPNUUIDMetadataForJames.custom, actualPNUUIDMetadataForJames.custom)
        assertEquals(expectedPNUUIDMetadataForJames.updated, actualPNUUIDMetadataForJames.updated)
        assertEquals(expectedPNUUIDMetadataForJames.eTag, actualPNUUIDMetadataForJames.eTag)
    }

    @Then("the UUID metadata for 'Bob' and 'Lisa' persona")
    fun the_UUID_metadata_for_Bob_and_Lisa_persona(){
        val personasLocation = CONTRACT_TEST_CONFIG.personasLocation()
        val bobPersonaAsString = File("$personasLocation/bob.json").readText(Charsets.UTF_8)
        val lisaPersonaAsString = File("$personasLocation/lisa.json").readText(Charsets.UTF_8)

        val expectedPNUUIDMetadataForBob: PNUUIDMetadata = Gson().fromJson(bobPersonaAsString, PNUUIDMetadata::class.java)
        val expectedPNUUIDMetadataForLisa: PNUUIDMetadata = Gson().fromJson(lisaPersonaAsString, PNUUIDMetadata::class.java)

        val actualPNUUIDMetadataForBob = getAllUUIDMetadataState.result!!.data[0]
        val actualPNUUIDMetadataForLisa = getAllUUIDMetadataState.result!!.data[1]

        assertEquals(expectedPNUUIDMetadataForBob.id, actualPNUUIDMetadataForBob.id)
        assertEquals(expectedPNUUIDMetadataForBob.name, actualPNUUIDMetadataForBob.name)
        assertEquals(expectedPNUUIDMetadataForBob.email, actualPNUUIDMetadataForBob.email)
        assertEquals(expectedPNUUIDMetadataForBob.externalId, actualPNUUIDMetadataForBob.externalId)
        assertEquals(expectedPNUUIDMetadataForBob.profileUrl, actualPNUUIDMetadataForBob.profileUrl)
        assertEquals(expectedPNUUIDMetadataForBob.custom, actualPNUUIDMetadataForBob.custom)
        assertEquals(expectedPNUUIDMetadataForBob.updated, actualPNUUIDMetadataForBob.updated)
        assertEquals(expectedPNUUIDMetadataForBob.eTag, actualPNUUIDMetadataForBob.eTag)

        assertEquals(expectedPNUUIDMetadataForLisa.id, actualPNUUIDMetadataForLisa.id)
        assertEquals(expectedPNUUIDMetadataForLisa.name, actualPNUUIDMetadataForLisa.name)
        assertEquals(expectedPNUUIDMetadataForLisa.email, actualPNUUIDMetadataForLisa.email)
        assertEquals(expectedPNUUIDMetadataForLisa.externalId, actualPNUUIDMetadataForLisa.externalId)
        assertEquals(expectedPNUUIDMetadataForLisa.profileUrl, actualPNUUIDMetadataForLisa.profileUrl)
        assertEquals(expectedPNUUIDMetadataForLisa.custom, actualPNUUIDMetadataForLisa.custom)
        assertEquals(expectedPNUUIDMetadataForLisa.updated, actualPNUUIDMetadataForLisa.updated)
        assertEquals(expectedPNUUIDMetadataForLisa.eTag, actualPNUUIDMetadataForLisa.eTag)
    }
}
