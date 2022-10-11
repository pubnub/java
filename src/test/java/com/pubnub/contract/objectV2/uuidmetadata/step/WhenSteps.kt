package com.pubnub.contract.objectV2.uuidmetadata.step

import com.pubnub.contract.objectV2.uuidmetadata.state.GetAllUUIDMetadataState
import com.pubnub.contract.objectV2.uuidmetadata.state.GetUUIDMetadataState
import com.pubnub.contract.objectV2.uuidmetadata.state.RemoveUUIDMetadataState
import com.pubnub.contract.objectV2.uuidmetadata.state.SetUUIDMetadataState
import com.pubnub.contract.state.World
import io.cucumber.java.en.When


class WhenSteps(
    private val getUUIDMetadataState: GetUUIDMetadataState,
    private val setUUIDMetadataState: SetUUIDMetadataState,
    private val removeUUIDMetadataState: RemoveUUIDMetadataState,
    private val getAllUUIDMetadataState: GetAllUUIDMetadataState,
    private val world: World
) {


    @When("I get the UUID metadata")
    fun I_get_the_UUID_metadata() {
        val pnGetUUIDMetadataResult = world.pubnub.getUUIDMetadata()
            .uuid(getUUIDMetadataState.id)
            .sync()
        getUUIDMetadataState.result = pnGetUUIDMetadataResult
        world.responseStatus = pnGetUUIDMetadataResult?.status
    }

    @When("I get the UUID metadata with custom for current user")
    fun I_get_the_UUID_metadata_with_custom_for_current_user() {
        val pnGetUUIDMetadataResult = world.pubnub.getUUIDMetadata()
            .includeCustom(true)
            .sync()
        getUUIDMetadataState.result = pnGetUUIDMetadataResult
        world.responseStatus = pnGetUUIDMetadataResult?.status
    }


    @When("I set the UUID metadata")
    fun I_set_the_UUID_metadata() {
        val pnSetUUIDMetadataResult = world.pubnub.setUUIDMetadata()
            .uuid(setUUIDMetadataState.id)
            .name(setUUIDMetadataState.pnUUIDMetadata.name)
            .email(setUUIDMetadataState.pnUUIDMetadata.email)
            .profileUrl(setUUIDMetadataState.pnUUIDMetadata.profileUrl)
            .externalId(setUUIDMetadataState.pnUUIDMetadata.externalId)
            .sync()
        setUUIDMetadataState.result = pnSetUUIDMetadataResult
        world.responseStatus = pnSetUUIDMetadataResult?.status
    }

    @When("I remove the UUID metadata")
    fun I_remove_the_UUID_metadata() {
        val pnRemoveUUIDMetadataResult = world.pubnub.removeUUIDMetadata()
            .uuid(removeUUIDMetadataState.id)
            .sync()
        removeUUIDMetadataState.result = pnRemoveUUIDMetadataResult
        world.responseStatus = pnRemoveUUIDMetadataResult?.status
    }

    @When("I remove the UUID metadata for current user")
    fun I_remove_the_UUID_metadata_for_current_user() {
        val pnRemoveUUIDMetadataResult = world.pubnub.removeUUIDMetadata().sync()
        removeUUIDMetadataState.result = pnRemoveUUIDMetadataResult
        world.responseStatus = pnRemoveUUIDMetadataResult?.status
    }

    @When("I get all UUID metadata")
    fun I_get_all_UUID_metadata() {
        val pnGetAllUUIDMetadataResult = world.pubnub.getAllUUIDMetadata().sync()
        getAllUUIDMetadataState.result = pnGetAllUUIDMetadataResult
        world.responseStatus = pnGetAllUUIDMetadataResult?.status
    }

    @When("I get all UUID metadata with custom")
    fun I_get_all_UUID_metadata_with_custom() {
        val pnGetAllUUIDMetadataResult = world.pubnub.getAllUUIDMetadata()
            .includeCustom(true)
            .sync()
        getAllUUIDMetadataState.result = pnGetAllUUIDMetadataResult
        world.responseStatus = pnGetAllUUIDMetadataResult?.status
    }
}
