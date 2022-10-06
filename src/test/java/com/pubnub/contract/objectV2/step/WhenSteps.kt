package com.pubnub.contract.objectV2.step

import com.pubnub.contract.objectV2.state.GetAllUUIDMetadataState
import com.pubnub.contract.objectV2.state.GetUUIDMetadataState
import com.pubnub.contract.objectV2.state.RemoveUUIDMetadataState
import com.pubnub.contract.objectV2.state.SetUUIDMetadataState
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
        getUUIDMetadataState.result = world.pubnub.getUUIDMetadata()
            .uuid(getUUIDMetadataState.id)
            .sync()
        world.responseStatus = getUUIDMetadataState.result?.status
    }

    @When("I get the UUID metadata with custom for current user")
    fun I_get_the_UUID_metadata_with_custom_for_current_user() {
        getUUIDMetadataState.result = world.pubnub.getUUIDMetadata()
            .includeCustom(true)
            .sync()
        world.responseStatus = getUUIDMetadataState.result?.status
    }


    @When("I set the UUID metadata")
    fun I_set_the_UUID_metadata() {
        setUUIDMetadataState.result = world.pubnub.setUUIDMetadata()
            .uuid(setUUIDMetadataState.id)
            .name(setUUIDMetadataState.pnUUIDMetadata.name)
            .email(setUUIDMetadataState.pnUUIDMetadata.email)
            .profileUrl(setUUIDMetadataState.pnUUIDMetadata.profileUrl)
            .externalId(setUUIDMetadataState.pnUUIDMetadata.externalId)
            .sync()
        world.responseStatus = setUUIDMetadataState.result?.status
    }

    @When("I remove the UUID metadata")
    fun I_remove_the_UUID_metadata() {
        removeUUIDMetadataState.result = world.pubnub.removeUUIDMetadata()
            .uuid(removeUUIDMetadataState.id)
            .sync()
        world.responseStatus = removeUUIDMetadataState.result?.status
    }

    @When("I remove the UUID metadata for current user")
    fun I_remove_the_UUID_metadata_for_current_user() {
        removeUUIDMetadataState.result = world.pubnub.removeUUIDMetadata().sync()
        world.responseStatus = removeUUIDMetadataState.result?.status
    }

    @When("I get all UUID metadata")
    fun I_get_all_UUID_metadata() {
        getAllUUIDMetadataState.result = world.pubnub.getAllUUIDMetadata().sync()
        world.responseStatus = getAllUUIDMetadataState.result?.status
    }

    @When("I get all UUID metadata with custom")
    fun I_get_all_UUID_metadata_with_custom() {
        getAllUUIDMetadataState.result = world.pubnub.getAllUUIDMetadata()
            .includeCustom(true)
            .sync()
        world.responseStatus = getAllUUIDMetadataState.result?.status
    }

}