package com.pubnub.apikt.integration.pam

import com.pubnub.apikt.PubNub

class PamServerIntegrationTest : AccessManagerIntegrationTest() {

    override fun getPamLevel() = LEVEL_APP

    override fun performOnServer() = true

    override val pubnubToTest: PubNub = server
}
