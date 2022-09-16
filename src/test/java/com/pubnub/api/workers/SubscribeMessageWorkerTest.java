package com.pubnub.api.workers;

import com.google.gson.Gson;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.PubNubUtil;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.managers.DuplicationManager;
import com.pubnub.api.managers.ListenerManager;
import com.pubnub.api.models.consumer.pubsub.files.PNFileEventResult;
import com.pubnub.api.models.server.SubscribeEnvelope;
import com.pubnub.api.models.server.SubscribeMessage;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class SubscribeMessageWorkerTest {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final SubscribeMessage subscribeMessage = subscribeMessage();
    private final String authKey = "ak";

    @Test
    public void fileEventUrlContainsAuthQueryParamWhenAuthIsSet() throws InterruptedException, PubNubException {
        //given
        PNConfiguration config = configWithAuth(config());
        PubNub pubnub = new PubNub(config);
        LinkedBlockingQueue<SubscribeMessage> queue = new LinkedBlockingQueue<>();
        ListenerManager listenerManager = new ListenerManager(pubnub);
        SubscribeMessageWorker subscribeMessageWorker = subscribeMessageWorker(pubnub,
                listenerManager,
                queue
        );
        AtomicReference<PNFileEventResult> fileEventResult = new AtomicReference<>();
        CountDownLatch receivedLatch = new CountDownLatch(1);
        listenerManager.addListener(capturingFileEventListener(fileEventResult, receivedLatch));


        //when
        executor.execute(subscribeMessageWorker);
        queue.offer(subscribeMessage);

        //then
        if (!receivedLatch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Message was not received");
        }
        Map<String, String> queryParams = queryParams(fileEventResult.get().getFile().getUrl());
        Assert.assertEquals(setOf(PubNubUtil.AUTH_QUERY_PARAM_NAME), queryParams.keySet());
    }

    @Test
    public void fileEventUrlContainsNoQueryParamsWhenNoSecretNorAuth() throws InterruptedException, PubNubException {
        //given
        PNConfiguration config = config();
        PubNub pubnub = new PubNub(config);
        LinkedBlockingQueue<SubscribeMessage> queue = new LinkedBlockingQueue<>();
        ListenerManager listenerManager = new ListenerManager(pubnub);
        SubscribeMessageWorker subscribeMessageWorker = subscribeMessageWorker(pubnub,
                listenerManager,
                queue
        );
        AtomicReference<PNFileEventResult> fileEventResult = new AtomicReference<>();
        CountDownLatch receivedLatch = new CountDownLatch(1);
        listenerManager.addListener(capturingFileEventListener(fileEventResult, receivedLatch));


        //when
        executor.execute(subscribeMessageWorker);
        queue.offer(subscribeMessage);

        //then
        if (!receivedLatch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Message was not received");
        }
        Map<String, String> queryParams = queryParams(fileEventResult.get().getFile().getUrl());
        Assert.assertEquals(Collections.emptyMap(), queryParams);
    }

    @Test
    public void fileEventUrlContainsSignatureQueryParamWhenSecretIsSet() throws InterruptedException, PubNubException {
        //given
        PNConfiguration config = configWithSecret(config());
        PubNub pubnub = new PubNub(config);
        LinkedBlockingQueue<SubscribeMessage> queue = new LinkedBlockingQueue<>();
        ListenerManager listenerManager = new ListenerManager(pubnub);
        SubscribeMessageWorker subscribeMessageWorker = subscribeMessageWorker(pubnub,
                listenerManager,
                queue
        );
        AtomicReference<PNFileEventResult> fileEventResult = new AtomicReference<>();
        CountDownLatch receivedLatch = new CountDownLatch(1);
        listenerManager.addListener(capturingFileEventListener(fileEventResult, receivedLatch));

        //when
        executor.execute(subscribeMessageWorker);
        queue.offer(subscribeMessage);

        //then
        if (!receivedLatch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Message was not received");
        }
        Map<String, String> queryParams = queryParams(fileEventResult.get().getFile().getUrl());
        Assert.assertEquals(setOf(PubNubUtil.SIGNATURE_QUERY_PARAM_NAME, PubNubUtil.TIMESTAMP_QUERY_PARAM_NAME), queryParams.keySet());
    }

    @Test
    public void fileEventUrlContainsSignatureAndAuthQueryParamsWhenAuthAndSecretAreSet() throws InterruptedException, PubNubException {
        //given
        PNConfiguration config = configWithAuth(configWithSecret(config()));
        PubNub pubnub = new PubNub(config);
        LinkedBlockingQueue<SubscribeMessage> queue = new LinkedBlockingQueue<>();
        ListenerManager listenerManager = new ListenerManager(pubnub);
        SubscribeMessageWorker subscribeMessageWorker = subscribeMessageWorker(pubnub,
                listenerManager,
                queue
        );
        AtomicReference<PNFileEventResult> fileEventResult = new AtomicReference<>();
        CountDownLatch receivedLatch = new CountDownLatch(1);
        listenerManager.addListener(capturingFileEventListener(fileEventResult, receivedLatch));

        //when
        executor.execute(subscribeMessageWorker);
        queue.offer(subscribeMessage);

        //then
        if (!receivedLatch.await(5, TimeUnit.SECONDS)) {
            Assert.fail("Message was not received");
        }
        Map<String, String> queryParams = queryParams(fileEventResult.get().getFile().getUrl());
        System.out.println(fileEventResult.get().getFile().getUrl());
        Assert.assertEquals(setOf(PubNubUtil.SIGNATURE_QUERY_PARAM_NAME,
                PubNubUtil.TIMESTAMP_QUERY_PARAM_NAME,
                PubNubUtil.AUTH_QUERY_PARAM_NAME), queryParams.keySet());
    }

    private Set<String> setOf(String... values) {
        return new HashSet<>(Arrays.asList(values.clone()));
    }

    private SubscribeCallback.BaseSubscribeCallback capturingFileEventListener(AtomicReference<PNFileEventResult> fileEventResult,
                                                                               CountDownLatch receivedLatch) {
        return new SubscribeCallback.BaseSubscribeCallback() {
            @Override
            public void file(@NotNull PubNub pubnub, @NotNull PNFileEventResult pnFileEventResult) {
                fileEventResult.set(pnFileEventResult);
                receivedLatch.countDown();
            }
        };
    }

    private SubscribeMessageWorker subscribeMessageWorker(PubNub pubnub,
                                                          ListenerManager listenerManager,
                                                          LinkedBlockingQueue<SubscribeMessage> queue) {
        return new SubscribeMessageWorker(
                listenerManager,
                queue,
                new SubscribeMessageProcessor(pubnub, new DuplicationManager(pubnub.getConfiguration()))
        );
    }

    private PNConfiguration config() throws PubNubException {
        PNConfiguration config = new PNConfiguration(PubNub.generateUUID());
        config.setPublishKey("pk");
        config.setSubscribeKey("ck");
        return config;
    }

    private PNConfiguration configWithAuth(PNConfiguration config) {
        config.setAuthKey(authKey);
        return config;
    }

    private PNConfiguration configWithSecret(PNConfiguration config) {
        config.setSecretKey("sk");
        return config;
    }

    private SubscribeMessage subscribeMessage() {
        Gson gson = new Gson();
        Scanner s = new Scanner(SubscribeMessageWorkerTest.class.getResourceAsStream("/fileEvent.json")).useDelimiter(
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