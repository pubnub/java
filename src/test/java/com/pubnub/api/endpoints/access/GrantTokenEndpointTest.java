package com.pubnub.api.endpoints.access;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.SpaceId;
import com.pubnub.api.UserId;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.models.consumer.access_manager.sum.SpacePermissions;
import com.pubnub.api.models.consumer.access_manager.sum.UserPermissions;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

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
            pubnub.grantToken(1)
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERR_RESOURCES_MISSING, e.getPubnubError().getErrorCode());
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    public void validate_NoTTLSet() {
        try {
            pubnub.grantToken()
                    .spacesPermissions(Arrays.asList(SpacePermissions.id(new SpaceId("mySpaceId")).delete()))
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERR_TTL_MISSING, e.getPubnubError().getErrorCode());
        }
    }

    @Test
    public void validate_SecretKeyMissing() {
        try {
            createPubNubInstance().grantToken(1)
                    .spacesPermissions(Arrays.asList(SpacePermissions.id(new SpaceId("mySpaceId")).delete()))
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERR_SECRET_KEY_MISSING, e.getPubnubError().getErrorCode());
        }
    }

    @Test
    public void validate_SubscribeKeyMissing() {
        try {
            new PubNub(new PNConfiguration(new UserId("pn-" + UUID.randomUUID())).setSecretKey("secret")).grantToken(1)
                    .usersPermissions(Arrays.asList(UserPermissions.id(new UserId("myUserId")).get()))
                    .sync();
        } catch (PubNubException e) {
            assertEquals(PNERR_SUBSCRIBE_KEY_MISSING, e.getPubnubError().getErrorCode());
        }
    }
}
