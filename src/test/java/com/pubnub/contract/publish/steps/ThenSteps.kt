package com.pubnub.contract.publish.steps

import com.pubnub.contract.state.World
import io.cucumber.java.en.Then
import org.junit.Assert

class ThenSteps(
    private val world: World
) {

    @Then("I receive error response")
    fun I_receive_error_response() {
        Assert.assertNotNull(world.pnException)
    }

    @Then("I receive an error response")
    fun I_receive_an_error_response() {
        I_receive_error_response()
    }
}
