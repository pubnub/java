package com.pubnub.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class StateTest {
    Pubnub pubnub;
    String channel;
    String group;

    String random;

    @Before
    public void setUp() throws InterruptedException {
        random = UUID.randomUUID().toString().substring(0, 8);

        pubnub = new Pubnub("demo", "demo");
        pubnub.setCacheBusting(false);

        group = "jtest-" + random;
        channel = "ch1" + random;
    }

    @Test
    public void testStateForChannel()
            throws InterruptedException, PubnubException, JSONException {

        JSONObject state = new JSONObject();
        state.put("nickname", "jtester");
        state.put("status", "online");
        state.put("age", 32);

        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        final CountDownLatch latch3 = new CountDownLatch(1);

        final TestHelper.SimpleCallback cb1 = new TestHelper.SubscribeCallback(latch1);
        final TestHelper.SimpleCallback cb2 = new TestHelper.SimpleCallback(latch2);
        final TestHelper.SimpleCallback cb3 = new TestHelper.SimpleCallback(latch3);

        pubnub.setHeartbeat(6);
        pubnub.subscribe(channel, cb1);
        latch1.await(10, TimeUnit.SECONDS);

        pubnub.setState(channel, pubnub.getUUID(), state, cb2);
        latch2.await(10, TimeUnit.SECONDS);

        pubnub.getState(channel, pubnub.getUUID(), cb3);
        latch3.await(10, TimeUnit.SECONDS);

        JSONObject result = (JSONObject) cb3.getResponse();

        assertEquals(state.getString("nickname"), result.getString("nickname"));
        assertEquals(state.getString("status"), result.getString("status"));
        assertEquals(state.getInt("age"), result.getInt("age"));
    }

    @Test
    public void testStateForChannelGroup()
            throws InterruptedException, PubnubException, JSONException {

        JSONObject state = new JSONObject();
        state.put("nickname", "jtester");
        state.put("status", "online");
        state.put("age", 32);

        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch3 = new CountDownLatch(1);
        final CountDownLatch latch4 = new CountDownLatch(1);

        final TestHelper.SimpleCallback cb1 = new TestHelper.SimpleCallback(latch1);
        final TestHelper.SimpleCallback cb3 = new TestHelper.SimpleCallback(latch3);
        final TestHelper.SimpleCallback cb4 = new TestHelper.SimpleCallback(latch4);

        pubnub.setHeartbeat(6);

        pubnub.channelGroupAddChannel(group, channel, cb1);
        latch1.await(10, TimeUnit.SECONDS);

        pubnub.channelGroupSetState(group, pubnub.getUUID(), state, cb3);
        latch3.await(10, TimeUnit.SECONDS);

        pubnub.getState(channel, pubnub.getUUID(), cb4);
        latch4.await(10, TimeUnit.SECONDS);

        JSONObject result = (JSONObject) cb4.getResponse();

        assertEquals(state.getString("nickname"), result.getString("nickname"));
        assertEquals(state.getString("status"), result.getString("status"));
        assertEquals(state.getInt("age"), result.getInt("age"));
    }

    @Test
    public void testStateForChannelGroupUsingGroupGetter()
            throws InterruptedException, PubnubException, JSONException {

        JSONObject state = new JSONObject();
        state.put("nickname", "jtester");
        state.put("status", "online");
        state.put("age", 32);

        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        final CountDownLatch latch3 = new CountDownLatch(1);

        final TestHelper.SimpleCallback cb1 = new TestHelper.SimpleCallback(latch1);
        final TestHelper.SimpleCallback cb2 = new TestHelper.SimpleCallback(latch2);
        final TestHelper.SimpleCallback cb3 = new TestHelper.SimpleCallback(latch3);

        pubnub.setHeartbeat(6);

        pubnub.channelGroupAddChannel(group, channel, cb1);
        latch1.await(10, TimeUnit.SECONDS);

        pubnub.channelGroupSetState(group, pubnub.getUUID(), state, cb2);
        latch2.await(10, TimeUnit.SECONDS);

        pubnub.channelGroupGetState(group, pubnub.getUUID(), cb3);
        latch3.await(10, TimeUnit.SECONDS);

        JSONObject result = (JSONObject) cb3.getResponse();

        assertEquals(state.getString("nickname"), result.getString("nickname"));
        assertEquals(state.getString("status"), result.getString("status"));
        assertEquals(state.getInt("age"), result.getInt("age"));
    }
}
