package com.pubnub.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class PubnubTest {

    class SubscribeCallback extends Callback {

        private CountDownLatch latch;

        private Object response;

        public SubscribeCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        public SubscribeCallback() {

        }

        public Object getResponse() {
            return response;
        }

        @Override
        public void successCallback(String channel, Object message) {
            response = message;
            if (latch != null)
                latch.countDown();
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            response = error;
            if (latch != null)
                latch.countDown();
        }
    }

    class PublishCallback extends Callback {

        private CountDownLatch latch;
        private int result = 0;

        public int getResult() {
            return result;
        }

        public PublishCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        public PublishCallback() {

        }

        public void successCallback(String channel, Object message) {
            JSONArray jsarr;
            try {
                jsarr = (JSONArray) message;
                result = (Integer) jsarr.get(0);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (latch != null)
                latch.countDown();
        }

        public void errorCallback(String channel, PubnubError error) {
            JSONArray jsarr;
            result = 0;
            if (latch != null)
                latch.countDown();
        }

    }

    class UlsCallback extends Callback {

        private CountDownLatch latch;
        private JSONObject result = null;

        public JSONObject getResult() {
            return result;
        }

        public UlsCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        public UlsCallback() {

        }

        public void successCallback(String channel, Object message) {

            result = (JSONObject) message;

            if (latch != null)
                latch.countDown();
        }

        public void errorCallback(String channel, PubnubError error) {
            result = null;
            if (latch != null)
                latch.countDown();
        }

    }

    class HereNowCallback extends Callback {

        private int occupancy;
        private String[] uuids;
        private CountDownLatch latch;

        public int getOccupancy() {
            return occupancy;
        }

        public HereNowCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        public HereNowCallback() {

        }

        @Override
        public void successCallback(String channel, Object message) {
            JSONObject resp = null;
            try {
                resp = new JSONObject(message.toString());
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if (resp != null) {
                try {
                    occupancy = (Integer) resp.get("occupancy");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (latch != null)
                latch.countDown();
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            if (latch != null)
                latch.countDown();
        }

    }

    class HistoryCallback extends Callback {

        private int count;
        private String[] uuids;
        private CountDownLatch latch;

        public int getCount() {
            return count;
        }

        public HistoryCallback(CountDownLatch latch) {
            this.latch = latch;
        }

        public HistoryCallback() {

        }

        @Override
        public void successCallback(String channel, Object message) {
            JSONArray resp = null;
            try {
                resp = (new JSONArray(message.toString())).getJSONArray(0);
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if (resp != null) {
                count = resp.length();
            }
            if (latch != null)
                latch.countDown();
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            if (latch != null)
                latch.countDown();
        }

    }

    Pubnub pubnub = new Pubnub("demo", "demo");
    Pubnub pubnub_enc = new Pubnub("demo", "demo", "demo", "demo", false);
    String testSuccessMessage = "";

    @Test
    public void testPublishString() throws PubnubException, InterruptedException {
        String channel = "java-unittest-" + Math.random();
        final String sendMessage = "Test Message " + Math.random();

        final CountDownLatch latch = new CountDownLatch(2);

        final PublishCallback pbCb = new PublishCallback(latch);
        SubscribeCallback sbCb = new SubscribeCallback(latch) {
            @Override
            public void connectCallback(String channel, Object message) {
                pubnub.publish(channel, sendMessage, pbCb);
            }
        };

        pubnub.subscribe(channel, sbCb);

        latch.await(10, TimeUnit.SECONDS);

        assertEquals(1, pbCb.getResult());
        assertEquals(sendMessage, sbCb.getResponse());
    }

    @Test
    public void testPublishJSONArray() throws PubnubException, InterruptedException {
        String channel = "java-unittest-" + Math.random();
        final JSONArray sendMessage = new JSONArray().put(1).put("Test");

        final CountDownLatch latch = new CountDownLatch(2);

        final PublishCallback pbCb = new PublishCallback(latch);
        SubscribeCallback sbCb = new SubscribeCallback(latch) {
            @Override
            public void connectCallback(String channel, Object message) {
                pubnub.publish(channel, sendMessage, pbCb);
            }
        };

        pubnub.subscribe(channel, sbCb);
        latch.await(10, TimeUnit.SECONDS);

        assertEquals(1, pbCb.getResult());
        assertEquals(sendMessage.toString(), sbCb.getResponse().toString());
    }

    @Test
    public void testPublishJSONObject() throws InterruptedException, PubnubException, JSONException {
        String channel = "java-unittest-" + Math.random();

        final JSONObject sendMessage;

        sendMessage = new JSONObject().put("1", "Test");

        final CountDownLatch latch = new CountDownLatch(2);

        final PublishCallback pbCb = new PublishCallback(latch);
        SubscribeCallback sbCb = new SubscribeCallback(latch) {
            @Override
            public void connectCallback(String channel, Object message) {
                pubnub.publish(channel, sendMessage, pbCb);
            }
        };

        pubnub.subscribe(channel, sbCb);

        latch.await(10, TimeUnit.SECONDS);

        assertEquals(1, pbCb.getResult());
        assertEquals(sendMessage.toString(), sbCb.getResponse().toString());
    }

    @Test
    public void testPublishStringWithEncryption() throws InterruptedException, PubnubException {
        String channel = "java-unittest-" + Math.random();
        final String sendMessage = "Test Message " + Math.random();

        final CountDownLatch latch = new CountDownLatch(2);

        final PublishCallback pbCb = new PublishCallback(latch);
        SubscribeCallback sbCb = new SubscribeCallback(latch) {
            @Override
            public void connectCallback(String channel, Object message) {
                pubnub_enc.publish(channel, sendMessage, pbCb);
            }
        };

        pubnub_enc.subscribe(channel, sbCb);
        latch.await(10, TimeUnit.SECONDS);

        assertEquals(1, pbCb.getResult());
        assertEquals(sendMessage, sbCb.getResponse());
    }

    @Test
    public void testPublishJSONArrayWithEncryption() throws PubnubException, InterruptedException {
        String channel = "java-unittest-" + Math.random();
        final JSONArray sendMessage = new JSONArray().put(1).put("Test");

        final CountDownLatch latch = new CountDownLatch(2);

        final PublishCallback pbCb = new PublishCallback(latch);
        SubscribeCallback sbCb = new SubscribeCallback(latch) {
            @Override
            public void connectCallback(String channel, Object message) {
                pubnub_enc.publish(channel, sendMessage, pbCb);
            }
        };

        pubnub_enc.subscribe(channel, sbCb);
        latch.await(10, TimeUnit.SECONDS);

        assertEquals(1, pbCb.getResult());
        assertEquals(sendMessage.toString(), sbCb.getResponse().toString());
    }

    @Test
    public void testPublishJSONObjectWithEncryption() throws InterruptedException, PubnubException, JSONException {
        String channel = "java-unittest-" + Math.random();

        final JSONObject sendMessage;

        sendMessage = new JSONObject().put("1", "Test");

        final CountDownLatch latch = new CountDownLatch(2);

        final PublishCallback pbCb = new PublishCallback(latch);
        SubscribeCallback sbCb = new SubscribeCallback(latch) {
            @Override
            public void connectCallback(String channel, Object message) {
                pubnub_enc.publish(channel, sendMessage, pbCb);
            }
        };

        pubnub_enc.subscribe(channel, sbCb);
        latch.await(10, TimeUnit.SECONDS);

        assertEquals(1, pbCb.getResult());
        assertEquals(sendMessage.toString(), sbCb.getResponse().toString());
    }

    @Test
    public void testHereNowOneUser() throws InterruptedException, PubnubException {
        String channel = "java-unittest-" + Math.random();

        final CountDownLatch latch = new CountDownLatch(2);

        final HereNowCallback hnCb = new HereNowCallback(latch);

        SubscribeCallback sbCb = new SubscribeCallback(latch) {
            @Override
            public void connectCallback(String channel, Object message) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {

                }
                pubnub.hereNow(channel, hnCb);
                pubnub.unsubscribe(channel);
                if (latch != null)
                    latch.countDown();
            }
        };

        pubnub.subscribe(channel, sbCb);
        latch.await(10, TimeUnit.SECONDS);

        assertEquals(1, hnCb.getOccupancy());
    }

    @Test
    public void testHistoryCountOne() {
        String channel = "java-unittest-" + Math.random();
        final String sendMessage = "Test Message " + Math.random();
        final CountDownLatch latch = new CountDownLatch(3);
        final int limit = 1;
        final HistoryCallback hCb = new HistoryCallback(latch);

        pubnub.publish(channel, sendMessage, new PublishCallback(latch) {
            @Override
            public void successCallback(String channel, Object message) {
                pubnub.publish(channel, sendMessage,
                new PublishCallback(latch) {
                    @Override
                    public void successCallback(String channel,
                    Object message) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        pubnub.history(channel, limit, hCb);
                        super.successCallback(channel, message);
                    }
                });
                super.successCallback(channel, message);
            }
        });
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(limit, hCb.getCount());
    }

    @Test
    public void testHistory() {
        String channel = "java-unittest-" + Math.random();
        final String sendMessage = "Test Message " + Math.random();
        final CountDownLatch latch = new CountDownLatch(3);

        final HistoryCallback hCb = new HistoryCallback(latch);

        pubnub.publish(channel, sendMessage, new PublishCallback(latch) {
            @Override
            public void successCallback(String channel, Object message) {
                pubnub.publish(channel, sendMessage,
                new PublishCallback(latch) {
                    @Override
                    public void successCallback(String channel,
                    Object message) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        pubnub.history(channel, 100, hCb);
                        super.successCallback(channel, message);
                    }
                });
                super.successCallback(channel, message);
            }
        });
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(2, hCb.getCount());
    }

    @Test
    public void testConnectionRestore() throws PubnubException, InterruptedException {
        String channel = "java-unittest-" + Math.random();

        final CountDownLatch latch = new CountDownLatch(3);
        final SubscribeCallback subscribeCb = new SubscribeCallback(latch);

        pubnub.setResumeOnReconnect(true);

        pubnub.subscribe(channel, new SubscribeCallback(latch) {
            public void connectCallback(String channel, Object message) {
                pubnub.unsubscribe(channel);
                pubnub.publish(channel, 10, new PublishCallback(latch) {
                    public void successCallback(String channel, Object message) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        try {
                            pubnub.subscribe(channel, subscribeCb);
                        } catch (PubnubException e) {
                            e.printStackTrace();
                        }

                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        assertEquals(10, subscribeCb.getResponse());
    }

    @Test
    public void testConnectionRestoreUnsubInSuccessCallback() throws PubnubException, InterruptedException {
        String channel = "java-unittest-" + Math.random();

        final CountDownLatch latch = new CountDownLatch(5);
        final SubscribeCallback subscribeCb = new SubscribeCallback(latch);

        pubnub.setResumeOnReconnect(true);

        pubnub.subscribe(channel, new SubscribeCallback(latch) {
            public void connectCallback(String channel, Object message) {
                pubnub.publish(channel, 10, new PublishCallback(latch) {
                    public void successCallback(String channel, Object message) {
                        pubnub.unsubscribe(channel);
                        pubnub.publish(channel, 20, new PublishCallback(latch) {
                            public void successCallback(String channel, Object message) {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }

                                try {
                                    pubnub.subscribe(channel, subscribeCb);
                                } catch (PubnubException e) {
                                    e.printStackTrace();
                                }

                                latch.countDown();
                            }
                        });
                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        assertEquals(20, subscribeCb.getResponse());
    }

    @Test
    public void testConnectionRestoreMultipleChannels() throws PubnubException, InterruptedException {
        String channel = "java-unittest-" + Math.random();

        final CountDownLatch latch = new CountDownLatch(3);
        final SubscribeCallback subscribeCb = new SubscribeCallback(latch);

        pubnub.setResumeOnReconnect(true);

        pubnub.subscribe(channel, new SubscribeCallback(latch) {
            public void connectCallback(String channel, Object message) {
                pubnub.unsubscribe(channel);
                pubnub.publish(channel, 10, new PublishCallback(latch) {
                    public void successCallback(String channel, Object message) {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        try {
                            pubnub.subscribe(channel, subscribeCb);
                        } catch (PubnubException e) {
                            e.printStackTrace();
                        }

                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        assertEquals(10, subscribeCb.getResponse());
    }

    @Test
    public void testConnectionRestoreMultipleChannelsUnsubInSuccessCallback() throws InterruptedException, PubnubException {
        String channel = "java-unittest-" + Math.random();

        final CountDownLatch latch = new CountDownLatch(5);
        final SubscribeCallback subscribeCb = new SubscribeCallback(latch);

        pubnub.setResumeOnReconnect(true);

        pubnub.subscribe(channel, new SubscribeCallback(latch) {
            public void connectCallback(String channel, Object message) {
                pubnub.publish(channel, 10, new PublishCallback(latch) {
                    public void successCallback(String channel, Object message) {
                        pubnub.unsubscribe(channel);
                        pubnub.publish(channel, 20, new PublishCallback(latch) {
                            public void successCallback(String channel, Object message) {
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }

                                try {
                                    pubnub.subscribe(channel, subscribeCb);
                                } catch (PubnubException e) {
                                    e.printStackTrace();
                                }

                                latch.countDown();
                            }
                        });
                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        assertEquals(20, subscribeCb.getResponse());
    }

    @Test
    public void testConnectionRestoreFalse() throws InterruptedException, PubnubException {
        String channel = "java-unittest-" + Math.random();

        final CountDownLatch latch = new CountDownLatch(2);
        final SubscribeCallback subscribeCb = new SubscribeCallback(latch);

        pubnub.setResumeOnReconnect(false);

        pubnub.subscribe(channel, new SubscribeCallback(latch) {
            public void connectCallback(String channel, Object message) {
                pubnub.unsubscribe(channel);
                pubnub.publish(channel, 10, new PublishCallback(latch) {
                    public void successCallback(String channel, Object message) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        try {
                            pubnub.subscribe(channel, subscribeCb);
                        } catch (PubnubException e) {
                            e.printStackTrace();
                        }

                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        assertNull(subscribeCb.getResponse());
    }

    @Test
    public void testConnectionRestoreFalseMultipleChannels() throws InterruptedException, PubnubException {
        String channel = "java-unittest-" + Math.random();
        final CountDownLatch latch = new CountDownLatch(3);

        final SubscribeCallback subscribeCb = new SubscribeCallback(latch);

        pubnub.setResumeOnReconnect(false);

        pubnub.subscribe(new String[]{channel, "b"}, new SubscribeCallback(latch) {
            public void connectCallback(String channel, Object message) {
                pubnub.unsubscribe(channel);
                pubnub.publish(channel, 10, new PublishCallback(latch) {
                    public void successCallback(String channel, Object message) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        try {
                            pubnub.subscribe(channel, subscribeCb);
                        } catch (PubnubException e) {
                            e.printStackTrace();
                        }

                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        assertNull(subscribeCb.getResponse());
    }

    @Test
    public void testConnectionRestoreFalseUnsubInSuccessCallback() throws InterruptedException, PubnubException {
        String channel = "java-unittest-" + Math.random();

        final CountDownLatch latch = new CountDownLatch(3);
        final SubscribeCallback subscribeCb = new SubscribeCallback(latch);

        pubnub.setResumeOnReconnect(false);

        pubnub.subscribe(channel, new SubscribeCallback(latch) {
            public void connectCallback(String channel, Object message) {
                pubnub.publish(channel, 10, new PublishCallback(latch) {
                    public void successCallback(String channel, Object message) {
                        pubnub.unsubscribe(channel);
                        pubnub.publish(channel, 20, new PublishCallback(latch) {
                            public void successCallback(String channel, Object message) {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }

                                try {
                                    pubnub.subscribe(channel, subscribeCb);
                                } catch (PubnubException e) {
                                    e.printStackTrace();
                                }

                                latch.countDown();
                            }
                        });
                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        assertNull(subscribeCb.getResponse());
    }

    @Test
    public void testConnectionRestoreFalseMultipleChannelsUnsubInSuccessCallback() throws InterruptedException, PubnubException {
        String channel = "java-unittest-" + Math.random();

        final CountDownLatch latch = new CountDownLatch(3);
        final SubscribeCallback subscribeCb = new SubscribeCallback(latch);

        pubnub.setResumeOnReconnect(false);

        pubnub.subscribe(new String[]{channel, "b"}, new SubscribeCallback(latch) {
            public void connectCallback(String channel, Object message) {
                pubnub.publish(channel, 10, new PublishCallback(latch) {
                    public void successCallback(String channel, Object message) {
                        pubnub.unsubscribe(channel);
                        pubnub.publish(channel, 20, new PublishCallback(latch) {
                            public void successCallback(String channel, Object message) {
                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }

                                try {
                                    pubnub.subscribe(channel, subscribeCb);
                                } catch (PubnubException e) {
                                    e.printStackTrace();
                                }

                                latch.countDown();
                            }
                        });
                        latch.countDown();
                    }
                });
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        assertNull(subscribeCb.getResponse());
    }

    @Test
    public void testSubscribeInMultipleThreads() throws InterruptedException {
        final String channel = "java-unittest-" + Math.random();
        final CountDownLatch latch = new CountDownLatch(100);
        final Hashtable<String, Object> results = new Hashtable<String, Object>();
        final Hashtable<String, Object> inputs = new Hashtable<String, Object>();
        final Hashtable<String, Object> stats = new Hashtable<String, Object>();
        final int count = 100;
        final Random rand = new Random();
        stats.put("connects", 0);
        stats.put("count", 0);
        stats.put("duplicate", false);
        stats.put("wrong", false);

        class SubscribeThread implements Runnable {

            class PublishThread implements Runnable {
                private String ch;
                private int message;

                PublishThread(String ch, int message) {
                    this.ch = ch;
                    this.message = message;
                }

                public void run() {
                    try {
                        Thread.sleep(rand.nextInt(20000));
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    pubnub.publish(ch, message, new Callback() {

                        @Override
                        public void successCallback(String channel,
                        Object message) {
                            stats.put("count", (Integer) stats.get("count") + 1);
                        }

                        @Override
                        public void errorCallback(String channel,
                        PubnubError error) {

                        }
                    });
                }
            }

            private String ch;

            SubscribeThread(String ch) {
                this.ch = ch;
            }

            private void startPublish() {
                for (int i = 1; i <= count; i++) {
                    inputs.put(channel + "-" + i, i);
                    new Thread(new PublishThread(channel + "-" + i, i)).start();
                }
            }

            public void run() {

                Hashtable<String, Object> args = new Hashtable<String, Object>();

                args.put("channels", new String[]{ch});
                args.put("callback", new Callback() {

                    @Override
                    public void connectCallback(String channel, Object message) {

                        stats.put("connects", (Integer) (stats.get("connects")) + 1);

                        if ((Integer) stats.get("connects") >= count)
                            startPublish();
                    }

                    @Override
                    public void successCallback(String channel, Object message) {
                        int c = (results.get(channel) == null) ? 0
                                : (Integer) results.get(channel);
                        int d = (inputs.get(channel) == null) ? 0
                                : (Integer) inputs.get(channel);
                        if (((Integer) c).equals((Integer) message))
                            stats.put("duplicate", true);
                        if (!(((Integer) d).equals((Integer) message)))
                            stats.put("wrong", true);

                        assertEquals((Integer) d, (Integer) message);
                        results.put(channel, message);
                        stats.put("count", (Integer) stats.get("count") - 1);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                    }
                });
                try {
                    pubnub.subscribe(args);
                } catch (PubnubException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 1; i <= count; i++) {
            inputs.put(channel + "-" + i, i);
            new Thread(new SubscribeThread(channel + "-" + i)).start();
        }

        latch.await(30, TimeUnit.SECONDS);

        assertFalse((Boolean) stats.get("duplicate").equals(true));
        assertFalse((Boolean) stats.get("wrong").equals(true));
        assertEquals((Integer)0,(Integer) stats.get("count"));
    }

    @Test
    public void testSubscribeInMultipleThreads3() throws InterruptedException {
        final String channel = "java-unittest-" + Math.random();
        final CountDownLatch latch = new CountDownLatch(100);
        final Hashtable results = new Hashtable();
        final Hashtable inputs = new Hashtable();
        final Hashtable stats = new Hashtable();
        final int count = 100;
        final Random rand = new Random();
        stats.put("connects", 0);
        stats.put("count", 0);
        stats.put("duplicate", false);
        stats.put("wrong", false);

        class PublishThread implements Runnable {
            private String ch;
            private int message;

            PublishThread(String ch, int message) {
                this.ch = ch;
                this.message = message;
            }

            public void run() {
                try {
                    Thread.sleep(rand.nextInt(20000));
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                Hashtable args = new Hashtable();
                args.put("channel", ch);
                args.put("message", message);
                args.put("callback", new Callback() {

                    @Override
                    public void successCallback(String channel, Object message) {
                        stats.put("count", (Integer) stats.get("count") + 1);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {

                    }
                });
                pubnub.publish(args, new Callback() {});
            }
        }
        class SubscribeThread implements Runnable {
            private String ch;

            SubscribeThread(String ch) {
                this.ch = ch;
            }

            private void startPublish() {
                for (int i = 1; i <= count; i++) {
                    inputs.put(channel + "-" + i, i);
                    new Thread(new PublishThread(channel + "-" + i, i)).start();
                }
            }

            public void run() {
                try {
                    Thread.sleep(rand.nextInt(5000));
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                Hashtable args = new Hashtable();

                args.put("channels", new String[]{ch});
                args.put("callback", new Callback() {

                    @Override
                    public void successCallback(String channel, Object message) {
                        int c = (results.get(channel) == null) ? 0
                                : (Integer) results.get(channel);
                        int d = (inputs.get(channel) == null) ? 0
                                : (Integer) inputs.get(channel);
                        if (((Integer) c).equals((Integer) message))
                            stats.put("duplicate", true);
                        if (!(((Integer) d).equals((Integer) message)))
                            stats.put("wrong", true);
                        assertEquals((Integer) d, (Integer) message);
                        results.put(channel, message);
                        stats.put("count", (Integer) stats.get("count") - 1);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                    }
                });
                try {
                    pubnub.subscribe(args);
                } catch (PubnubException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 1; i <= count; i++) {
            inputs.put(channel + "-" + i, i);
            new Thread(new SubscribeThread(channel + "-" + i)).start();
        }
        for (int i = 1; i <= count; i++) {
            new Thread(new PublishThread(channel + "-" + i, i)).start();
        }

        latch.await(30, TimeUnit.SECONDS);

        assertFalse((Boolean) stats.get("duplicate").equals(true));
        assertFalse((Boolean) stats.get("wrong").equals(true));
        // assertEquals((Integer)count,(Integer)results.get("count"));

    }

    @Test
    public void testSubscribeInMultipleThreads2() {
        final String channel = "java-unittest-" + Math.random();
        final CountDownLatch latch = new CountDownLatch(100);
        final Hashtable results = new Hashtable();
        final Hashtable inputs = new Hashtable();
        final Hashtable stats = new Hashtable();
        final int count = 100;
        final Random rand = new Random();
        stats.put("connects", 0);
        stats.put("count", 0);
        stats.put("duplicate", false);
        stats.put("wrong", false);

        class PublishThread implements Runnable {
            private String ch;
            private int message;

            PublishThread(String ch, int message) {
                this.ch = ch;
                this.message = message;
            }

            public void run() {
                try {
                    Thread.sleep(rand.nextInt(20000));
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                Hashtable args = new Hashtable();
                args.put("channel", ch);
                args.put("message", message);
                args.put("callback", new Callback() {

                    @Override
                    public void successCallback(String channel, Object message) {
                        stats.put("count", (Integer) stats.get("count") + 1);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {

                    }
                });
                pubnub.publish(args, new Callback() {});
            }
        }
        class SubscribeThread implements Runnable {
            private String ch;

            SubscribeThread(String ch) {
                this.ch = ch;
            }

            private void startPublish() {
                for (int i = 1; i <= count; i++) {
                    inputs.put(channel + "-" + i, i);
                    new Thread(new PublishThread(channel + "-" + i, i)).start();
                }
            }

            public void run() {
                try {
                    Thread.sleep(rand.nextInt(2000));
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                Hashtable args = new Hashtable();
                args.put("channels", new String[]{ch});
                args.put("callback", new Callback() {

                    @Override
                    public void successCallback(String channel, Object message) {
                        int c = (results.get(channel) == null) ? 0
                                : (Integer) results.get(channel);
                        int d = (inputs.get(channel) == null) ? 0
                                : (Integer) inputs.get(channel);
                        if (((Integer) c).equals((Integer) message))
                            stats.put("duplicate", true);
                        if (!(((Integer) d).equals((Integer) message)))
                            stats.put("wrong", true);
                        assertEquals((Integer) d, (Integer) message);
                        results.put(channel, message);
                        stats.put("count", (Integer) stats.get("count") - 1);
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                    }
                });
                try {
                    pubnub.subscribe(args);
                } catch (PubnubException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 1; i <= count; i++) {
            inputs.put(channel + "-" + i, i);
            new Thread(new SubscribeThread(channel + "-" + i)).start();
        }
        for (int i = 1; i <= count; i++) {
            new Thread(new PublishThread(channel + "-" + i, i)).start();
        }

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse((Boolean) stats.get("duplicate").equals(true));
        assertFalse((Boolean) stats.get("wrong").equals(true));
        // assertEquals((Integer)count,(Integer)results.get("count"));

    }

    @Test
    public void testSubscribeInMultipleThreadsWithDarRorTrue() {
        final String channel = "java-unittest-" + Math.random();
        final CountDownLatch latch = new CountDownLatch(100);
        final Hashtable results = new Hashtable();
        final Hashtable inputs = new Hashtable();
        final Hashtable stats = new Hashtable();
        final int count = 125;
        final Random rand = new Random();
        stats.put("connects", 0);
        stats.put("count", 0);
        stats.put("duplicate", false);
        stats.put("wrong", false);

        pubnub.setResumeOnReconnect(true);

        class DarThread implements Runnable {
            private volatile boolean _die;

            public void stop() {
                _die = true;
            }

            public void run() {
                while (!_die) {
                    pubnub.disconnectAndResubscribe();
                    try {
                        Thread.sleep(rand.nextInt(5000));
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        class SubscribeThread implements Runnable {

            class PublishThread implements Runnable {
                private String ch;
                private int message;

                PublishThread(String ch, int message) {
                    this.ch = ch;
                    this.message = message;
                }

                public void run() {
                    try {
                        Thread.sleep(rand.nextInt(20000));
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    Hashtable args = new Hashtable();
                    args.put("channel", ch);
                    args.put("message", message);
                    args.put("callback", new Callback() {

                        @Override
                        public void successCallback(String channel,
                        Object message) {
                            stats.put("count", (Integer) stats.get("count") + 1);
                        }

                        @Override
                        public void errorCallback(String channel,
                        PubnubError error) {

                        }
                    });
                    pubnub.publish(args, new Callback() {});
                }
            }

            private String ch;

            SubscribeThread(String ch) {
                this.ch = ch;
            }

            private void startPublish() {
                for (int i = 1; i <= count; i++) {
                    inputs.put(channel + "-" + i, i);
                    new Thread(new PublishThread(channel + "-" + i, i)).start();
                }
            }

            public void run() {

                Hashtable args = new Hashtable();
                args.put("channel", ch);
                args.put("callback", new Callback() {

                    @Override
                    public void connectCallback(String channel, Object message) {

                        stats.put("connects",
                                  (Integer) (stats.get("connects")) + 1);
                        if ((Integer) stats.get("connects") >= count) {
                            startPublish();
                            new Thread(new DarThread()).start();
                        }
                    }

                    @Override
                    public void successCallback(String channel, Object message) {
                        int c = (results.get(channel) == null) ? 0
                                : (Integer) results.get(channel);
                        int d = (inputs.get(channel) == null) ? 0
                                : (Integer) inputs.get(channel);
                        if (((Integer) c).equals((Integer) message)) {
                            stats.put("duplicate", true);
                        }
                        if (!(((Integer) d).equals((Integer) message)))
                            stats.put("wrong", true);
                        results.put(channel, message);
                        stats.put("count", (Integer) stats.get("count") - 1);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                    }
                });
                try {
                    pubnub.subscribe(args);
                } catch (PubnubException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 1; i <= count; i++) {
            inputs.put(channel + "-" + i, i);
            new Thread(new SubscribeThread(channel + "-" + i)).start();
        }

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse((Boolean) stats.get("duplicate").equals(true));
        assertFalse((Boolean) stats.get("wrong").equals(true));
        assertEquals((Integer)0, (Integer) stats.get("count"));

    }

    @Test
    public void testSubscribeInMultipleThreadsWithDarRorFalse() {
        final String channel = "java-unittest-" + Math.random();
        final CountDownLatch latch = new CountDownLatch(100);
        final Hashtable results = new Hashtable();
        final Hashtable inputs = new Hashtable();
        final Hashtable stats = new Hashtable();
        final int count = 125;
        final Random rand = new Random();
        stats.put("connects", 0);
        stats.put("count", 0);
        stats.put("duplicate", false);
        stats.put("wrong", false);

        pubnub.setResumeOnReconnect(false);

        class DarThread implements Runnable {
            private volatile boolean _die;

            public void stop() {
                _die = true;
            }

            public void run() {
                while (!_die) {
                    pubnub.disconnectAndResubscribe();
                    try {
                        Thread.sleep(rand.nextInt(10000));
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        class SubscribeThread implements Runnable {

            class PublishThread implements Runnable {
                private String ch;
                private int message;

                PublishThread(String ch, int message) {
                    this.ch = ch;
                    this.message = message;
                }

                public void run() {
                    try {
                        Thread.sleep(rand.nextInt(20000));
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                    Hashtable args = new Hashtable();
                    args.put("channel", ch);
                    args.put("message", message);
                    args.put("callback", new Callback() {

                        @Override
                        public void successCallback(String channel,
                        Object message) {
                            stats.put("count", (Integer) stats.get("count") + 1);
                        }

                        @Override
                        public void errorCallback(String channel,
                        PubnubError error) {

                        }
                    });
                    pubnub.publish(args, new Callback() {});
                }
            }

            private String ch;

            SubscribeThread(String ch) {
                this.ch = ch;
            }

            private void startPublish() {
                for (int i = 1; i <= count; i++) {
                    inputs.put(channel + "-" + i, i);
                    new Thread(new PublishThread(channel + "-" + i, i)).start();
                }
            }

            public void run() {

                Hashtable args = new Hashtable();
                args.put("channels", new String[]{ch});
                args.put("callback", new Callback() {

                    @Override
                    public void connectCallback(String channel, Object message) {

                        stats.put("connects",
                                  (Integer) (stats.get("connects")) + 1);
                        if ((Integer) stats.get("connects") >= count) {
                            startPublish();
                            new Thread(new DarThread()).start();
                        }
                    }

                    @Override
                    public void successCallback(String channel, Object message) {
                        int c = (results.get(channel) == null) ? 0
                                : (Integer) results.get(channel);
                        int d = (inputs.get(channel) == null) ? 0
                                : (Integer) inputs.get(channel);
                        if (((Integer) c).equals((Integer) message)) {
                            stats.put("duplicate", true);
                        }
                        if (!(((Integer) d).equals((Integer) message)))
                            stats.put("wrong", true);
                        results.put(channel, message);
                        stats.put("count", (Integer) stats.get("count") - 1);
                    }

                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                    }
                });
                try {
                    pubnub.subscribe(args);
                } catch (PubnubException e) {
                    e.printStackTrace();
                }
            }
        }

        for (int i = 1; i <= count; i++) {
            inputs.put(channel + "-" + i, i);
            new Thread(new SubscribeThread(channel + "-" + i)).start();
        }

        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse((Boolean) stats.get("duplicate").equals(true));
        assertFalse((Boolean) stats.get("wrong").equals(true));
        // assertEquals((Integer)count,(Integer)results.get("count"));
    }

    @Test
    public void testUlsGrantRW() {
        final CountDownLatch latch = new CountDownLatch(1);
        UlsCallback ulscb = new UlsCallback(latch);
        Pubnub pubnub = new Pubnub("pub-c-a2650a22-deb1-44f5-aa87-1517049411d5",
                                   "sub-c-a478dd2a-c33d-11e2-883f-02ee2ddab7fe",
                                   "sec-c-YjFmNzYzMGMtYmI3NC00NzJkLTlkYzYtY2MwMzI4YTJhNDVh");

        pubnub.setCacheBusting(false);
        pubnub.pamGrant("hello-uls", "abcd", true, true, 1400, ulscb);

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            assertEquals("sub-c-a478dd2a-c33d-11e2-883f-02ee2ddab7fe", ulscb.getResult().getString("subscribe_key"));
        } catch (JSONException e) {
            assertTrue(false);
        }
    }
    @Test
    public void testUlsAuditSubKey() {
        final CountDownLatch latch = new CountDownLatch(1);
        UlsCallback ulscb = new UlsCallback(latch);
        Pubnub pubnub = new Pubnub("pub-c-a2650a22-deb1-44f5-aa87-1517049411d5",
                                   "sub-c-a478dd2a-c33d-11e2-883f-02ee2ddab7fe",
                                   "sec-c-YjFmNzYzMGMtYmI3NC00NzJkLTlkYzYtY2MwMzI4YTJhNDVh");

        pubnub.setCacheBusting(false);
        pubnub.pamAudit(ulscb);

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            assertEquals("sub-c-a478dd2a-c33d-11e2-883f-02ee2ddab7fe", ulscb.getResult().getString("subscribe_key"));
        } catch (JSONException e) {
            assertTrue(false);
        }
    }
    @Test
    public void testUlsAuditChannel() {
        final CountDownLatch latch = new CountDownLatch(1);
        UlsCallback ulscb = new UlsCallback(latch);
        Pubnub pubnub = new Pubnub("pub-c-a2650a22-deb1-44f5-aa87-1517049411d5",
                                   "sub-c-a478dd2a-c33d-11e2-883f-02ee2ddab7fe",
                                   "sec-c-YjFmNzYzMGMtYmI3NC00NzJkLTlkYzYtY2MwMzI4YTJhNDVh");

        pubnub.setCacheBusting(false);
        pubnub.pamAudit("hello-uls", ulscb);

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            assertTrue(ulscb.getResult().getJSONObject("channels").getJSONObject("hello-uls") != null);
        } catch (JSONException e) {
            assertTrue(false);
        }
    }
    @Test
    public void testUlsAuditAuth() {
        final CountDownLatch latch = new CountDownLatch(1);
        UlsCallback ulscb = new UlsCallback(latch);
        Pubnub pubnub = new Pubnub("pub-c-a2650a22-deb1-44f5-aa87-1517049411d5",
                                   "sub-c-a478dd2a-c33d-11e2-883f-02ee2ddab7fe",
                                   "sec-c-YjFmNzYzMGMtYmI3NC00NzJkLTlkYzYtY2MwMzI4YTJhNDVh");

        pubnub.setCacheBusting(false);
        pubnub.pamAudit("hello-uls", "abcd", ulscb);

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            assertEquals("hello-uls", ulscb.getResult().getString("channel"));
        } catch (JSONException e) {
            assertTrue(false);
        }
    }
    @Test
    public void testUlsRevoke() {
        final CountDownLatch latch = new CountDownLatch(1);
        UlsCallback ulscb = new UlsCallback(latch);
        Pubnub pubnub = new Pubnub("pub-c-a2650a22-deb1-44f5-aa87-1517049411d5",
                                   "sub-c-a478dd2a-c33d-11e2-883f-02ee2ddab7fe",
                                   "sec-c-YjFmNzYzMGMtYmI3NC00NzJkLTlkYzYtY2MwMzI4YTJhNDVh");

        pubnub.setCacheBusting(false);
        pubnub.pamRevoke("hello-uls", "abcd", ulscb);

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            assertEquals("hello-uls", ulscb.getResult().getString("channel"));
        } catch (JSONException e) {
            assertTrue(false);
        }
    }
}
