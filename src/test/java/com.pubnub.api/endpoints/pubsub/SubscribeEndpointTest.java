package com.pubnub.api.endpoints.pubsub;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.server.SubscribeEnvelope;
import com.pubnub.api.models.server.SubscribeMessage;
import com.pubnub.api.endpoints.TestHarness;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SubscribeEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    PubNub pubnub;
    Subscribe instance;

    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
        instance = new Subscribe(pubnub);
    }

    @Test
    public void subscribeChannelSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        SubscribeEnvelope subscribeEnvelope = instance.channels(Arrays.asList("coolChannel")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

        assertEquals("1", subscribeEnvelope.getMetadata().getRegion());
        assertTrue(subscribeEnvelope.getMetadata().getTimetoken().equals(14607577960932487L));

        assertEquals(1, subscribeEnvelope.getMessages().size());
        SubscribeMessage subscribeMessage = subscribeEnvelope.getMessages().get(0);
        assertEquals("4", subscribeMessage.getShard());
        assertEquals("0", subscribeMessage.getFlags());
        assertEquals("coolChannel", subscribeMessage.getChannel());
        assertEquals("coolChan-bnel", subscribeMessage.getSubscriptionMatch());
        assertEquals("sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f", subscribeMessage.getSubscribeKey());
        assertEquals("Client-g5d4g", subscribeMessage.getIssuingClientId());
        assertEquals("{text=Enter Message Here}", subscribeMessage.getPayload().toString());
    }

    @Test
    public void subscribeChannelsSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel,coolChannel2/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        instance.channels(Arrays.asList("coolChannel", "coolChannel2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }

    @Test
    public void subscribeChannelsAuthSync() throws PubNubException {

        pubnub.getConfiguration().setAuthKey("authKey");

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel,coolChannel2/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        instance.channels(Arrays.asList("coolChannel", "coolChannel2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals("authKey", requests.get(0).queryParameter("auth").firstValue());
        assertEquals(1, requests.size());
    }

    @Test
    public void subscribeChannelsWithGroupSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/coolChannel,coolChannel2/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        instance.channels(Arrays.asList("coolChannel", "coolChannel2")).channelGroups(Arrays.asList("cg1")) .sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1", requests.get(0).queryParameter("channel-group").firstValue());
    }

    @Test
    public void subscribeGroupsSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        instance.channelGroups(Arrays.asList("cg1", "cg2")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1,cg2", requests.get(0).queryParameter("channel-group").firstValue());
    }

    @Test
    public void subscribeGroupSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        instance.channelGroups(Arrays.asList("cg1")).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1", requests.get(0).queryParameter("channel-group").firstValue());
    }

    @Test
    public void subscribeWithTimeTokenSync() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        instance.channelGroups(Arrays.asList("cg1")).timetoken(1337L).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1", requests.get(0).queryParameter("channel-group").firstValue());
        assertEquals("1337", requests.get(0).queryParameter("tt").firstValue());
    }

    @Test
    public void subscribeWithFilter() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        instance.channelGroups(Arrays.asList("cg1")).filterExpression("this=1&that=cool").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1", requests.get(0).queryParameter("channel-group").firstValue());
        assertEquals("this%3D1%26that%3Dcool", requests.get(0).queryParameter("filter-expr").firstValue());
    }

    @Test
    public void subscribeWithRegion() throws PubNubException {

        stubFor(get(urlPathEqualTo("/v2/subscribe/mySubscribeKey/,/0"))
                .willReturn(aResponse().withBody("{\"t\":{\"t\":\"14607577960932487\",\"r\":1},\"m\":[{\"a\":\"4\",\"f\":0,\"i\":\"Client-g5d4g\",\"p\":{\"t\":\"14607577960925503\",\"r\":1},\"k\":\"sub-c-4cec9f8e-01fa-11e6-8180-0619f8945a4f\",\"c\":\"coolChannel\",\"d\":{\"text\":\"Enter Message Here\"},\"b\":\"coolChan-bnel\"}]}")));

        instance.channelGroups(Arrays.asList("cg1")).region("10").sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1", requests.get(0).queryParameter("channel-group").firstValue());
        assertEquals("10", requests.get(0).queryParameter("tr").firstValue());
    }

}
