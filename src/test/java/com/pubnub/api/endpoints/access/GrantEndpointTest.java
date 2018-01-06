package com.pubnub.api.endpoints.access;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.awaitility.Awaitility;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerKeyData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;

public class GrantEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Grant partialGrant;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
        partialGrant = pubnub.grant();
        pubnub.getConfiguration().setSecretKey("secretKey").setIncludeInstanceIdentifier(true);
        wireMockRule.start();
    }

    @Test
    public void noGroupsOneChannelOneKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(0, result.getChannelGroups().size());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("nUZe6LEDe2RGPeh4qoe-IpjAYzBf8RJMtSXI7ktaah8=", signature);

    }

    @Test
    public void noGroupsOneChannelTwoKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channels(Collections.singletonList("ch1")).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(0, result.getChannelGroups().size());

        assertEquals(2, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("ml__GSdczD-5CLqz8Vappaux1bZj6IycOP4O6FyS7ek=", signature);
    }

    @Test
    public void noGroupsTwoChannelOneKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channels(Arrays.asList("ch1", "ch2")).sync();

        assertEquals(2, result.getChannels().size());
        assertEquals(0, result.getChannelGroups().size());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(1, result.getChannels().get("ch2").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("J6g30_aZUtbXOLCQ3kkYx0-K6Rm0F-pFUPa3kZ2647Q=", signature);
    }

    @Test
    public void noGroupsTwoChannelTwoKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channels(Arrays.asList("ch1", "ch2")).sync();

        assertEquals(2, result.getChannels().size());
        assertEquals(0, result.getChannelGroups().size());

        assertEquals(2, result.getChannels().get("ch1").size());
        assertEquals(2, result.getChannels().get("ch2").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key2").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("-3DlrA39lnyhbjTtuJC6s13Gc5Owa_SdRxAx3zJFUd4=", signature);
    }

    @Test
    public void oneGroupNoChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel-groups\":\"cg1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channelGroups(Collections.singletonList("cg1")).sync();

        assertEquals(0, result.getChannels().size());
        assertEquals(1, result.getChannelGroups().size());

        assertEquals(1, result.getChannelGroups().get("cg1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("JfuIHr4BMGVbaAVp1QdDvvhcX2aaCEqINIcIsHJMGCs=", signature);
    }

    @Test
    public void oneGroupNoChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel-groups\":\"cg1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channelGroups(Collections.singletonList("cg1")).sync();

        assertEquals(0, result.getChannels().size());
        assertEquals(1, result.getChannelGroups().size());

        assertEquals(2, result.getChannelGroups().get("cg1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("m47WC6N_zx9EhnX3XcO-bNS0wO5Fw4sj8154NoyyVKs=", signature);
    }

    @Test
    public void oneGroupOneChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}},\"channel-groups\":\"cg1\"},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).channelGroups(Arrays.asList("cg1")).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(1, result.getChannelGroups().size());

        assertEquals(1, result.getChannelGroups().get("cg1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("TXuXp8mvpziVIeuyd8b0dgNXRYEJ9oBIhktnhilt7Xs=", signature);
    }

    @Test
    public void oneGroupOneChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}},\"channel-groups\":\"cg1\"},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channels(Collections.singletonList("ch1")).channelGroups(Arrays.asList("cg1")).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(1, result.getChannelGroups().size());

        assertEquals(2, result.getChannelGroups().get("cg1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());

        assertEquals(2, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("KbQ7nkSPXZO_iTfn7nNfCO_Ut7OsEg4Hzb321vGgYUc=", signature);

    }

    @Test
    public void oneGroupTwoChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}},\"channel-groups\":\"cg1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channels(Arrays.asList("ch1", "ch2")).channelGroups(Arrays.asList("cg1")).sync();

        assertEquals(2, result.getChannels().size());
        assertEquals(1, result.getChannelGroups().size());

        assertEquals(1, result.getChannelGroups().get("cg1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("gstN6_LP_z9LVMcBY4nAgXZQu_AcAta6Cuq2nWglbmw=", signature);

    }

    @Test
    public void oneGroupTwoChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}},\"channel-groups\":\"cg1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channels(Arrays.asList("ch1", "ch2")).channelGroups(Collections.singletonList("cg1")).sync();

        assertEquals(2, result.getChannels().size());
        assertEquals(1, result.getChannelGroups().size());

        assertEquals(2, result.getChannelGroups().get("cg1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());

        assertEquals(2, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key2").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("p6eQF50jjcKvWQ1rulCE41WHKlJyp11byW989x4FEFc=", signature);
    }


    @Test
    public void twoGroupNoChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channelGroups(Arrays.asList("cg1", "cg2")).sync();

        assertEquals(0, result.getChannels().size());
        assertEquals(2, result.getChannelGroups().size());

        assertEquals(1, result.getChannelGroups().get("cg1").size());
        assertEquals(1, result.getChannelGroups().get("cg2").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("rBRwhq15luvJ5IOFMgQqICHP_K10h0lm1m4TbKTgdWE=", signature);
    }

    @Test
    public void twoGroupNoChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channelGroups(Arrays.asList("cg1", "cg2")).sync();

        assertEquals(0, result.getChannels().size());
        assertEquals(2, result.getChannelGroups().size());

        assertEquals(2, result.getChannelGroups().get("cg1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key2").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("_78G4e79CvlnpKJfQTvORBGzimTFZfzxY5tcEVLW4Xs=", signature);
    }

    @Test
    public void twoGroupOneChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}},\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channelGroups(Arrays.asList("cg1", "cg2")).channels(Arrays.asList("ch1")).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(2, result.getChannelGroups().size());

        assertEquals(1, result.getChannelGroups().get("cg1").size());
        assertEquals(1, result.getChannelGroups().get("cg2").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("8y9GXwhZFO1m9saIe1FVkAu5jIPMu-U9eHVSy0ybyio=", signature);
    }

    @Test
    public void twoGroupOneChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}},\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channelGroups(Arrays.asList("cg1", "cg2")).channels(Collections.singletonList("ch1")).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(2, result.getChannelGroups().size());

        assertEquals(2, result.getChannelGroups().get("cg1").size());
        assertEquals(2, result.getChannelGroups().get("cg2").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key2").getClass());

        assertEquals(2, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("GIgfQnwun0NgZNYD_rDtS3DkIuK7xMyWN8p_7kG4y5Q=", signature);
    }

    @Test
    public void twoGroupTwoChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}},\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channelGroups(Arrays.asList("cg1", "cg2")).channels(Arrays.asList("ch1", "ch2")).sync();

        assertEquals(2, result.getChannels().size());
        assertEquals(2, result.getChannelGroups().size());

        assertEquals(1, result.getChannelGroups().get("cg1").size());
        assertEquals(1, result.getChannelGroups().get("cg2").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(1, result.getChannels().get("ch2").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("Kvm2uRwgJ60iGYyvI60KC_WIdU3cxIKyYaX1LK0PL3c=", signature);
    }

    @Test
    public void twoGroupTwoChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}},\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channelGroups(Arrays.asList("cg1", "cg2")).channels(Arrays.asList("ch1", "ch2")).sync();

        assertEquals(2, result.getChannels().size());
        assertEquals(2, result.getChannelGroups().size());

        assertEquals(2, result.getChannelGroups().get("cg1").size());
        assertEquals(2, result.getChannelGroups().get("cg2").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key2").getClass());

        assertEquals(2, result.getChannels().get("ch1").size());
        assertEquals(2, result.getChannels().get("ch2").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key2").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("uptOF3nuKzGHSJ-swKQFj4XSgzKetO5uHl9iRkPeYGY=", signature);
    }

    @Test
    public void noGroupsOneChannelOneKeyTTLTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .withQueryParam("ttl", matching("1334"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).ttl(1334).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(0, result.getChannelGroups().size());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("VcJTCcpMugw4Stm409oUeaLWR0-ZewRJNKKetI6A1Jk=", signature);
    }

    @Test
    public void noGroupsOneChannelOneReadKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("1"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).read(true).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(0, result.getChannelGroups().size());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("9ngAxIIhZUq6qqCAPXwcpoqPlSRXDokohXmgZ9jEZsM=", signature);
    }

    @Test
    public void noGroupsOneChannelOneWriteKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("1"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).write(true).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(0, result.getChannelGroups().size());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("ZNoFdFsoGjNmKLoOFPW7Xn9es3c41ZG1gHZ3Szz9BbA=", signature);
    }

    @Test
    public void noGroupsOneChannelOneKeyManageTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("1"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).manage(true).sync();

        assertEquals(1, result.getChannels().size());
        assertEquals(0, result.getChannelGroups().size());

        assertEquals(1, result.getChannels().get("ch1").size());
        assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("lMTGADViQb_pQPkwOypoiRhiJGdN2prTlWlHCfZrgx0=", signature);
    }

    @Test
    public void testIsAuthRequiredSuccessSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("instanceid", matching("PubNubInstanceId"))
                .withQueryParam("requestid", matching("PubNubRequestId"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        pubnub.getConfiguration().setAuthKey("myKey");
        partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/v1/auth/grant/sub-key/mySubscribeKey.*")));
        assertEquals(1, requests.size());
        String signature = requests.get(0).queryParameter("signature").firstValue();
        assertEquals("nUZe6LEDe2RGPeh4qoe-IpjAYzBf8RJMtSXI7ktaah8=", signature);
    }

    @Test
    public void testOperationTypeSuccessAsync() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("HlyfXDFhdgNhKfBzGaouxh2T2SRimm4bVq_JVKLRPQI="))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        final AtomicInteger atomic = new AtomicInteger(0);

        partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).async(new PNCallback<PNAccessManagerGrantResult>() {
            @Override
            public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                if (status != null && status.getOperation()== PNOperationType.PNAccessManagerGrant) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @Test
    public void testNullSecretKey() {
        pubnub.getConfiguration().setSecretKey(null);

        try {
            partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).sync();
            throw new RuntimeException("should never reach here");
        } catch (PubNubException e) {
            assertEquals("ULS configuration failed. Secret Key not configured.", e.getPubnubError().getMessage());
        }
    }

    @Test
    public void testEmptySecretKey() {
        pubnub.getConfiguration().setSecretKey("");

        try {
            partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).sync();
            throw new RuntimeException("should never reach here");
        } catch (PubNubException e) {
            assertEquals("ULS configuration failed. Secret Key not configured.", e.getPubnubError().getMessage());
        }
    }

    @Test
    public void testNullSubscribeKey() {
        pubnub.getConfiguration().setSubscribeKey(null);

        try {
            partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).sync();
            throw new RuntimeException("should never reach here");
        } catch (PubNubException e) {
            assertEquals("ULS configuration failed. Subscribe Key not configured.", e.getPubnubError().getMessage());
        }
    }

    @Test
    public void testEmptySubscribeKey() {
        pubnub.getConfiguration().setSubscribeKey("");

        try {
            partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).sync();
            throw new RuntimeException("should never reach here");
        } catch (PubNubException e) {
            assertEquals("ULS configuration failed. Subscribe Key not configured.", e.getPubnubError().getMessage());
        }
    }

    @Test
    public void testNullPublishKey() {
        pubnub.getConfiguration().setPublishKey(null);

        try {
            partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).sync();
            throw new RuntimeException("should never reach here");
        } catch (PubNubException e) {
            assertEquals("ULS configuration failed. Publish Key not configured.", e.getPubnubError().getMessage());
        }

    }

    @Test
    public void testEmptyPublishKey() {
        pubnub.getConfiguration().setPublishKey("");

        try {
            partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).sync();
            throw new RuntimeException("should never reach here");
        } catch (PubNubException e) {
            assertEquals("ULS configuration failed. Publish Key not configured.", e.getPubnubError().getMessage());
        }
    }

    @Test
    public void testMissingChannelsAndChannelGroup() {
        try {
            partialGrant.authKeys(Collections.singletonList("key1")).sync();
            throw new RuntimeException("should never reach here");
        } catch (PubNubException e) {
            assertEquals("Channel and Group Missing.", e.getPubnubError().getMessage());
        }
    }

    @Test
    public void testNullPayload() {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"service\":\"Access Manager\",\"status\":200}")));

        try {
            partialGrant.authKeys(Collections.singletonList("key1")).channels(Collections.singletonList("ch1")).sync();
        } catch (PubNubException e) {
            assertEquals("Parsing Error", e.getPubnubError().getMessage());
        }
    }

    @Test
    public void testNullAuthKeyAsync() throws PubNubException {

        final AtomicInteger atomic = new AtomicInteger(0);

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("signature", matching("HlyfXDFhdgNhKfBzGaouxh2T2SRimm4bVq_JVKLRPQI="))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));


        partialGrant.channels(Collections.singletonList("ch1")).async(new PNCallback<PNAccessManagerGrantResult>() {
            @Override
            public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                if (status != null && status.getOperation()== PNOperationType.PNAccessManagerGrant && status.isError()) {
                    atomic.incrementAndGet();
                }
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

}

