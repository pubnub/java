package com.pubnub.api.workers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.UserId;
import com.pubnub.api.managers.DuplicationManager;
import com.pubnub.api.models.consumer.pubsub.PNEvent;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNSignalResult;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.server.SubscribeEnvelope;
import com.pubnub.api.models.server.SubscribeMessage;
import okhttp3.HttpUrl;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class SubscribeMessageProcessorTest {

    private final SubscribeMessage subscribeMessage = subscribeMessage();

    @Test
    public void fileEventUrlContainsAuthQueryParamWhenAuthIsSet() throws PubNubException {
        //given
        SubscribeMessageProcessor subscribeMessageProcessor = subscribeMessageProcessor(configWithAuth(config()));

        //when
        PNEvent result = subscribeMessageProcessor.processIncomingPayload(subscribeMessage);

        //then
        assertThat(result, is(instanceOf(PNFileEventResult.class)));
        Map<String, String> queryParams = queryParams(((PNFileEventResult) result).getFile().getUrl());
        Assert.assertEquals(setOf(PubNubUtil.AUTH_QUERY_PARAM_NAME), queryParams.keySet());
    }

    @Test
    public void fileEventUrlContainsNoQueryParamsWhenNoSecretNorAuth() throws PubNubException {
        //given
        SubscribeMessageProcessor subscribeMessageProcessor = subscribeMessageProcessor(config());

        //when
        PNEvent result = subscribeMessageProcessor.processIncomingPayload(subscribeMessage);

        //then
        assertThat(result, is(instanceOf(PNFileEventResult.class)));
        Map<String, String> queryParams = queryParams(((PNFileEventResult) result).getFile().getUrl());
        Assert.assertEquals(Collections.emptyMap(), queryParams);
    }

    @Test
    public void fileEventUrlContainsSignatureQueryParamWhenSecretIsSet() throws PubNubException {
        //given
        SubscribeMessageProcessor subscribeMessageProcessor = subscribeMessageProcessor(configWithSecret(config()));

        //when
        PNEvent result = subscribeMessageProcessor.processIncomingPayload(subscribeMessage);

        //then
        assertThat(result, is(instanceOf(PNFileEventResult.class)));
        Map<String, String> queryParams = queryParams(((PNFileEventResult) result).getFile().getUrl());
        Assert.assertEquals(setOf(PubNubUtil.SIGNATURE_QUERY_PARAM_NAME, PubNubUtil.TIMESTAMP_QUERY_PARAM_NAME), queryParams.keySet());
    }

    @Test
    public void fileEventUrlContainsSignatureAndAuthQueryParamsWhenAuthAndSecretAreSet() throws InterruptedException, PubNubException {
        //given
        SubscribeMessageProcessor subscribeMessageProcessor = subscribeMessageProcessor(configWithAuth(configWithSecret(config())));

        //when
        PNEvent result = subscribeMessageProcessor.processIncomingPayload(subscribeMessage);

        //then
        assertThat(result, is(instanceOf(PNFileEventResult.class)));
        Map<String, String> queryParams = queryParams(((PNFileEventResult) result).getFile().getUrl());
        Assert.assertEquals(setOf(PubNubUtil.SIGNATURE_QUERY_PARAM_NAME,
                PubNubUtil.TIMESTAMP_QUERY_PARAM_NAME,
                PubNubUtil.AUTH_QUERY_PARAM_NAME), queryParams.keySet());
    }

    @Test
    public void testJsonMessageHandleJsonString() throws PubNubException {
        testDifferentJsonMessages(new JsonPrimitive("thisIsMessage"));
    }

    @Test
    public void testJsonMessageHandleJsonBoolean() throws PubNubException {
        testDifferentJsonMessages(new JsonPrimitive(true));
    }

    @Test
    public void testJsonMessageHandleJsonNumber() throws PubNubException {
        testDifferentJsonMessages(new JsonPrimitive(1337));
    }

    @Test
    public void testJsonMessageHandleJsonNull() throws PubNubException {
        testDifferentJsonMessages(JsonNull.INSTANCE);
    }

    @Test
    public void testJsonMessageHandleSimpleJsonObject() throws PubNubException {
        JsonObject simpleObject = new JsonObject();
        simpleObject.add("test", new JsonPrimitive("value"));
        testDifferentJsonMessages(simpleObject);
    }

    @Test
    public void testJsonMessageHandleJsonArray() throws PubNubException {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive("array"));
        array.add(new JsonPrimitive("of"));
        array.add(new JsonPrimitive("elements"));
        testDifferentJsonMessages(array);
    }

    @Test
    public void testJsonMessageHandleMoreComplexJson() throws PubNubException {
        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive("array"));
        array.add(new JsonPrimitive("of"));
        array.add(new JsonPrimitive("elements"));

        JsonObject object = new JsonObject();
        object.add("with", array);
        testDifferentJsonMessages(object);
    }

    @Test
    public void pnMessageResult_should_contain_messageType_and_spaceId_when_they_are_provided_in_subscribeMessage() throws PubNubException {
        //given
        String message = "message content something interesting";
        String messageType = "myMessageType";
        String spaceId = "mySpace";
        String subscribeResponse = getSubscribeResponseWithMessageTypeAndSpaceId(message, messageType, spaceId);

        Gson gson = new Gson();
        SubscribeMessageProcessor subscribeMessageProcessor = subscribeMessageProcessor(config());
        SubscribeEnvelope subscribeEnvelope = gson.fromJson(subscribeResponse, SubscribeEnvelope.class);

        //when
        PNEvent result = subscribeMessageProcessor.processIncomingPayload(subscribeEnvelope.getMessages().get(0));

        //then
        PNMessageResult messageResult = (PNMessageResult) result;

        assertThat(result, is(instanceOf(PNMessageResult.class)));
        assertThat(messageResult.getMessage().getAsString(), is(message));
        assertThat(messageResult.getMessageType().getValue(), equalTo(messageType));
        assertThat(messageResult.getSpaceId().getValue(), equalTo(spaceId));
    }


    @Test
    public void pnSignalResult_should_contain_messageType_and_spaceId_when_they_are_provided_in_subscribeMessage() throws PubNubException {
        //given
        String message = "signal content something interesting";
        String messageType = "myMessageType";
        String spaceId = "mySpace";
        String subscribeResponseJson = getSubscribeResponseForSignalWithMessageTypeAndSpaceId(message, messageType, spaceId);

        Gson gson = new Gson();
        SubscribeMessageProcessor subscribeMessageProcessor = subscribeMessageProcessor(config());
        SubscribeEnvelope subscribeEnvelope = gson.fromJson(subscribeResponseJson, SubscribeEnvelope.class);

        //when
        PNEvent result = subscribeMessageProcessor.processIncomingPayload(subscribeEnvelope.getMessages().get(0));

        //then
        PNSignalResult signalResult = (PNSignalResult) result;

        assertThat(result, is(instanceOf(PNSignalResult.class)));
        assertThat(signalResult.getMessage().getAsString(), is(message));
        assertThat(signalResult.getMessageType().getValue(), equalTo(messageType));
        assertThat(signalResult.getSpaceId().getValue(), equalTo(spaceId));


    }

    private String getSubscribeResponseForSignalWithMessageTypeAndSpaceId(String message, String messageType, String spaceId) {
        return "{\n" +
                "  \"t\": {\n" +
                "    \"t\": \"16734390751423544\",\n" +
                "    \"r\": 41\n" +
                "  },\n" +
                "  \"m\": [\n" +
                "    {\n" +
                "      \"a\": \"1\",\n" +
                "      \"f\": 0,\n" +
                "      \"e\": 1,\n" +
                "      \"i\": \"client-a9e92988-4909-421a-a66e-96c778bc9d86\",\n" +
                "      \"p\": {\n" +
                "        \"t\": \"16734390751423544\",\n" +
                "        \"r\": 41\n" +
                "      },\n" +
                "      \"k\": \"sub-c-cb8b98b4-cd27-11ec-b360-1a35c262c233\",\n" +
                "      \"c\": \"ch_mqwvjygftw\",\n" +
                "     \"d\":  \"" + message + "\",\n" +
                "      \"mt\": \"" + messageType + "\",\n" +
                "      \"si\": \"" + spaceId + "\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private void testDifferentJsonMessages(JsonElement jsonMessage) throws PubNubException {
        //given
        Gson gson = new Gson();
        SubscribeMessageProcessor subscribeMessageProcessor = subscribeMessageProcessor(config());

        //when
        PNEvent result = subscribeMessageProcessor.processIncomingPayload(gson.fromJson(fileMessage(jsonMessage.toString()), SubscribeMessage.class));

        //then
        assertThat(result, is(instanceOf(PNFileEventResult.class)));
        assertThat(((PNFileEventResult) result).getJsonMessage(), is(jsonMessage));
    }

    private String fileMessage(String messageJson) {
        return "{\"a\":\"0\",\"f\":0,\"e\":4,\"i\":\"client-52774e6f-2f4e-4915-aefd-e8bb75cd2e7d\",\"p\":{\"t\":\"16632349939765880\",\"r\":43},\"k\":\"sub-c-4b1dbfef-2fa9-495f-a316-2b634063083d\",\"c\":\"ch_1663234993171_F4FC4F460F\",\"u\":\"This is meta\",\"d\":{\"message\":" + messageJson + ",\"file\":{\"id\":\"30ce0095-3c50-4cdc-a626-bf402d233731\",\"name\":\"fileNamech_1663234993171_F4FC4F460F.txt\"}}}";
    }

    private Set<String> setOf(String... values) {
        return new HashSet<>(Arrays.asList(values.clone()));
    }


    private SubscribeMessageProcessor subscribeMessageProcessor(PNConfiguration conf) throws PubNubException {
        return new SubscribeMessageProcessor(new PubNub(conf), new DuplicationManager(conf));
    }

    private PNConfiguration config() throws PubNubException {
        PNConfiguration config = new PNConfiguration(new UserId("pn-" + UUID.randomUUID()));
        config.setPublishKey("pk");
        config.setSubscribeKey("ck");
        return config;
    }

    private PNConfiguration configWithAuth(PNConfiguration config) {
        String authKey = "ak";
        config.setAuthKey(authKey);
        return config;
    }

    private PNConfiguration configWithSecret(PNConfiguration config) {
        config.setSecretKey("sk");
        return config;
    }

    private SubscribeMessage subscribeMessage() {
        Gson gson = new Gson();
        Scanner s = new Scanner(SubscribeMessageProcessorTest.class.getResourceAsStream("/fileEvent.json")).useDelimiter(
                "\\A");
        String result = s.hasNext() ? s.next() : "";
        SubscribeEnvelope envelope = gson.fromJson(result, SubscribeEnvelope.class);
        Assert.assertEquals(1, envelope.getMessages().size());
        return envelope.getMessages().get(0);
    }

    private Map<String, String> queryParams(String urlString) {
        Map<String, String> queryParameters = new HashMap<>();
        HttpUrl httpUrl = HttpUrl.get(urlString);
        for (String paramName : httpUrl.queryParameterNames()) {
            queryParameters.put(paramName, httpUrl.queryParameter(paramName));
        }
        return queryParameters;
    }

    private String getSubscribeResponseWithMessageTypeAndSpaceId(String message, String messageType, String spaceId) {
        String subscribeResponse = "{\n" +
                "    \"t\": {\n" +
                "        \"t\": \"16710463904083117\",\n" +
                "        \"r\": 21\n" +
                "    },\n" +
                "    \"m\": [\n" +
                "        {\n" +
                "            \"a\": \"4\",\n" +
                "            \"f\": 0,\n" +
                "            \"p\": {\n" +
                "                \"t\": \"16710463855524468\",\n" +
                "                \"r\": 21\n" +
                "            },\n" +
                "            \"k\": \"demo\",\n" +
                "            \"c\": \"testChannel\",\n" +
                "            \"d\":  \"" + message + "\",\n" +
                "            \"mt\": \"" + messageType + "\",\n" +
                "            \"si\": \"" + spaceId + "\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";

        return subscribeResponse;
    }

}
