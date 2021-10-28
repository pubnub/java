package com.pubnub.api.managers.token_manager;

import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.access_manager.v3.PNToken;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class TokenParserTest {

    @Test
    public void parseTokenWithMeta() throws PubNubException {
        String tokenWithMeta = "qEF2AkF0GmFLd-NDdHRsGQWgQ3Jlc6VEY2hhbqFjY2gxGP9DZ3JwoWNjZzEY_0N1c3KgQ3NwY6BEdXVpZKFldXVpZDEY_0NwYXSlRGNoYW6gQ2dycKBDdXNyoENzcGOgRHV1aWShYl4kAURtZXRho2VzY29yZRhkZWNvbG9yY3JlZGZhdXRob3JlcGFuZHVEdXVpZGtteWF1dGh1dWlkMUNzaWdYIP2vlxHik0EPZwtgYxAW3-LsBaX_WgWdYvtAXpYbKll3";
        PNToken parsed = new TokenParser().unwrapToken(tokenWithMeta);
        Assert.assertNotNull(parsed.getMeta());
        assertFalse(((Map<?, ?>) parsed.getMeta()).isEmpty());
    }
}
