package com.pubnub.contract.publish.steps

import com.pubnub.contract.state.World
import io.cucumber.java.en.Then
import org.junit.Assert



class ThenSteps(
    private val world: World
) {

    val EXPECTED_ERROR_CODE = 400

    @Then("I receive error response")
    fun I_receive_error_response() {
        Assert.assertEquals(EXPECTED_ERROR_CODE, world.pnException?.statusCode)
    }
}
