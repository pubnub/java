package com.pubnub.contract.step

import com.pubnub.contract.state.World
import io.cucumber.java.en.Then
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers

class ErrorMessageAndDetailsStep(private val world: World) {
    @Then("I see the error message {string} and details {string}")
    fun i_see_the_error_message_and_details(message: String, details: String) {
        val exception = world.pnException!!
        assertThat(exception.errormsg, Matchers.containsString(message))
        assertThat(exception.errormsg, Matchers.containsString(details))
    }

}
