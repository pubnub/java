package com.pubnub.api.endpoints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubRuntimeException;
import com.pubnub.api.models.consumer.history.PNFetchMessageItem;
import com.pubnub.api.models.consumer.history.PNFetchMessagesResult;
import org.junit.*;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


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

        Assert.assertEquals(2, response.getChannels().size());
        Assert.assertTrue(response.getChannels().containsKey("mychannel"));
        Assert.assertTrue(response.getChannels().containsKey("my_channel"));
        Assert.assertEquals(1, response.getChannels().get("mychannel").size());
        Assert.assertEquals(2, response.getChannels().get("my_channel").size());
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

        Assert.assertEquals(2, response.getChannels().size());
        Assert.assertTrue(response.getChannels().containsKey("mychannel"));
        Assert.assertTrue(response.getChannels().containsKey("my_channel"));
        Assert.assertEquals(1, response.getChannels().get("mychannel").size());
        Assert.assertEquals(2, response.getChannels().get("my_channel").size());

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

        Assert.assertEquals(2, response.getChannels().size());
        Assert.assertTrue(response.getChannels().containsKey("mychannel"));
        Assert.assertTrue(response.getChannels().containsKey("my_channel"));
        Assert.assertEquals(1, response.getChannels().get("mychannel").size());
        Assert.assertEquals(1, response.getChannels().get("my_channel").size());
    }

    @Test
    public void should_return_userMessageType_when_userMessageTypePresent_in_response_and_includeMessageType_set_to_true() throws PubNubException {
        final String expectedChannelName = "myChannel";
        String fetchMessagesUrl = "/v3/history/sub-key/mySubscribeKey/channel/" + expectedChannelName;
        final String expectedUserMessageType = "myMessageType";
        final Integer expectedPnMessageType = null;

        stubFor(get(urlPathEqualTo(fetchMessagesUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getJsonWithSpecificChannelAndUserMessageTypeAndPnMessageType(expectedChannelName, expectedUserMessageType, expectedPnMessageType))));

        final PNFetchMessagesResult fetchMessagesResult = pubnub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .includeMessageType(true)
                .sync();

        String messageTypeValue = fetchMessagesResult.getChannels().get(expectedChannelName).get(0).getMessageType().getValue();
        assertEquals(expectedUserMessageType, messageTypeValue);
        assertNull(fetchMessagesResult.getChannels().get(expectedChannelName).get(0).getSpaceId());
    }

    @Test
    public void should_return_null_when_getting_messageType_but_includeMessageType_is_set_to_false() throws PubNubException {
        final String expectedChannelName = "myChannel";
        String fetchMessagesUrl = "/v3/history/sub-key/mySubscribeKey/channel/" + expectedChannelName;

        stubFor(get(urlPathEqualTo(fetchMessagesUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getJsonWithNoUserMessageTypeAndNoPnMessageType())));

        final PNFetchMessagesResult fetchMessagesResult = pubnub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .includeMessageType(false)
                .sync();

        PNFetchMessageItem pnFetchMessageItem = fetchMessagesResult.getChannels().get(expectedChannelName).get(0);
        assertNull(pnFetchMessageItem.getMessageType());
    }

    @Test
    public void should_return_spaceId_when_includeSpaceId_is_set_to_true() throws PubNubException {
        final String expectedChannelName = "myChannel";
        String fetchMessagesUrl = "/v3/history/sub-key/mySubscribeKey/channel/" + expectedChannelName;
        final String expectedSpaceIdValue = "mySpace";

        stubFor(get(urlPathEqualTo(fetchMessagesUrl))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getJsonWithSpecificChannelAndSpaceId(expectedChannelName, expectedSpaceIdValue))));

        final PNFetchMessagesResult fetchMessagesResult = pubnub.fetchMessages()
                .channels(Collections.singletonList(expectedChannelName))
                .includeMessageType(true)
                .sync();

        String spaceIdValue = fetchMessagesResult.getChannels().get(expectedChannelName).get(0).getSpaceId().getValue();
        assertEquals(expectedSpaceIdValue, spaceIdValue);
    }

    private String getJsonWithSpecificChannelAndUserMessageTypeAndPnMessageType(String channelName, String userMessageType, Integer pnMessageType) {
        return "{\n" +
                "  \"status\": 200,\n" +
                "  \"error\": false,\n" +
                "  \"error_message\": \"\",\n" +
                "  \"channels\": {\n" +
                "    \"" + channelName + "\": [\n" +
                "      {\n" +
                "        \"message\": \"0_msg\",\n" +
                "        \"timetoken\": \"16703466236354401\",\n" +
                "        \"message_type\": " + pnMessageType + ",\n" +
                "        \"type\": \"" + userMessageType + "\",\n" +
                "        \"uuid\": \"client-d60257a5-2e70-4b40-b9d6-5dacf664fc0c\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"message\": \"1_msg\",\n" +
                "        \"timetoken\": \"16703466289837512\",\n" +
                "        \"message_type\": 1,\n" +
                "        \"type\": \"customMessageTypeHahaha\",\n" +
                "        \"uuid\": \"client-d60257a5-2e70-4b40-b9d6-5dacf664fc0c\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
    }

    private String getJsonWithNoUserMessageTypeAndNoPnMessageType() {
        return "{\n" +
                "  \"status\": 200,\n" +
                "  \"error\": false,\n" +
                "  \"error_message\": \"\",\n" +
                "  \"channels\": {\n" +
                "    \"myChannel\": [\n" +
                "      {\n" +
                "        \"message\": \"0_msg\",\n" +
                "        \"timetoken\": \"16703466236354401\",\n" +
                "        \"uuid\": \"client-d60257a5-2e70-4b40-b9d6-5dacf664fc0c\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"message\": \"1_msg\",\n" +
                "        \"timetoken\": \"16703466289837512\",\n" +
                "        \"uuid\": \"client-d60257a5-2e70-4b40-b9d6-5dacf664fc0c\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
    }

    private String getJsonWithSpecificChannelAndSpaceId(String channelName, String spaceId) {
        return "{\n" +
                "  \"status\": 200,\n" +
                "  \"error\": false,\n" +
                "  \"error_message\": \"\",\n" +
                "  \"channels\": {\n" +
                "    \"" + channelName + "\": [\n" +
                "      {\n" +
                "        \"message\": \"0_msg\",\n" +
                "        \"timetoken\": \"16703466236354401\",\n" +
                "        \"message_type\": 0,\n" +
                "        \"type\": \"myCustomMessageType\",\n" +
                "        \"space_id\": \"" + spaceId + "\",\n" +
                "        \"uuid\": \"client-d60257a5-2e70-4b40-b9d6-5dacf664fc0c\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"message\": \"1_msg\",\n" +
                "        \"timetoken\": \"16703466289837512\",\n" +
                "        \"message_type\": 1,\n" +
                "        \"type\": \"customMessageTypeHahaha\",\n" +
                "        \"space_id\": \"customSpaceId\",\n" +
                "        \"uuid\": \"client-d60257a5-2e70-4b40-b9d6-5dacf664fc0c\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
    }

}
