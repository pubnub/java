package com.pubnub.api.endpoints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.history.PNFetchMessagesResult;
import org.junit.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;


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

}
