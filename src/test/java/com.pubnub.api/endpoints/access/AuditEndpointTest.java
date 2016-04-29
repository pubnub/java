package com.pubnub.api.endpoints.access;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerAuditResult;
import com.pubnub.api.endpoints.TestHarness;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static org.junit.Assert.assertEquals;

public class AuditEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Audit partialAudit;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException {

        pubnub = this.createPubNubInstance(8080);
        partialAudit = pubnub.audit();

        pubnub.getConfiguration().setSecretKey("secretKey");

    }

    @Test
    public void testSuccessChannelGroupSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/audit/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("IjnQ0J7c0SYT3gHBxrIC_8OkDHTqsF9KnI0SlBRLNfg%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"channel-group\":\"cg2\",\"auths\":{\"key1\":{\"r\":1,\"m\":1,\"w\":1}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerAuditResult pnAccessManagerAuditResult = partialAudit.channelGroup("cg1").authKeys(Arrays.asList("key1")).sync();

        Assert.assertEquals("cg2", pnAccessManagerAuditResult.getChannelGroup());
        Assert.assertEquals(true, pnAccessManagerAuditResult.getAuthKeys().get("key1").isManageEnabled());
        Assert.assertEquals(true, pnAccessManagerAuditResult.getAuthKeys().get("key1").isReadEnabled());
        Assert.assertEquals(true, pnAccessManagerAuditResult.getAuthKeys().get("key1").isWriteEnabled());
        Assert.assertEquals("channel-group+auth", pnAccessManagerAuditResult.getLevel());
        Assert.assertEquals("sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f", pnAccessManagerAuditResult.getSubscribeKey());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/audit/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());

    }

    @Test
    public void testSuccessChannelSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/audit/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("ZlPruaId7jzupmK4LUynpnjvA2CQYyrrT0475wWkbwY%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":1,\"m\":1,\"w\":1}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerAuditResult pnAccessManagerAuditResult = partialAudit.channel("ch1").authKeys(Arrays.asList("key1")).sync();

        Assert.assertEquals("ch1", pnAccessManagerAuditResult.getChannel());
        Assert.assertEquals(true, pnAccessManagerAuditResult.getAuthKeys().get("key1").isManageEnabled());
        Assert.assertEquals(true, pnAccessManagerAuditResult.getAuthKeys().get("key1").isReadEnabled());
        Assert.assertEquals(true, pnAccessManagerAuditResult.getAuthKeys().get("key1").isWriteEnabled());
        Assert.assertEquals("user", pnAccessManagerAuditResult.getLevel());
        Assert.assertEquals("sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f", pnAccessManagerAuditResult.getSubscribeKey());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }
}
