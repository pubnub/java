package com.pubnub.api.workers;

import com.google.gson.*;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.managers.DuplicationManager;
import com.pubnub.api.models.consumer.pubsub.PNEvent;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.server.SubscribeMessage;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(Parameterized.class)
public class SubscribeMessageProcessorTest {

    private final JsonElement messageJson;

    public SubscribeMessageProcessorTest(final JsonElement messageJson) {
        this.messageJson = messageJson;
    }

    @Parameterized.Parameters
    public static Iterable<Object> data() {
        JsonObject simpleObject = new JsonObject();
        simpleObject.add("test", new JsonPrimitive("value"));

        JsonArray array = new JsonArray();
        array.add(new JsonPrimitive("array"));
        array.add(new JsonPrimitive("of"));
        array.add(new JsonPrimitive("elements"));

        JsonObject object = new JsonObject();
        object.add("with", array.deepCopy());

        return Arrays.asList(
                new JsonPrimitive("thisIsMessage"),
                new JsonPrimitive(true),
                new JsonPrimitive(1337),
                JsonNull.INSTANCE,
                simpleObject,
                array,
                object
        );
    }


    @Test
    public void testDifferentJsonMessages() throws PubNubException {
        Gson gson = new Gson();
        PNConfiguration configuration = new PNConfiguration(new UserId("test"));
        PubNub pubnub = new PubNub(configuration);

        SubscribeMessageProcessor subscribeMessageProcessor = new SubscribeMessageProcessor(
                pubnub,
                new DuplicationManager(configuration)
        );

        PNEvent result = subscribeMessageProcessor.processIncomingPayload(gson.fromJson(fileMessage(messageJson.toString()), SubscribeMessage.class));


        assertThat(result, is(instanceOf(PNFileEventResult.class)));
        assertThat(((PNFileEventResult) result).getJsonMessage(), is(messageJson));
    }

    private String fileMessage(String messageJson) {
        return "{\"a\":\"0\",\"f\":0,\"e\":4,\"i\":\"client-52774e6f-2f4e-4915-aefd-e8bb75cd2e7d\",\"p\":{\"t\":\"16632349939765880\",\"r\":43},\"k\":\"sub-c-4b1dbfef-2fa9-495f-a316-2b634063083d\",\"c\":\"ch_1663234993171_F4FC4F460F\",\"u\":\"This is meta\",\"d\":{\"message\":" + messageJson + ",\"file\":{\"id\":\"30ce0095-3c50-4cdc-a626-bf402d233731\",\"name\":\"fileNamech_1663234993171_F4FC4F460F.txt\"}}}";
    }

}
