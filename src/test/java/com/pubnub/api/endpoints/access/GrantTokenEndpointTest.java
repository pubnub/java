package com.pubnub.api.endpoints.access;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGrant;
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGroupGrant;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import static com.pubnub.api.builder.PubNubErrorBuilder.PNERR_RESOURCES_MISSING;
import static com.pubnub.api.builder.PubNubErrorBuilder.PNERR_SECRET_KEY_MISSING;
import static com.pubnub.api.builder.PubNubErrorBuilder.PNERR_SUBSCRIBE_KEY_MISSING;
import static com.pubnub.api.builder.PubNubErrorBuilder.PNERR_TTL_MISSING;
import static org.junit.Assert.assertEquals;

public class GrantTokenEndpointTest extends TestHarness {

    private final PubNub pubnub = this.createPubNubInstance();

    public GrantTokenEndpointTest() throws PubNubException {
    }

    @Before
    public void beforeEach() throws IOException {
        pubnub.getConfiguration().setSecretKey("secretKey").setIncludeInstanceIdentifier(true);
    }

    @Test
    public void validate_NoResourceSet() {
        try {
            pubnub.grantToken()
                    .ttl(1)
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERR_RESOURCES_MISSING, e.getPubnubError().getErrorCode());
        }
    }

    @Test
    public void validate_NoTTLSet() {
        try {
            pubnub.grantToken()
                    .channels(Collections.singletonList(ChannelGrant.name("test").read()))
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERR_TTL_MISSING, e.getPubnubError().getErrorCode());
        }
    }

    @Test
    public void validate_SecretKeyMissing() {
        try {
            createPubNubInstance().grantToken()
                    .ttl(1)
                    .channelGroups(Collections.singletonList(ChannelGroupGrant.id("test").read()))
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERR_SECRET_KEY_MISSING, e.getPubnubError().getErrorCode());
        }
    }

    @Test
    public void validate_SubscribeKeyMissing() {
        try {
            new PubNub(new PNConfiguration(PubNub.generateUUID()).setSecretKey("secret")).grantToken()
                    .ttl(1)
                    .channelGroups(Collections.singletonList(ChannelGroupGrant.id("test").read()))
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERR_SUBSCRIBE_KEY_MISSING, e.getPubnubError().getErrorCode());
        }
    }
}
