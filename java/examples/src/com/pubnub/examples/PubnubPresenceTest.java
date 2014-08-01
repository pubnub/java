package com.pubnub.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubException;

enum Status {
    PASS,
    FAIL
};

class Test {
    private String subkey;
    private String origin;
    private Map<String, String> channels;
    private String channelA;
    private String channelB;
    private JSONObject steps;
    private JSONArray events;
    private Status result;
    private int id;
    private String description;
    private boolean ssl;
    private int currentStep = -1;

    Test(int id, String description, String subkey, String origin, String channelA, String channelB, boolean ssl, JSONObject steps) {
       this.id = id;
       this.description = description;
       this.subkey = subkey;
       this.origin = origin;
       this.channels = new LinkedHashMap<String,String>();
       channels.put("channelA", channelA);
       channels.put("channelB", channelB);
       this.ssl = ssl;
       this.result = Status.FAIL;
       this.steps = steps;
    }

    void updateStatus() {
        for (int i = 0; i < steps.size(); i++) {

            JSONObject step = (JSONObject) steps.get(String.valueOf(i));
            JSONArray expectedEvents = (JSONArray) step.get("listener");
            JSONArray observedEvents = (JSONArray) step.get("events");

            if (expectedEvents.size() != observedEvents.size()) {
                result = Status.FAIL;
                return;
            }

            if (expectedEvents.size() <= 2) {
                for (int j = 0; j < expectedEvents.size(); j++) {
                    String expectedEventChannel = channels.get((String)((JSONArray)expectedEvents.get(j)).get(1));
                    String expectedEventAction = (String)((JSONArray)expectedEvents.get(j)).get(0);

                    String observedEventChannel = (String)((JSONObject)observedEvents.get(j)).get("channel");
                    String observedEventAction = (String)((JSONObject)observedEvents.get(j)).get("action");

                    if ( !expectedEventChannel.equals(observedEventChannel) ||
                            !expectedEventAction.equals(observedEventAction)) {
                        result = Status.FAIL;
                        return;
                    }
                }
            } else {
                Set expected = new LinkedHashSet();
                Set observed = new LinkedHashSet();
                for (int j = 0; j < expectedEvents.size(); j++) {
                    String expectedEventChannel = channels.get((String)((JSONArray)expectedEvents.get(j)).get(1));
                    String expectedEventAction = (String)((JSONArray)expectedEvents.get(j)).get(0);

                    String observedEventChannel = (String)((JSONObject)observedEvents.get(j)).get("channel");
                    String observedEventAction = (String)((JSONObject)observedEvents.get(j)).get("action");

                    expected.add(expectedEventChannel + "," + expectedEventAction);
                    observed.add(observedEventChannel + "," + observedEventAction);
                }
                if (!expected.equals(observed)) {
                    result = Status.FAIL;
                    return;
                }

            }
            result = Status.PASS;
        }
    }
    void printResult () {
        updateStatus();
        System.out.println();
        System.out.println("-----------------");
        System.out.println("Test Id : " + id + ", " + description);
        System.out.println("Status : " + result);
        System.out.println("Origin : " + origin);
        System.out.println("SSL : " + ssl);
        System.out.println("Sub Key : " + subkey);
        System.out.println();

        for (int i = 0 ; i < steps.size() ; i++) {
            JSONObject jso = (JSONObject) steps.get(String.valueOf(i));
            jso.toJSONString();
            System.out.println(jso.toJSONString());
        }
        System.out.println("-----------------");
        System.out.println();
    }

    void run() {

        Pubnub listener = new Pubnub("", subkey);
        Pubnub actor = new Pubnub("",subkey, ssl);

        try {
            listener.presence(channels.get("channelA"), new Callback(){

                @Override
                public void successCallback(String channel, Object message) {
                    System.out.println(channel + " : " + message);
                    JSONObject jso = null;
                    try {
                        jso = (JSONObject)new JSONParser().parse(message.toString());
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    jso.put("channel", channel.split("-pnpres")[0]);
                    jso.put("time", Calendar.getInstance().getTime());
                    JSONObject step =  (JSONObject)steps.get(String.valueOf(currentStep));
                    JSONArray events = (JSONArray) step.get("events");
                    if (events == null) {
                        step.put("events", new JSONArray());
                        events = (JSONArray) step.get("events");
                    }
                    events.add(jso);
                }

            });
        } catch (PubnubException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            listener.presence(channels.get("channelB"), new Callback(){

                @Override
                public void successCallback(String channel, Object message) {
                    System.out.println(channel + " : " + message);
                    JSONObject jso = null;
                    try {
                        jso = (JSONObject)new JSONParser().parse(message.toString());
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    jso.put("channel", channel.split("-pnpres")[0]);
                    jso.put("time", Calendar.getInstance().getTime());

                    JSONObject step =  (JSONObject)steps.get(String.valueOf(currentStep));
                    JSONArray events = (JSONArray) step.get("events");
                    if (events == null) {
                        step.put("events", new JSONArray());
                        events = (JSONArray) step.get("events");
                    }
                    events.add(jso);

                }

            });
        } catch (PubnubException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }


        for (int i = 0 ; i < steps.size(); i++) {
            currentStep++;
            JSONObject step = (JSONObject) steps.get(String.valueOf(i));
            JSONArray actorStep = (JSONArray) step.get("actor");

            if (actorStep.get(0).equals("subscribe")) {
                System.out.println("SUBSCRIBE : " +  channels.get(actorStep.get(1)));
                try {
                    actor.subscribe(channels.get(actorStep.get(1)), new Callback(){

                        @Override
                        public void successCallback(String channel,
                                Object message) {

                        }

                    });
                } catch (PubnubException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                System.out.println("UNSUBSCRIBE : " +  channels.get(actorStep.get(1)));
                actor.unsubscribe(channels.get(actorStep.get(1)));

            }
            JSONArray listenerStep = (JSONArray)step.get("listener");

            Long stepDelay = 0L;

            for (int k = 0; k < listenerStep.size(); k++) {
                Long delay = (Long) ((JSONArray)listenerStep.get(k)).get(2);
                if (delay > stepDelay) stepDelay = delay;
            }

            System.out.println("[" + Calendar.getInstance().getTime() + "] :  wait for " + (stepDelay/1000.00) + " seconds");
            try {
                Thread.sleep(stepDelay);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        }

    }


}


public class PubnubPresenceTest {
    static List<Test> testsList = new ArrayList<Test>();
    static String client = "3.6";

    public static void main(String[] args) {

        JSONParser parser = new JSONParser();



        try {

            Object obj = parser.parse(new FileReader("./tests.json"));

            JSONArray tests =  (JSONArray) obj;
            obj = parser.parse(new FileReader("./keysets.json"));
            JSONObject keysets = (JSONObject) obj;
            for (int i = 0 ; i < tests.size();i++) {
                JSONObject jso = (JSONObject) tests.get(i);
                JSONObject common = (JSONObject) jso.get("common");
                String description = (String) common.get("description");
                JSONObject steps = (JSONObject) jso.get("steps");
                boolean ssl = (Boolean) common.get("ssl");
                String server = (String) common.get("server");
                String clientStr = (String) common.get("client");
                if (!client.equals(clientStr)) {
                    System.out.println("Skipping Test Due to Client Mismatch : " + clientStr);
                    continue;
                }
                String origin = "pubsub";
                String subkey = (String) ((JSONObject)(keysets.get(common.get("keyset")))).get("subKey") ;
                long time = new Date().getTime();
                String channelA = "A-java-" + i + 1 + "-" + time;
                String channelB = "B-java-" + i + 1 + "-"+ time;


                Test t = new Test(i + 1, description, subkey, origin, channelA, channelB, ssl, steps);
                testsList.add(t);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println("Total Number of Tests : " + testsList.size());
        for (int i = 0; i < testsList.size(); i++) {
            System.out.println("Running Test : " + (i + 1));
            testsList.get(i).run();
            testsList.get(i).printResult();
        }
        System.out.println("++++++++ RESULTS ++++++++");
        for (int i = 0; i < testsList.size(); i++) {
            testsList.get(i).printResult();
        }
    }

}
