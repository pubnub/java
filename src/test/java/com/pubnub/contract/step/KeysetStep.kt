package com.pubnub.contract.step

import com.pubnub.contract.CONTRACT_TEST_CONFIG
import com.pubnub.contract.ContractTestConfig
import com.pubnub.contract.state.World
import io.cucumber.java.en.Given
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers

class KeysetStep(private val world: World) {

    @Given("I have a keyset with access manager enabled")
    fun i_have_a_keyset_with_access_manager_enabled() {
        MatcherAssert.assertThat(CONTRACT_TEST_CONFIG.pamPubKey(), Matchers.notNullValue())
        MatcherAssert.assertThat(CONTRACT_TEST_CONFIG.pamSubKey(), Matchers.notNullValue())
        MatcherAssert.assertThat(CONTRACT_TEST_CONFIG.pamSecKey(), Matchers.notNullValue())
        world.configuration.apply {
            subscribeKey = CONTRACT_TEST_CONFIG.pamSubKey()
            publishKey = CONTRACT_TEST_CONFIG.pamPubKey()
            secretKey = CONTRACT_TEST_CONFIG.pamSecKey()
        }
    }

    @Given("I have a keyset with access manager enabled - without secret key")
    fun i_have_a_keyset_with_access_manager_enabled_without_secret_key() {
        MatcherAssert.assertThat(CONTRACT_TEST_CONFIG.pubKey(), Matchers.notNullValue())
        MatcherAssert.assertThat(CONTRACT_TEST_CONFIG.subKey(), Matchers.notNullValue())

        world.configuration.apply {
            subscribeKey = CONTRACT_TEST_CONFIG.subKey()
            publishKey = CONTRACT_TEST_CONFIG.pubKey()
        }
    }
}
