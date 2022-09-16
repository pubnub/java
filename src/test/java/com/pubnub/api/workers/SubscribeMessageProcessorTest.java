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
import com.pubnub.api.managers.DuplicationManager;
import com.pubnub.api.models.consumer.pubsub.PNEvent;
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

import static org.hamcrest.MatcherAssert.assertThat;
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
        PNConfiguration config = new PNConfiguration(PubNub.generateUUID());
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
}
