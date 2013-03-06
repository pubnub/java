package com.pubnub.examples;

import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

public class PubnubPublishTest {

    String channel = "pubnub_java_api";
    String channel_enc = "pubnub_java_api_enc";

    Pubnub _pubnub;
    Pubnub _pubnub_enc;

    Object[] messages;

    public PubnubPublishTest() {
        _pubnub = new Pubnub("demo", "demo", false);
        _pubnub_enc = new Pubnub("demo", "demo", "demo", "demo", false);
        messages = new Object[3];
        messages[0] = "This is a string message";
        JSONArray jsarr = new JSONArray();
        jsarr.put("This is json array");
        jsarr.put("One more entry in json array");
        messages[1] = jsarr;
        JSONObject jsobj = new JSONObject();
        try {
            jsobj.put("msg1", "Hi");
            jsobj.put("msg2", "Java");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        messages[2] = jsobj;
    }

    /**
     * @param params
     */
    public static void main(String[] params) {

        PubnubPublishTest ppt = new PubnubPublishTest();
        ppt.runSubscribe();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        ppt.runPublish();
        ppt.runHistory();
        ppt.runDetailedHistory();
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
        }
        ppt._pubnub.shutdown();
        ppt._pubnub_enc.shutdown();
    }

    public void runSubscribe() {
        try {
            _pubnub.subscribe(new String[] { channel }, new Callback() {
                public void successCallback(String channel, Object message) {
                    notifyUser("RECV : " + channel + " : " + message.getClass()
                            + " : " + message.toString());
                }

                public void errorCallback(String channel, Object message) {
                    notifyUser("RECV : " + channel + " : " + message.getClass()
                            + " : " + message.toString());
                }

            });
            _pubnub_enc.subscribe(new String[] { channel_enc }, new Callback() {
                public void successCallback(String channel, Object message) {
                    notifyUser("RECV : " + channel + " : " + message.getClass()
                            + " : " + message.toString());
                }

                public void errorCallback(String channel, Object message) {
                    notifyUser("RECV : " + channel + " : " + message.getClass()
                            + " : " + message.toString());
                }
            });

        } catch (PubnubException e) {
            e.printStackTrace();
        }

    }

    public void runPublish() {
        for (int i = 0; i < 3; i++) {
            publish(_pubnub, channel, messages[i]);
            publish(_pubnub_enc, channel_enc, messages[i]);
        }
    }

    public void runHistory() {
        history(_pubnub, channel);
        history(_pubnub_enc, channel_enc);
    }

    public void runDetailedHistory() {
        detailedHistory(_pubnub, channel);
        detailedHistory(_pubnub_enc, channel_enc);
    }

    private static void notifyUser(Object message) {
        System.out.println(message.toString());
    }

    public void publish(Pubnub pubnub, String channel, final Object msg) {
        Hashtable args = new Hashtable(2);
        args.put("channel", channel); // Channel Name
        args.put("message", msg); // JSON Message
        pubnub.publish(args, new Callback() {
            public void successCallback(String channel, Object message) {
                notifyUser("SENT : " + channel + " : " + msg.getClass() + " : "
                        + message.toString());
            }

            public void errorCallback(String channel, Object message) {
                notifyUser("SENT : " + channel + " : " + msg.getClass() + " : "
                        + message.toString());
            }
        });
    }

    public void history(Pubnub pubnub, String channel) {

        pubnub.history(channel, 10, new Callback() {
            public void successCallback(String channel, Object message) {
                notifyUser("HISTORY : " + channel + " : " + message.getClass()
                        + " : " + message.toString());
            }

            public void errorCallback(String channel, Object message) {
                notifyUser("HISTORY : " + channel + " : " + message.getClass()
                        + " : " + message.toString());
            }
        });
    }

    public void detailedHistory(Pubnub pubnub, String channel) {

        pubnub.detailedHistory(channel, 10, new Callback() {
            public void successCallback(String channel, Object message) {
                notifyUser("DETAILED HISTORY : " + channel + " : "
                        + message.getClass() + " : " + message.toString());
            }

            public void errorCallback(String channel, Object message) {
                notifyUser("DETAILED HISTORY : " + channel + " : "
                        + message.getClass() + " : " + message.toString());
            }
        });
    }
}
