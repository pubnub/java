package com.pubnub.contract.objectV2.common

import com.pubnub.contract.state.World
import io.cucumber.java.en.Then
import org.junit.Assert

class ThenSteps(private val world: World) {

    @Then("I receive a successful response")
    fun I_receive_a_successful_response() {
        val status = world.responseStatus
        Assert.assertEquals(status, 200)
    }
}
