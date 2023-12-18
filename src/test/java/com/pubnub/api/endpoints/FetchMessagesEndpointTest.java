package com.pubnub.api.endpoints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.crypto.CryptoModule;
import com.pubnub.api.models.consumer.history.HistoryMessageType;
import com.pubnub.api.models.consumer.history.PNFetchMessagesResult;
import org.jetbrains.annotations.Nullable;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class FetchMessagesEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(PORT), false);

    private FetchMessages partialHistory;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        partialHistory = pubnub.fetchMessages();
        wireMockRule.start();
    }


    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void testSyncSuccess() throws PubNubException {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":[{\"message\":\"hihi\",\"timetoken\":\"14698320467224036\"}," +
                        "{\"message\":\"Hey\",\"timetoken\":\"14698320468265639\"}]," +
                        "\"mychannel\":[{\"message\":\"sample message\",\"timetoken\":\"14369823849575729\"}]}}")));

        PNFetchMessagesResult response =
                partialHistory.channels(Arrays.asList("mychannel,my_channel")).maximumPerChannel(25).sync();

        assert response != null;

        Assert.assertEquals(response.getChannels().size(), 2);
        Assert.assertTrue(response.getChannels().containsKey("mychannel"));
        Assert.assertTrue(response.getChannels().containsKey("my_channel"));
        Assert.assertEquals(response.getChannels().get("mychannel").size(), 1);
        Assert.assertEquals(response.getChannels().get("my_channel").size(), 2);
    }

    @Test
    public void testSyncAuthSuccess() throws PubNubException {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":[{\"message\":\"hihi\",\"timetoken\":\"14698320467224036\"}," +
                        "{\"message\":\"Hey\",\"timetoken\":\"14698320468265639\"}]," +
                        "\"mychannel\":[{\"message\":\"sample message\",\"timetoken\":\"14369823849575729\"}]}}")));

        pubnub.getConfiguration().setAuthKey("authKey");

        PNFetchMessagesResult response =
                partialHistory.channels(Arrays.asList("mychannel,my_channel")).maximumPerChannel(25).sync();

        assert response != null;

        Assert.assertEquals(response.getChannels().size(), 2);
        Assert.assertTrue(response.getChannels().containsKey("mychannel"));
        Assert.assertTrue(response.getChannels().containsKey("my_channel"));
        Assert.assertEquals(response.getChannels().get("mychannel").size(), 1);
        Assert.assertEquals(response.getChannels().get("my_channel").size(), 2);

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals("authKey", requests.get(0).queryParameter("auth").firstValue());
        assertEquals(1, requests.size());
    }

    @Test
    public void testSyncEncryptedSuccess() throws PubNubException {
        pubnub.getConfiguration().setCipherKey("testCipher");

        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", " +
                        "\"channels\": {\"my_channel\":[{\"message\":\"jC/yJ2y99BeYFYMQ7c53pg==\"," +
                        "\"timetoken\":\"14797423056306675\"}]," +
                        "\"mychannel\":[{\"message\":\"jC/yJ2y99BeYFYMQ7c53pg==\"," +
                        "\"timetoken\":\"14797423056306675\"}]}}")));

        PNFetchMessagesResult response =
                partialHistory.channels(Arrays.asList("mychannel,my_channel")).maximumPerChannel(25).sync();

        assert response != null;

        Assert.assertEquals(response.getChannels().size(), 2);
        Assert.assertTrue(response.getChannels().containsKey("mychannel"));
        Assert.assertTrue(response.getChannels().containsKey("my_channel"));
        Assert.assertEquals(response.getChannels().get("mychannel").size(), 1);
        Assert.assertEquals(response.getChannels().get("my_channel").size(), 1);
    }

    @Test
    public void testProcessMessageEncryptedWithCrypto() throws PubNubException {
        pubnub.getConfiguration().setCryptoModule(CryptoModule.createAesCbcCryptoModule("enigma", false));
        String message = "Hello world.";
        String messageEncrypted = "bk8x+ZEg+Roq8ngUo7lfFg==";
        JsonElement result = partialHistory.processMessage(new JsonPrimitive(messageEncrypted));
        assertEquals(new JsonPrimitive(message), result);
    }

    @Test
    public void testProcessMessageUnencryptedWithCrypto() throws PubNubException {
        pubnub.getConfiguration().setCryptoModule(CryptoModule.createAesCbcCryptoModule("enigma", false));
        String message = "Hello world.";
        PubNubException exception = assertThrows(PubNubException.class, () -> {
            partialHistory.processMessage(new JsonPrimitive(message));
        });
        assertEquals(exception.getPubnubError(), PubNubErrorBuilder.PNERROBJ_PNERR_CRYPTO_IS_CONFIGURED_BUT_MESSAGE_IS_NOT_ENCRYPTED);
    }

    @Test
    public void testProcessMessageWithPnOtherEncryptedWithCrypto() throws PubNubException {
        pubnub.getConfiguration().setCryptoModule(CryptoModule.createAesCbcCryptoModule("enigma", false));
        String message = "Hello world.";
        String messageEncrypted = "bk8x+ZEg+Roq8ngUo7lfFg==";

        JsonObject messageObject = new JsonObject();
        messageObject.addProperty("something", "some text");
        messageObject.addProperty("pn_other", messageEncrypted);

        JsonObject expectedObject = new JsonObject();
        expectedObject.addProperty("something", "some text");
        expectedObject.addProperty("pn_other", message);

        JsonElement result = partialHistory.processMessage(messageObject);

        assertEquals(expectedObject, result);

    }

    @Test
    public void testMessageTypesAreProperlyDeserialized() throws PubNubException {
        stubFor(
                get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/myChannel")).willReturn(
                        aResponse().withBody(
                                "{\n" +
                                        "          \"status\": 200,\n" +
                                        "          \"error\": false,\n" +
                                        "          \"error_message\": \"\",\n" +
                                        "          \"channels\": {\n" +
                                        "            \"myChannel\": [\n" +
                                        "              {\n" +
                                        "                \"message\": \"thisIsMessage1\",\n" +
                                        "                \"timetoken\": \"14797423056306675\",\n" +
                                        "                \"message_type\": 0\n" +
                                        "              },\n" +
                                        "              {\n" +
                                        "                \"message\": \"thisIsMessage1\",\n" +
                                        "                \"timetoken\": \"14797423056306675\",\n" +
                                        "                \"message_type\": null\n" +
                                        "              },\n" +
                                        "              {\n" +
                                        "                \"message\": \"thisIsMessage2\",\n" +
                                        "                \"timetoken\": \"14797423056306676\",\n" +
                                        "                \"message_type\": 4\n" +
                                        "              }\n" +
                                        "            ]\n" +
                                        "          }\n" +
                                        "        }"
                        )
                )
        );

        @Nullable PNFetchMessagesResult response = pubnub.fetchMessages()
                .channels(Collections.singletonList("myChannel"))
                .includeMessageType(true)
                .sync();
        assertEquals(
                response.getChannels().values().stream().flatMap(items -> items.stream().map(item -> item.getMessageType())).collect(Collectors.toList()),
                Lists.newArrayList(HistoryMessageType.MESSAGE, HistoryMessageType.MESSAGE, HistoryMessageType.FILE)
        );
    }

    @Test
    public void testMessageTypesAreNull_includeMessageTypeIsFalse() throws PubNubException {
        stubFor(
                get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/myChannel")).willReturn(
                        aResponse().withBody(
                                "{\n" +
                                        "          \"status\": 200,\n" +
                                        "          \"error\": false,\n" +
                                        "          \"error_message\": \"\",\n" +
                                        "          \"channels\": {\n" +
                                        "            \"myChannel\": [\n" +
                                        "              {\n" +
                                        "                \"message\": \"thisIsMessage1\",\n" +
                                        "                \"timetoken\": \"14797423056306675\"\n" +
                                        "              },\n" +
                                        "              {\n" +
                                        "                \"message\": \"thisIsMessage1\",\n" +
                                        "                \"timetoken\": \"14797423056306675\"\n" +
                                        "              },\n" +
                                        "              {\n" +
                                        "                \"message\": \"thisIsMessage2\",\n" +
                                        "                \"timetoken\": \"14797423056306676\"\n" +
                                        "              }\n" +
                                        "            ]\n" +
                                        "          }\n" +
                                        "        }"
                        )
                )
        );

        @Nullable PNFetchMessagesResult response = pubnub.fetchMessages()
                .channels(Collections.singletonList("myChannel"))
                .includeMessageType(false)
                .sync();
        assertEquals(
                response.getChannels().values().stream().flatMap(items -> items.stream().map(item -> item.getMessageType())).collect(Collectors.toList()),
                Lists.newArrayList(null, null, null)
        );
    }
}
