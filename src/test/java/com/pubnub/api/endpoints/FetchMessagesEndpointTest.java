package com.pubnub.api.endpoints;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.history.PNFetchMessagesResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.assertEquals;


public class FetchMessagesEndpointTest extends TestHarness {

    private FetchMessages partialHistory;
    private PubNub pubnub;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
        partialHistory = pubnub.fetchMessages();
        wireMockRule.start();
    }


    @Test
    public void testSyncSuccess() throws PubNubException {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", \"channels\": {\"my_channel\":[{\"message\":\"hihi\",\"timetoken\":\"14698320467224036\"},{\"message\":\"Hey\",\"timetoken\":\"14698320468265639\"}],\"mychannel\":[{\"message\":\"sample message\",\"timetoken\":\"14369823849575729\"}]}}")));

        PNFetchMessagesResult response = partialHistory.channels(Collections.singletonList("mychannel,my_channel")).maximumPerChannel(25).sync();

        Assert.assertEquals(response.getChannels().size(), 2);
        Assert.assertEquals(response.getChannels().containsKey("mychannel"), true);
        Assert.assertEquals(response.getChannels().containsKey("my_channel"), true);
        Assert.assertEquals(response.getChannels().get("mychannel").size(),1);
        Assert.assertEquals(response.getChannels().get("my_channel").size(),2);
    }

    @Test
    public void testSyncAuthSuccess() throws PubNubException {
        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", \"channels\": {\"my_channel\":[{\"message\":\"hihi\",\"timetoken\":\"14698320467224036\"},{\"message\":\"Hey\",\"timetoken\":\"14698320468265639\"}],\"mychannel\":[{\"message\":\"sample message\",\"timetoken\":\"14369823849575729\"}]}}")));

        pubnub.getConfiguration().setAuthKey("authKey");

        PNFetchMessagesResult response = partialHistory.channels(Collections.singletonList("mychannel,my_channel")).maximumPerChannel(25).sync();

        Assert.assertEquals(response.getChannels().size(), 2);
        Assert.assertEquals(response.getChannels().containsKey("mychannel"), true);
        Assert.assertEquals(response.getChannels().containsKey("my_channel"), true);
        Assert.assertEquals(response.getChannels().get("mychannel").size(),1);
        Assert.assertEquals(response.getChannels().get("my_channel").size(),2);

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals("authKey", requests.get(0).queryParameter("auth").firstValue());
        assertEquals(1, requests.size());
    }

    @Test
    public void testSyncEncryptedSuccess() throws PubNubException {
        pubnub.getConfiguration().setCipherKey("testCipher");

        stubFor(get(urlPathEqualTo("/v3/history/sub-key/mySubscribeKey/channel/mychannel,my_channel"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"error\": false, \"error_message\": \"\", \"channels\": {\"my_channel\":[{\"message\":\"jC/yJ2y99BeYFYMQ7c53pg==\",\"timetoken\":\"14797423056306675\"}],\"mychannel\":[{\"message\":\"jC/yJ2y99BeYFYMQ7c53pg==\",\"timetoken\":\"14797423056306675\"}]}}")));

        PNFetchMessagesResult response = partialHistory.channels(Collections.singletonList("mychannel,my_channel")).maximumPerChannel(25).sync();

        Assert.assertEquals(response.getChannels().size(), 2);
        Assert.assertEquals(response.getChannels().containsKey("mychannel"), true);
        Assert.assertEquals(response.getChannels().containsKey("my_channel"), true);
        Assert.assertEquals(response.getChannels().get("mychannel").size(),1);
        Assert.assertEquals(response.getChannels().get("my_channel").size(),1);
    }

}
