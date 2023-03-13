package com.pubnub.apikt.managers

import org.junit.Assert
import org.junit.Test

class TokenParserTest {

    @Test
    fun parseTokenWithMeta() {
        val tokenWithMeta = "qEF2AkF0GmFLd-NDdHRsGQWgQ3Jlc6VEY2hhbqFjY2gxGP9DZ3JwoWNjZzEY_0N1c3KgQ3NwY6BEdXVpZKFldXVpZDEY_0NwYXSlRGNoYW6gQ2dycKBDdXNyoENzcGOgRHV1aWShYl4kAURtZXRho2VzY29yZRhkZWNvbG9yY3JlZGZhdXRob3JlcGFuZHVEdXVpZGtteWF1dGh1dWlkMUNzaWdYIP2vlxHik0EPZwtgYxAW3-LsBaX_WgWdYvtAXpYbKll3"
        val parsed = TokenParser().unwrapToken(tokenWithMeta)
        Assert.assertNotNull(parsed.meta)
        Assert.assertTrue(parsed.meta is Map<*, *>)
        Assert.assertTrue((parsed.meta as Map<*, *>).isNotEmpty())
    }
}
