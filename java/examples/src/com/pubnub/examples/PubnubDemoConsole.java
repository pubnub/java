package com.pubnub.examples;

import com.pubnub.api.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Hashtable;
import java.util.Scanner;

import static java.lang.System.out;

public class PubnubDemoConsole {

    Pubnub pubnub;
    String publish_key = "demo";
    String subscribe_key = "demo";
    String secret_key = "";
    String cipher_key = "";
    boolean SSL;
    Scanner reader;

    public PubnubDemoConsole(String publish_key, String subscribe_key, String secret_key,
                             String cipher_key) {
        this.publish_key = publish_key;
        this.subscribe_key = subscribe_key;
        this.secret_key = secret_key;
        this.cipher_key = cipher_key;
    }

    public PubnubDemoConsole() {

    }

    private void notifyUser(Object message) {
        out.println(message.toString());
    }

    private void publish(String channel, boolean store) {
        notifyUser("Enter the message for publish. To exit loop enter QUIT");
        String message = "";

        Callback cb = new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("PUBLISH : " + message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("PUBLISH : " + error);
            }
        };

        while (true) {
            Hashtable args = new Hashtable(2);
            message = reader.nextLine();
            if (message.equalsIgnoreCase("QUIT")) {
                break;
            }

            try {
                Integer i = Integer.parseInt(message);
                pubnub.publish(channel, i, store, cb);
                continue;
            } catch (Exception e) {

            }
            try {
                Double d = Double.parseDouble(message);
                pubnub.publish(channel, d, store, cb);
                continue;
            } catch (Exception e) {

            }
            try {
                JSONArray js = new JSONArray(message);
                pubnub.publish(channel, js, store, cb);
                continue;
            } catch (Exception e) {

            }
            try {
                JSONObject js = new JSONObject(message);
                pubnub.publish(channel, js, store, cb);
                continue;
            } catch (Exception e) {

            }
            pubnub.publish(channel, message, store, cb);
        }

    }

    private void subscribe(final String channel) {

        try {
            pubnub.subscribe(channel, new Callback() {

                @Override
                public void connectCallback(String channel, Object message) {
                    notifyUser("SUBSCRIBE : CONNECT on channel:" + channel
                               + " : " + message.getClass() + " : "
                               + message.toString());
                }

                @Override
                public void disconnectCallback(String channel, Object message) {
                    notifyUser("SUBSCRIBE : DISCONNECT on channel:" + channel
                               + " : " + message.getClass() + " : "
                               + message.toString());
                }

                public void reconnectCallback(String channel, Object message) {
                    notifyUser("SUBSCRIBE : RECONNECT on channel:" + channel
                               + " : " + message.getClass() + " : "
                               + message.toString());
                }

                @Override
                public void successCallback(String channel, Object message, String timetoken) {
                    //notifyUser("SUBSCRIBE : " + channel + " : "
                     //          + message.getClass() + " : " + message.toString());
                    if ( message instanceof JSONObject) {
                        notifyUser("SUBSCRIBE : " + channel + " : "
                                + message.getClass() + " : " + message.toString());
                         try {
                            notifyUser( "TIMETOKEN: " + timetoken  + ", "+  ((JSONObject)message).getString("data")) ;
                            notifyUser( "TIMETOKEN: " + timetoken  + ", "+  ((JSONObject)message).getString("data2")) ;
                            notifyUser( "TIMETOKEN: " + timetoken  + ", "+  ((JSONObject)message).getString("data3")) ;
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    } else {
                        System.out.println("TIMETOKEN: " + timetoken  + ", "+  "Message not a json object : " + message);
                    }


                }

                @Override
                public void errorCallback(String channel, PubnubError error) {

                    /*

                    # Switch on error code, see PubnubError.java

                    if (error.errorCode == 112) {
                        # Bad Auth Key!
                        unsubscribe, get a new auth key, subscribe, etc...
                    } else if (error.errorCode == 113) {
                        # Need to set Auth Key !
                        unsubscribe, set auth, resubscribe
                    }

                    */

                    notifyUser("SUBSCRIBE : ERROR on channel " + channel
                               + " : " + error.toString());
                    if (error.errorCode == PubnubError.PNERR_TIMEOUT)
                        pubnub.disconnectAndResubscribe();
                }
            });

        } catch (Exception e) {
        }
    }

    private void presence(String channel) {
        try {
            pubnub.presence(channel, new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    notifyUser("PRESENCE : " + message);
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    notifyUser("PRESENCE : " + error);
                }
            });
        } catch (PubnubException e) {

        }
    }

    private void history(String channel, int count, boolean includeToken) {
        pubnub.history(channel, includeToken, count, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("HISTORY : " + message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("HISTORY : " + error);
            }
        });
    }

    private void hereNow(String channel) {
        boolean metadata = getBooleanFromConsole("Metadata");
        boolean disable_uuids = getBooleanFromConsole("Disable UUIDs");

        pubnub.hereNow(channel, metadata, disable_uuids, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("HERE NOW : " + message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("HERE NOW : " + error);
            }
        });
    }

    private void unsubscribe(String channel) {
        pubnub.unsubscribe(channel);
    }

    private void unsubscribePresence(String channel) {
        pubnub.unsubscribePresence(channel);
    }

    private void time() {
        pubnub.time(new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("TIME : " + message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("TIME : " + error);
            }
        });
    }

    private void disconnectAndResubscribe() {
        pubnub.disconnectAndResubscribe();

    }

    private void disconnectAndResubscribeWithTimetoken(String timetoken) {
        pubnub.disconnectAndResubscribeWithTimetoken(timetoken);

    }

    public void startDemo() {
        reader = new Scanner(System.in);
        notifyUser("HINT:\tTo test Re-connect and catch-up");
        notifyUser("\tDisconnect your machine from network/internet and");
        notifyUser("\tre-connect your machine after sometime");

        this.SSL = getBooleanFromConsole("SSL");

        if (this.publish_key.length() == 0) this.publish_key = getStringFromConsole("Publish Key");

        if (this.subscribe_key.length() == 0)  this.subscribe_key = getStringFromConsole("Subscribe Key");

        if (this.secret_key.length() == 0) this.secret_key = getStringFromConsole("Secret Key", true);

        if (this.cipher_key.length() == 0)  this.cipher_key = getStringFromConsole("Cipher Key", true);


        pubnub = new Pubnub(this.publish_key, this.subscribe_key, this.secret_key, this.cipher_key, this.SSL);

        displayMenuOptions();

        String channelName = null;
        int command = 0;
        while ((command = reader.nextInt()) != 9) {
            reader.nextLine();
            switch (command) {

            case 0:
                displayMenuOptions();
                break;

            case 1:
                channelName = getStringFromConsole("Subscribe: Enter Channel name");
                subscribe(channelName);

                notifyUser("Subscribed to following channels: ");
                notifyUser(PubnubUtil.joinString(
                               pubnub.getSubscribedChannelsArray(), " : "));
                break;
            case 2:
                channelName = getStringFromConsole("Channel Name");
                boolean store = getBooleanFromConsole("Store", true);
                publish(channelName, store);
                break;
            case 3:
                channelName = getStringFromConsole("Channel Name");
                presence(channelName);
                break;
            case 4:
                channelName = getStringFromConsole("Channel Name");
                int count = getIntFromConsole("Count");
                boolean includeToken = getBooleanFromConsole("Include Timetokens");
                history(channelName, count, includeToken);
                break;
            case 5:
                channelName = getStringFromConsole("Channel Name", true);
                hereNow(channelName);
                break;
            case 6:
                channelName = getStringFromConsole("Channel Name");
                unsubscribe(channelName);
                break;
            case 7:
                channelName = getStringFromConsole("Channel Name");
                unsubscribePresence(channelName);
                break;
            case 8:
                time();
                break;
            case 10:
                disconnectAndResubscribe();
                break;
            case 11:
                notifyUser("Disconnect and Resubscribe with timetoken : Enter timetoken");
                String timetoken = getStringFromConsole("Timetoken");
                disconnectAndResubscribeWithTimetoken(timetoken);
                break;
            case 12:
                pubnub.setResumeOnReconnect(pubnub.isResumeOnReconnect() ? false
                                            : true);
                notifyUser("RESUME ON RECONNECT : " + pubnub.isResumeOnReconnect());
                break;
            case 13:
                int maxRetries = getIntFromConsole("Max Retries");
                setMaxRetries(maxRetries);
                break;
            case 14:
                int retryInterval = getIntFromConsole("Retry Interval");
                setRetryInterval(retryInterval);
                break;
            case 15:
                int windowInterval = getIntFromConsole("Window Interval");
                setWindowInterval(windowInterval);
                break;
            case 16:
                int subscribeTimeout = getIntFromConsole("Subscribe Timeout ( in milliseconds) ");
                setSubscribeTimeout(subscribeTimeout);
                break;
            case 17:
                int nonSubscribeTimeout = getIntFromConsole("Non Subscribe Timeout ( in milliseconds) ");
                setNonSubscribeTimeout(nonSubscribeTimeout);
                break;
            case 18:
                notifyUser("Set/Unset Auth Key: Enter blank for unsetting key");
                String authKey = getStringFromConsole("Auth Key");
                pubnub.setAuthKey(authKey);
                break;
            case 19:
                pamGrant();
                break;
            case 20:
                pamRevoke();
                break;
            case 21:
                pamAudit();
                break;
            case 22:
                pubnub.setOrigin(getStringFromConsole("Origin"));
                break;
            case 23:
                pubnub.setDomain(getStringFromConsole("Domain"));
                break;
            case 24:
                pubnub.setCacheBusting(true);
                break;
            case 25:
                pubnub.setCacheBusting(false);
                break;
            case 26:
                notifyUser("Set UUID");
                String uuid = getStringFromConsole("UUID");
                pubnub.setUUID(uuid);
                break;
            case 27:
                int heartbeat = getIntFromConsole("Pubnub Presence Heartbeat ( in seconds ), Current value : " + pubnub.getHeartbeat());
                pubnub.setHeartbeat(heartbeat, new Callback(){

                    @Override
                    public void successCallback(String channel, Object message) {
                        System.out.println(System.currentTimeMillis() / 1000 + " : " + message);
                    }
                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                        System.out.println(System.currentTimeMillis() / 1000 + " : " + error);
                    }

                });
                break;
            case 28:
                int heartbeatInterval = getIntFromConsole("Pubnub Presence Heartbeat Interval ( in seconds ), Current value : " + pubnub.getHeartbeatInterval());
                pubnub.setHeartbeatInterval(heartbeatInterval, new Callback(){

                    @Override
                    public void successCallback(String channel, Object message) {
                        System.out.println(System.currentTimeMillis() / 1000 + " : " + message);
                    }
                    @Override
                    public void errorCallback(String channel, PubnubError error) {
                        System.out.println(System.currentTimeMillis() / 1000 + " : " + error);
                    }

                });
                break;
            case 29:
                getState();
                break;
            case 30:
                setState();
                break;
            case 31:
                String uid = getStringFromConsole("UUID", true);
                if (uid == null || uid.length() == 0) uid = pubnub.getUUID();
                whereNow(uid);
                break;
            default:
                notifyUser("Invalid Input");
            }
            displayMenuOptions();
        }
        notifyUser("Exiting");
        pubnub.shutdown();

    }

    private void whereNow(String uuid) {
        pubnub.whereNow(uuid, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("WHERE NOW : " + message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("WHERE NOW : " + error);
            }
        });
    }

    private void setState() {
        String channel = getStringFromConsole("Channel");
        String uuid = getStringFromConsole("UUID", true);
        if (uuid == null || uuid.length() == 0) uuid = pubnub.getUUID();
        JSONObject metadata = getJSONObjectFromConsole("Metadata");

        pubnub.setState(channel, uuid, metadata, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("SUBSCRIBER SET STATE : " + message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("SUBSCRIBER SET STATE : " + error);
            }
        });
    }

    private void getState() {
        String channel = getStringFromConsole("Channel");
        String uuid = getStringFromConsole("UUID", true);
        if (uuid == null || uuid.length() == 0) uuid = pubnub.getUUID();

        pubnub.getState(channel, uuid, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("SUBSCRIBER GET STATE : " + message);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("SUBSCRIBER GET STATE : " + error);
            }
        });
    }

    private String getStringFromConsole(String message, boolean optional) {

        int attempt_count = 0;
        String input = null;
        do {
            if (attempt_count > 0) System.out.print("Invalid input. ");
            String message1 = "Enter " + message ;
            message1 = (optional)?message1+" ( Optional input. You can skip by pressing enter )":message1;
            notifyUser(message1);
            input = reader.nextLine();
            attempt_count++;
        } while ((input == null || input.length() == 0) && !optional);
        notifyUser(message + " : " + input);
        return input;
    }

    private JSONObject getJSONObjectFromConsole(String message, boolean optional) {

        int attempt_count = 0;
        String input = null;
        JSONObject input_jso = null;
        do {
            if (attempt_count > 0) System.out.print("Invalid input. ");
            String message1 = "Enter " + message ;
            message1 = (optional)?message1+" ( Optional input. You can skip by pressing enter )":message1;
            notifyUser(message1);
            input = reader.nextLine();
            try {
                input_jso = new JSONObject(input);
            } catch (Exception e) {input_jso = null;}
            attempt_count++;
        } while ((input_jso == null || input_jso.length() == 0) && !optional);
        notifyUser(message + " : " + input_jso);
        return input_jso;
    }
    private JSONObject getJSONObjectFromConsole(String message) {
        return getJSONObjectFromConsole(message, false);
    }
    private String getStringFromConsole(String message) {
        return getStringFromConsole(message, false);
    }
    private int getIntFromConsole(String message, boolean optional) {

        int attempt_count = 0;
        String input = null;
        int returnVal = -1;
        do {
            if (attempt_count > 0) notifyUser("Invalid input. ");
            String message1 = "Enter " + message;
            message1 = (optional)?message1+" ( Optional input. You can skip by pressing enter ) ":message1;
            notifyUser(message1);
            input = reader.nextLine();
            attempt_count++;
            returnVal = Integer.parseInt(input);
        } while ((input == null || input.length() == 0 || returnVal < -1) && !optional);
        notifyUser(message + " : " + returnVal);
        return returnVal;
    }

    private int getIntFromConsole(String message) {
        return getIntFromConsole(message, false);
    }

    private boolean getBooleanFromConsole(String message, boolean optional) {

        int attempt_count = 0;
        String input = null;
        boolean returnVal = false;
        do {
            if (attempt_count > 0) notifyUser("Invalid input. ");
            String message1 = message + " ? ( Enter Yes/No or Y/N )";
            message1 = (optional)?message1+" ( Optional input. You can skip by pressing enter ) ":message1;
            notifyUser(message1);
            input = reader.nextLine();
            attempt_count++;
        } while ((input == null || input.length() == 0 ||
                  ( !input.equalsIgnoreCase("yes") && !input.equalsIgnoreCase("no") &&
                    !input.equalsIgnoreCase("y") && !input.equalsIgnoreCase("n"))) && !optional);
        returnVal =  (input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes"))?true:false;
        notifyUser(message + " : " + returnVal);
        return returnVal;
    }

    private boolean getBooleanFromConsole(String message) {
        return getBooleanFromConsole(message, false);
    }

    private void pamGrant() {
        String channel = getStringFromConsole("Channel");
        String auth_key = getStringFromConsole("Auth Key");
        boolean read = getBooleanFromConsole("Read");
        boolean write = getBooleanFromConsole("Write");
        int ttl = getIntFromConsole("TTL");

        pubnub.pamGrant(channel, auth_key, read, write, ttl, new Callback() {

            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("CHANNEL : " + channel + " , " + message.toString());

            }
            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("CHANNEL : " + channel + " , " + error.toString());
            }

        });
    }

    private void pamAudit() {
        String channel = getStringFromConsole("Channel", true);
        String auth_key = getStringFromConsole("Auth Key", true);

        Callback cb = new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("CHANNEL : " + channel + " , " + message.toString());

            }
            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("CHANNEL : " + channel + " , " + error.toString());
            }
        };

        if (channel != null && channel.length() > 0) {
            if (auth_key != null && auth_key.length() != 0) {
                pubnub.pamAudit(channel, auth_key, cb);
            } else {
                pubnub.pamAudit(channel, cb);
            }
        } else {
            pubnub.pamAudit(cb);
        }

    }

    private void pamRevoke() {
        String channel = getStringFromConsole("Enter Channel");
        String auth_key = getStringFromConsole("Auth Key");

        pubnub.pamRevoke(channel, auth_key, new Callback() {

            @Override
            public void successCallback(String channel, Object message) {
                notifyUser("CHANNEL : " + channel + " , " + message.toString());

            }
            @Override
            public void errorCallback(String channel, PubnubError error) {
                notifyUser("CHANNEL : " + channel + " , " + error.toString());
            }

        });
    }

    private void setMaxRetries(int maxRetries) {
        pubnub.setMaxRetries(maxRetries);
    }

    private void setRetryInterval(int retryInterval) {
        pubnub.setRetryInterval(retryInterval);
    }

    private void setWindowInterval(int windowInterval) {
        pubnub.setWindowInterval(windowInterval);
    }

    private void setSubscribeTimeout(int subscribeTimeout) {
        pubnub.setSubscribeTimeout(subscribeTimeout);
    }

    private void setNonSubscribeTimeout(int nonSubscribeTimeout) {
        pubnub.setNonSubscribeTimeout(nonSubscribeTimeout);
    }

    private void displayMenuOptions() {
        notifyUser("ENTER 1  FOR Subscribe "
                   + "(Currently subscribed to "
                   + this.pubnub.getCurrentlySubscribedChannelNames() + ")");
        notifyUser("ENTER 2  FOR Publish");
        notifyUser("ENTER 3  FOR Presence");
        notifyUser("ENTER 4  FOR History");
        notifyUser("ENTER 5  FOR Here Now");
        notifyUser("ENTER 6  FOR Unsubscribe");
        notifyUser("ENTER 7  FOR Presence-Unsubscribe");
        notifyUser("ENTER 8  FOR Time");
        notifyUser("ENTER 9  FOR EXIT OR QUIT");
        notifyUser("ENTER 10 FOR Disconnect-And-Resubscribe");
        notifyUser("ENTER 11 FOR Disconnect-And-Resubscribe with timetoken");
        notifyUser("ENTER 12 FOR Toggle Resume On Reconnect ( current: " + pubnub.getResumeOnReconnect() + " )");
        notifyUser("ENTER 13 FOR Setting MAX Retries ( current: " + pubnub.getMaxRetries() + " )");
        notifyUser("ENTER 14 FOR Setting Retry Interval ( current: " + pubnub.getRetryInterval() + " milliseconds )");
        notifyUser("ENTER 15 FOR Setting Window Interval ( current: " + pubnub.getWindowInterval() + " milliseconds )");
        notifyUser("ENTER 16 FOR Setting Subscribe Timeout ( current: " + pubnub.getSubscribeTimeout() + " milliseconds )");
        notifyUser("ENTER 17 FOR Setting Non Subscribe Timeout ( current: " + pubnub.getNonSubscribeTimeout() + " milliseconds )");
        notifyUser("ENTER 18 FOR Setting/Unsetting auth key ( current: " + pubnub.getAuthKey() + " )");
        notifyUser("ENTER 19 FOR PAM grant");
        notifyUser("ENTER 20 FOR PAM revoke");
        notifyUser("ENTER 21 FOR PAM Audit");
        notifyUser("ENTER 22 FOR Setting Origin ( current: " + pubnub.getOrigin() + " )");
        notifyUser("ENTER 23 FOR Setting Domain ( current: "+ pubnub.getDomain() + " )");
        notifyUser("ENTER 24 FOR Enabling Cache Busting  ( current: " + pubnub.getCacheBusting() + " )");
        notifyUser("ENTER 25 FOR Disabling Cache Busting ( current: " + pubnub.getCacheBusting() + " )");
        notifyUser("ENTER 26 FOR Setting UUID ( current: " + pubnub.getUUID() + " )");
        notifyUser("ENTER 27 FOR Setting Presence Heartbeat ( current: " + pubnub.getHeartbeat() + " )");
        notifyUser("ENTER 28 FOR Setting Presence Heartbeat Interval ( current: " + pubnub.getHeartbeatInterval() + " )");
        notifyUser("ENTER 29 FOR Getting Subscriber State");
        notifyUser("ENTER 30 FOR Setting Subscriber State");
        notifyUser("ENTER 31 FOR Where Now");
        notifyUser("\nENTER 0 to display this menu");
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        PubnubDemoConsole pdc;
        if (args.length == 4) {
            pdc = new PubnubDemoConsole(args[0], args[1], args[2], args[3]);
        } else
            pdc = new PubnubDemoConsole();
        pdc.startDemo();
    }

}
