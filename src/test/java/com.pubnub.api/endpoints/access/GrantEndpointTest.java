package com.pubnub.api.endpoints.access;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerKeyData;
import com.pubnub.api.endpoints.TestHarness;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;

public class GrantEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    private Grant partialGrant;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException {

        pubnub = this.createPubNubInstance(8080);
        partialGrant = pubnub.grant();

        pubnub.getConfiguration().setSecretKey("secretKey");

    }

    @Test
    public void NoGroupsOneChannelOneKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("KQ1g-hpCnWGD6DfR0J30xkB0KwBhbIBlLqKi8GNoD7M%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channels(Arrays.asList("ch1")).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(0, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

    }

    @Test
    public void NoGroupsOneChannelTwoKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("signature", matching("hlFr8HmcEIK51rEc-jhwym89SgCeAKvzhTYphwD_Sqw%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channels(Arrays.asList("ch1")).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(0, result.getChannelGroups().size());

        Assert.assertEquals(2, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());
    }

    @Test
    public void NoGroupsTwoChannelOneKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("YSf7ataHaZQj0-kE9P5-0ACR4fjrzzGJbzRSoF3J4UQ%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channels(Arrays.asList("ch1", "ch2")).sync();

        Assert.assertEquals(2, result.getChannels().size());
        Assert.assertEquals(0, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(1, result.getChannels().get("ch2").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());
    }

    @Test
    public void NoGroupsTwoChannelTwoKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("signature", matching("q9gzdsS-Z_Ff21ZfiXEakG3fkOWsT49MEn4ZKFxjehY%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channels(Arrays.asList("ch1", "ch2")).sync();

        Assert.assertEquals(2, result.getChannels().size());
        Assert.assertEquals(0, result.getChannelGroups().size());

        Assert.assertEquals(2, result.getChannels().get("ch1").size());
        Assert.assertEquals(2, result.getChannels().get("ch2").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key2").getClass());
    }

    @Test
    public void OneGroupNoChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("BGNIzQq6T-X_ES7RjSeUh0L_THx6RP956DBRIfilQCE%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel-groups\":\"cg1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channelGroups(Arrays.asList("cg1")).sync();

        Assert.assertEquals(0, result.getChannels().size());
        Assert.assertEquals(1, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());

    }

    @Test
    public void OneGroupNoChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("signature", matching("vBxdD0-HPCuKoVlXL2c7JYeAmDVBN-TGodVJXFbOLcI%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel-groups\":\"cg1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channelGroups(Arrays.asList("cg1")).sync();

        Assert.assertEquals(0, result.getChannels().size());
        Assert.assertEquals(1, result.getChannelGroups().size());

        Assert.assertEquals(2, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());

    }

    @Test
    public void OneGroupOneChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("eRwmB_DpD7rdR6qZDm4fh2PiXfdYbFXbwIcq-oVQOaY%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}},\"channel-groups\":\"cg1\"},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channels(Arrays.asList("ch1")).channelGroups(Arrays.asList("cg1")).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(1, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

    }

    @Test
    public void OneGroupOneChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("signature", matching("4ogB_IAapikN32FOCXJQD2FOHfrFbZqFrOCRry2C3AY%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}},\"channel-groups\":\"cg1\"},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channels(Arrays.asList("ch1")).channelGroups(Arrays.asList("cg1")).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(1, result.getChannelGroups().size());

        Assert.assertEquals(2, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());

        Assert.assertEquals(2, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());

    }

    @Test
    public void OneGroupTwoChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("SMoWF-RgW-2wGZ-wbzkaKwd6b3Zigq2gYFCxmw0Equs%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}},\"channel-groups\":\"cg1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channels(Arrays.asList("ch1", "ch2")).channelGroups(Arrays.asList("cg1")).sync();

        Assert.assertEquals(2, result.getChannels().size());
        Assert.assertEquals(1, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());

    }

    @Test
    public void OneGroupTwoChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("channel-group", matching("cg1"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("signature", matching("FF-8u5mhNhbrWD5_XyIrCOPitG8YQbmeGYZXaiOQYBA%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}},\"channel-groups\":\"cg1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channels(Arrays.asList("ch1", "ch2")).channelGroups(Arrays.asList("cg1")).sync();

        Assert.assertEquals(2, result.getChannels().size());
        Assert.assertEquals(1, result.getChannelGroups().size());

        Assert.assertEquals(2, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());

        Assert.assertEquals(2, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key2").getClass());
    }

    //

    @Test
    public void TwoGroupNoChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("6mss-Lh0eaN0G_TfY6WfFKBqePYe1j4blNGwJPMxTOs%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channelGroups(Arrays.asList("cg1", "cg2")).sync();

        Assert.assertEquals(0, result.getChannels().size());
        Assert.assertEquals(2, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(1, result.getChannelGroups().get("cg2").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());

    }

    @Test
    public void TwoGroupNoChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("signature", matching("q2AUprf6ZUD4N96hMTCVm7hoMfDRC7xfG4XPyLLNWB4%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channelGroups(Arrays.asList("cg1", "cg2")).sync();

        Assert.assertEquals(0, result.getChannels().size());
        Assert.assertEquals(2, result.getChannelGroups().size());

        Assert.assertEquals(2, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key2").getClass());

    }

    @Test
    public void TwoGroupOneChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("pRlfFAiCrKIqSPUjZgaI3ZLq9wK4-VUvhf_ispH6ow8%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}},\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channelGroups(Arrays.asList("cg1", "cg2")).channels(Arrays.asList("ch1")).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(2, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(1, result.getChannelGroups().get("cg2").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
    }

    @Test
    public void TwoGroupOneChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("signature", matching("8H6V_0LqSBinnK5Po5LA29ny8CH8eGonzB6UomoXvXo%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}},\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channelGroups(Arrays.asList("cg1", "cg2")).channels(Arrays.asList("ch1")).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(2, result.getChannelGroups().size());

        Assert.assertEquals(2, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(2, result.getChannelGroups().get("cg2").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key2").getClass());

        Assert.assertEquals(2, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());

    }

    @Test
    public void TwoGroupTwoChannelOneKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("yBYF6-JTkM8F-U-K4ACwzrPEN0iihJQUygz4JOh4rwU%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}},\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channelGroups(Arrays.asList("cg1", "cg2")).channels(Arrays.asList("ch1", "ch2")).sync();

        Assert.assertEquals(2, result.getChannels().size());
        Assert.assertEquals(2, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(1, result.getChannelGroups().get("cg2").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(1, result.getChannels().get("ch2").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());
    }

    @Test
    public void TwoGroupTwoChannelTwoKey() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1,ch2"))
                .withQueryParam("channel-group", matching("cg1,cg2"))
                .withQueryParam("auth", matching("key1,key2"))
                .withQueryParam("signature", matching("JLTTsGTwaaEORmY_zoZcCzYl4qEZVkid-YRSvKYMwUU%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"channel-group+auth\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channels\":{\"ch1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"ch2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}},\"channel-groups\":{\"cg1\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}},\"cg2\":{\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0},\"key2\":{\"r\":0,\"w\":0,\"m\":0}}}}},\"service\":\"Access Manager\",\"status\":200}\n")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1", "key2")).channelGroups(Arrays.asList("cg1", "cg2")).channels(Arrays.asList("ch1", "ch2")).sync();

        Assert.assertEquals(2, result.getChannels().size());
        Assert.assertEquals(2, result.getChannelGroups().size());

        Assert.assertEquals(2, result.getChannelGroups().get("cg1").size());
        Assert.assertEquals(2, result.getChannelGroups().get("cg2").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg1").get("key2").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannelGroups().get("cg2").get("key2").getClass());

        Assert.assertEquals(2, result.getChannels().get("ch1").size());
        Assert.assertEquals(2, result.getChannels().get("ch2").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key2").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key1").getClass());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch2").get("key2").getClass());

    }

    @Test
    public void NoGroupsOneChannelOneKeyTTLTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("pzSNXZSiBMkzg15vOQrKoDkue8OWoxEB1afup9Godac%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .withQueryParam("ttl", matching("1334"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channels(Arrays.asList("ch1")).ttl(1334).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(0, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

    }

    @Test
    public void NoGroupsOneChannelOneReadKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("uVcT9Q_XiLPpR1kmIzD9OJ6mTa1V1GyvyBcKBWSDrJU%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("1"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channels(Arrays.asList("ch1")).read(true).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(0, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

    }

    @Test
    public void NoGroupsOneChannelOneWriteKeyTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("-MirRhU4989XbPZEza82xvhZ0CdQIngM8b7qSUL1kDI%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("1"))
                .withQueryParam("m", matching("0"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channels(Arrays.asList("ch1")).write(true).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(0, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

    }

    @Test
    public void NoGroupsOneChannelOneKeyManageTest() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v1/auth/grant/sub-key/mySubscribeKey"))
                .withQueryParam("pnsdk", matching("Java/suchJava"))
                .withQueryParam("channel", matching("ch1"))
                .withQueryParam("auth", matching("key1"))
                .withQueryParam("signature", matching("_znR67zw5cdCq3Cmn1QHUHtFolkquYARh_JYCeMb8ig%3D%0A"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("timestamp", matching("1337"))
                .withQueryParam("r", matching("0"))
                .withQueryParam("w", matching("0"))
                .withQueryParam("m", matching("1"))
                .willReturn(aResponse().withBody("{\"message\":\"Success\",\"payload\":{\"level\":\"user\",\"subscribe_key\":\"sub-c-82ab2196-b64f-11e5-8622-0619f8945a4f\",\"ttl\":1,\"channel\":\"ch1\",\"auths\":{\"key1\":{\"r\":0,\"w\":0,\"m\":0}}},\"service\":\"Access Manager\",\"status\":200}")));

        PNAccessManagerGrantResult result = partialGrant.authKeys(Arrays.asList("key1")).channels(Arrays.asList("ch1")).manage(true).sync();

        Assert.assertEquals(1, result.getChannels().size());
        Assert.assertEquals(0, result.getChannelGroups().size());

        Assert.assertEquals(1, result.getChannels().get("ch1").size());
        Assert.assertEquals(PNAccessManagerKeyData.class, result.getChannels().get("ch1").get("key1").getClass());

    }

}
