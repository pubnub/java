package com.pubnub.apikt

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test

class PubNubUtilTest {
    @Test
    fun signedSHA256ReturnPrecomputedSingleLine() {
        assertThat(PubNubUtil.signSHA256("key", "data"), Matchers.`is`("UDH-PZicbRU3oBP6bnOdojRj_a7DtwE32Cjjas4iG9A="))
    }
}
