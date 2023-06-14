package com.pubnub.api.endpoints.presence;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.TestHarness;
import com.pubnub.api.models.consumer.presence.PNSetStateResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;


public class SetStateEndpointTest extends TestHarness {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(this.PORT), false);

    private SetState partialSetState;
    private PubNub pubnub;

    @Before
    public void beforeEach() throws IOException, PubNubException {
        pubnub = this.createPubNubInstance();
        partialSetState = pubnub.setPresenceState();
        wireMockRule.start();
    }

    @After
    public void afterEach() {
        pubnub.destroy();
        pubnub = null;
        wireMockRule.stop();
    }

    @Test
    public void applyStateForChannelSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        String subKey = "mySubscribeKey";
        String channel = "testChannel";

        String urlRegexForHeartbeat = "/v2/presence/sub-key/" + subKey + "/channel/" + channel + "/heartbeat?.*";
        stubFor(get(urlPathMatching(urlRegexForHeartbeat))
                .willReturn(aResponse().withStatus(200).withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}"))); //we don't expose this response to client


        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result =
                partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();

        assert result != null;

        assertEquals(20, pubnub.getMapper().elementToInt(result.getState(), "age"));
        assertEquals("online", pubnub.getMapper().elementToString(result.getState(), "status"));

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        List<LoggedRequest> heartbeatRequests = findAll(getRequestedFor(urlMatching(urlRegexForHeartbeat)));
        assertEquals(0, heartbeatRequests.size());
    }

    @Test
    public void applyStateForSomebodyElseChannelSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/someoneElseUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result =
                partialSetState.channels(Collections.singletonList("testChannel")).state(myState).uuid(
                        "someoneElseUUID").sync();

        assert result != null;

        assertEquals(20, pubnub.getMapper().elementToInt(result.getState(), "age"));
        assertEquals("online", pubnub.getMapper().elementToString(result.getState(), "status"));

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }

    @Test
    public void applyStateForChannelsSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel," +
                "testChannel2/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result =
                partialSetState.channels(Arrays.asList("testChannel", "testChannel2")).state(myState).sync();

        assert result != null;

        assertEquals(20, pubnub.getMapper().elementToInt(result.getState(), "age"));
        assertEquals("online", pubnub.getMapper().elementToString(result.getState(), "status"));
        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
    }

    @Test
    public void applyStateForChannelGroupSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result = partialSetState.channelGroups(Collections.singletonList("cg1")).state(myState).sync();

        assert result != null;

        assertEquals(20, pubnub.getMapper().elementToInt(result.getState(), "age"));
        assertEquals("online", pubnub.getMapper().elementToString(result.getState(), "status"));

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

    }

    @Test
    public void applyStateForChannelGroupsSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/,/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result = partialSetState.channelGroups(Arrays.asList("cg1", "cg2")).state(myState).sync();

        assert result != null;

        assertEquals(20, pubnub.getMapper().elementToInt(result.getState(), "age"));
        assertEquals("online", pubnub.getMapper().elementToString(result.getState(), "status"));

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("cg1,cg2", requests.get(0).queryParameter("channel-group").firstValue());

    }

    @Test
    public void applyStateForMixSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        PNSetStateResult result =
                partialSetState.channels(Collections.singletonList("ch1")).channelGroups(Arrays.asList("cg1", "cg2")).state(myState).sync();

        assert result != null;

        assertEquals(20, pubnub.getMapper().elementToInt(result.getState(), "age"));
        assertEquals("online", pubnub.getMapper().elementToString(result.getState(), "status"));

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());

    }

    @Test(expected = PubNubException.class)
    public void applyNon200Sync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/ch1/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22status%22%3A%22oneline%22%2C%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}").withStatus(400)));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        partialSetState.channels(Collections.singletonList("ch1")).channelGroups(Arrays.asList("cg1", "cg2")).state(myState).sync();
    }

    @Test(expected = PubNubException.class)
    public void missingStateSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        partialSetState.channels(Collections.singletonList("testChannel")).sync();
    }

    @Test
    public void testIsAuthRequiredSuccessSync() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                //.withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .withQueryParam("state", equalToJson("{\"age\":20}"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        pubnub.getConfiguration().setAuthKey("myKey");
        partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @Test(expected = PubNubException.class)
    public void testNullSubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        pubnub.getConfiguration().setSubscribeKey(null);
        partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();
    }

    @Test(expected = PubNubException.class)
    public void testEmptySubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        pubnub.getConfiguration().setSubscribeKey("");
        partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();
    }

    @Test(expected = PubNubException.class)
    public void testChannelAndGroupMissingSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"payload\": { \"age\" : " +
                        "20, \"status\" : \"online\" }, \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        partialSetState.state(myState).sync();
    }

    @Test(expected = PubNubException.class)
    public void testNullPayloadSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/channel/testChannel/uuid/myUUID/data"))
                .withQueryParam("uuid", matching("myUUID"))
                .withQueryParam("pnsdk", matching("PubNub-Java-Unified/suchJava"))
                .withQueryParam("state", matching("%7B%22age%22%3A20%7D"))
                .willReturn(aResponse().withBody("{ \"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);

        partialSetState.channels(Collections.singletonList("testChannel")).state(myState).sync();
    }

    @Test
    public void when_calling_setState_withHeartbeat_flag_should_call_heartbeat_and_not_setState_REST_API() throws PubNubException {
        boolean withHeartbeat = true;
        String subKey = "mySubscribeKey";
        String channel = "testChannel";
        String ageKey = "age";
        String ageValue = "20";
        String bikeKey = "bike";
        String bikeValue = "Gravel";

        String urlRegexForHeartbeat = "/v2/presence/sub-key/" + subKey + "/channel/" + channel + "/heartbeat?.*";
        stubFor(get(urlPathMatching(urlRegexForHeartbeat))
                .willReturn(aResponse().withStatus(200).withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}"))); //we don't expose this response to client

        String urlRegexForSetState = "/v2/presence/sub-key/" + subKey + "/channel/" + channel + "/uuid/.*/data.*";
        stubFor(get(urlPathMatching(urlRegexForSetState)));


        Map<String, Object> myState = new HashMap<>();
        myState.put(ageKey, 20);
        myState.put(bikeKey, bikeValue);

        PNSetStateResult result = partialSetState.channels(Collections.singletonList("testChannel")).state(myState).withHeartbeat(withHeartbeat).sync();

        assertEquals(pubnub.getMapper().elementToInt(result.getState(), ageKey), Integer.parseInt(ageValue));
        assertEquals(pubnub.getMapper().elementToString(result.getState(), bikeKey), bikeValue);

        List<LoggedRequest> heartbeatRequests = findAll(getRequestedFor(urlMatching(urlRegexForHeartbeat)));
        assertEquals(1, heartbeatRequests.size());

        List<LoggedRequest> setStateRequests = findAll(getRequestedFor(urlMatching(urlRegexForSetState)));
        assertEquals(0, setStateRequests.size());
    }

    @Test
    public void when_calling_setState_withHeartbeat_flag_and_state_is_not_JsonObject_should_throw_exception() throws PubNubException {
        //given
        boolean withHeartbeat = true;
        String myStateIsNotJson = "new state";

        //when
        PubNubException exception = assertThrows(
                PubNubException.class,
                () -> partialSetState.channels(Collections.singletonList("testChannel")).state(myStateIsNotJson).withHeartbeat(withHeartbeat).sync()
        );

        //then
        assertEquals("State must be a JSON object.", exception.getPubnubError().getMessage());
    }

    @Test
    public void when_calling_setState_withHeartbeat_flag_for_two_channels_should_call_heartbeat_and_not_setState_REST_API() throws PubNubException, UnsupportedEncodingException {
        //given
        boolean withHeartbeat = true;
        String channel1 = "Channel1";
        String channel2 = "Channel2";
        String subKey = "mySubscribeKey";
        String ageKey = "age";
        String ageValue = "20";
        String bikeKey = "bike";
        String bikeValue = "Gravel";

        String urlRegexForHeartbeat = "/v2/presence/sub-key/" + subKey + "/channel/" + channel1 + "," + channel2 + "/heartbeat?.*";
        stubFor(get(urlPathMatching(urlRegexForHeartbeat))
                .willReturn(aResponse().withStatus(200).withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}"))); //we don't expose this response to client

        String urlRegexForSetState = "/v2/presence/sub-key/" + subKey + "/channel/" + channel1 + "," + channel2 + "/uuid/.*/data.*";
        stubFor(get(urlPathMatching(urlRegexForSetState)));

        Map<String, Object> myState = new HashMap<>();
        myState.put(ageKey, ageValue);
        myState.put(bikeKey, bikeValue);

        //when
        PNSetStateResult result = partialSetState.channels(Arrays.asList(channel1, channel2)).state(myState).withHeartbeat(withHeartbeat).sync();

        //then
        assertEquals(pubnub.getMapper().elementToInt(result.getState(), ageKey), Integer.parseInt(ageValue));
        assertEquals(pubnub.getMapper().elementToString(result.getState(), bikeKey), bikeValue);

        List<LoggedRequest> heartbeatRequests = findAll(getRequestedFor(urlMatching(urlRegexForHeartbeat)));
        assertEquals(1, heartbeatRequests.size());
        String setStateWithHeartbeatUrl = heartbeatRequests.get(0).getUrl();
        String stateValueInUrl = extractStateParameter(setStateWithHeartbeatUrl);
        JsonObject jsonObject = JsonParser.parseString(stateValueInUrl).getAsJsonObject();
        String myStateAsJson = pubnub.getMapper().toJson(myState);
        assertEquals(myStateAsJson, jsonObject.get(channel1).toString());
        assertEquals(myStateAsJson, jsonObject.get(channel2).toString());

        List<LoggedRequest> setStateRequests = findAll(getRequestedFor(urlMatching(urlRegexForSetState)));
        assertEquals(0, setStateRequests.size());
    }

    @Test
    public void when_calling_setState_withHeartbeat_flag_for_two_channels_and_two_channelGroup_should_call_heartbeat_and_not_setState_REST_API() throws PubNubException, UnsupportedEncodingException {
        //given
        boolean withHeartbeat = true;
        String channel1 = "Channel1";
        String channel2 = "Channel2";
        String channelGroup1 = "ChannelGroup1";
        String channelGroup2 = "ChannelGroup2";
        String subKey = "mySubscribeKey";
        String ageKey = "age";
        String ageValue = "20";
        String bikeKey = "bike";
        String bikeValue = "Gravel";

        String urlRegexForHeartbeat = "/v2/presence/sub-key/" + subKey + "/channel/" + channel1 + "," + channel2 + "/heartbeat?.*";
        stubFor(get(urlPathMatching(urlRegexForHeartbeat))
                .willReturn(aResponse().withStatus(200).withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}"))); //we don't expose this response to client

        String urlRegexForSetState = "/v2/presence/sub-key/" + subKey + "/channel/" + channel1 + "," + channel2 + "/uuid/.*/data.*";
        stubFor(get(urlPathMatching(urlRegexForSetState)));

        Map<String, Object> myState = new HashMap<>();
        myState.put(ageKey, ageValue);
        myState.put(bikeKey, bikeValue);

        //when
        PNSetStateResult result = partialSetState
                .channels(Arrays.asList(channel1, channel2))
                .channelGroups(Arrays.asList(channelGroup1, channelGroup2))
                .state(myState)
                .withHeartbeat(withHeartbeat)
                .sync();

        //then
        assertEquals(pubnub.getMapper().elementToInt(result.getState(), ageKey), Integer.parseInt(ageValue));
        assertEquals(pubnub.getMapper().elementToString(result.getState(), bikeKey), bikeValue);

        List<LoggedRequest> heartbeatRequests = findAll(getRequestedFor(urlMatching(urlRegexForHeartbeat)));
        assertEquals(1, heartbeatRequests.size());

        String setStateWithHeartbeatUrl = heartbeatRequests.get(0).getUrl();
        String stateValueInUrl = extractStateParameter(setStateWithHeartbeatUrl);
        JsonObject jsonObject = JsonParser.parseString(stateValueInUrl).getAsJsonObject();
        String myStateAsJson = pubnub.getMapper().toJson(myState);
        assertEquals(myStateAsJson, jsonObject.get(channel1).toString());
        assertEquals(myStateAsJson, jsonObject.get(channel2).toString());
        assertEquals(myStateAsJson, jsonObject.get(channelGroup1).toString());
        assertEquals(myStateAsJson, jsonObject.get(channelGroup2).toString());

        List<LoggedRequest> setStateRequests = findAll(getRequestedFor(urlMatching(urlRegexForSetState)));
        assertEquals(0, setStateRequests.size());
    }

    @Test
    public void should_throw_exception_when_calling_setState_withHeartbeat_for_userId_different_from_userId_in_configuration() {
        //given
        boolean withHeartbeat = true;
        String userIdValue = "different from pubnub.getConfiguration().getUserId().getValue()";
        Map<String, Object> myState = new HashMap<>();
        myState.put("age", 20);


        //when
        PubNubException exception = assertThrows(
                PubNubException.class,
                () -> partialSetState
                        .channels(Collections.singletonList("testChannel"))
                        .state(myState)
                        .uuid(userIdValue)
                        .withHeartbeat(withHeartbeat)
                        .sync()
        );

        //then
        assertEquals("UserId can't be different from UserId in configuration when flag withHeartbeat is set to true.", exception.getPubnubError().getMessage());
    }

    private String extractStateParameter(String url) throws UnsupportedEncodingException {
        String stateParam = "state=";
        int startIndex = url.indexOf(stateParam);
        int endIndex = url.indexOf("&", startIndex);
        String encodedState = url.substring(startIndex + stateParam.length(), endIndex);
        return URLDecoder.decode(encodedState, "UTF-8");
    }
}
