package com.pubnub.examples;

import org.apache.commons.cli.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pubnub.api.Callback;
import com.pubnub.api.PnApnsMessage;
import com.pubnub.api.PnGcmMessage;
import com.pubnub.api.PnMessage;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;
import com.pubnub.api.PubnubException;

public class PubnubPushSampleCode {

    private static void usage(Options options) {

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("Publisher", options);
    }

    public void start() {

    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Options options = new Options();
        String publish_key = "demo-36";
        String subscribe_key = "demo-36";
        boolean nativ = false;
        boolean gcm = false;
        boolean apns = false;

        String channel = "demo";

        String origin = "gcm-beta";

        String auth_key = "";

        CommandLine cmd = null;

        options.addOption(OptionBuilder.hasArg().withArgName("String")
                .withLongOpt("publish_key").withType(String.class)
                .withDescription("Publish Key ( default: 'demo-36' )").create());

        options.addOption(OptionBuilder.hasArg().withArgName("String")
                .withLongOpt("subscribe_key").withType(String.class)
                .withDescription("Subscribe Key ( default: 'demo-36' )")
                .create());

        options.addOption(OptionBuilder.hasArg().withArgName("String")
                .withLongOpt("origin").withType(String.class)
                .withDescription("Origin ( Ex. pubsub )").create());

        options.addOption(OptionBuilder.hasArg().withArgName("String")
                .withLongOpt("auth_key").withType(String.class)
                .withDescription("Auth Key").create());

        options.addOption(OptionBuilder.hasArg().withArgName("String")
                .withLongOpt("channel").withType(String.class)
                .withDescription("Secret Key ( default: 'my_channel' )")
                .create());

        options.addOption(OptionBuilder.withLongOpt("apns")
                .withDescription("APNS message").create());

        options.addOption(OptionBuilder.withLongOpt("gcm")
                .withDescription("GCM message").create());

        options.addOption(OptionBuilder.withLongOpt("native")
                .withDescription("Native message").create());

        CommandLineParser parser = new BasicParser();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e1) {
            usage(options);
            return;
        }

        if (cmd.hasOption("publish_key")) {
            try {
                publish_key = cmd.getOptionValue("publish_key");
            } catch (Exception e) {
                e.printStackTrace();
                usage(options);
                return;
            }
        }

        if (cmd.hasOption("subscribe_key")) {
            try {
                subscribe_key = cmd.getOptionValue("subscribe_key");
            } catch (Exception e) {
                e.printStackTrace();
                usage(options);
                return;
            }
        }

        if (cmd.hasOption("origin")) {
            try {
                origin = cmd.getOptionValue("origin");
            } catch (Exception e) {
                e.printStackTrace();
                usage(options);
                return;
            }
        }

        if (cmd.hasOption("channel")) {
            try {
                channel = cmd.getOptionValue("channel");
            } catch (Exception e) {
                e.printStackTrace();
                usage(options);
                return;
            }
        }

        if (cmd.hasOption("auth_key")) {
            try {
                auth_key = cmd.getOptionValue("auth_key");
            } catch (Exception e) {
                e.printStackTrace();
                usage(options);
                return;
            }
        }

        if (cmd.hasOption("apns")) {
            try {
                apns = true;
            } catch (Exception e) {
                e.printStackTrace();
                usage(options);
                return;
            }
        }

        if (cmd.hasOption("gcm")) {
            try {
                gcm = true;
            } catch (Exception e) {
                e.printStackTrace();
                usage(options);
                return;
            }
        }

        if (cmd.hasOption("native")) {
            try {
                nativ = true;
            } catch (Exception e) {
                e.printStackTrace();
                usage(options);
                return;
            }
        }

        final Pubnub pubnub = new Pubnub(publish_key, subscribe_key);
        pubnub.setAuthKey(auth_key);
        pubnub.setCacheBusting(false);
        pubnub.setOrigin(origin);

        // Create APNS message

        PnApnsMessage apnsMessage = new PnApnsMessage();
        apnsMessage.setApsAlert("Game update 49ers touchdown");
        apnsMessage.setApsBadge(2);

        try {
            apnsMessage.put("teams", new JSONArray().put("49ers")
                    .put("raiders"));
            apnsMessage.put("score", new JSONArray().put(7).put(0));
        } catch (JSONException e1) {

        }

        // Create GCM Message

        PnGcmMessage gcmMessage = new PnGcmMessage();

        JSONObject jso = new JSONObject();
        try {
            jso.put("summary", "Game update 49ers touchdown");
            jso.put("lastplay", "5yd run up the middle");
        } catch (JSONException e) {

        }

        gcmMessage.setData(jso);

        Callback callback = new Callback() {
            @Override
            public void successCallback(String channel, Object response) {
                System.out.println(response);
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                System.out.println(error);
            }
        };

        PnMessage message = null;

        if (apns && gcm) {
            message = new PnMessage(pubnub, channel, callback, apnsMessage,
                    gcmMessage);
        } else if (apns) {
            message = new PnMessage(pubnub, channel, callback, apnsMessage);
        } else if (gcm) {
            message = new PnMessage(pubnub, channel, callback, gcmMessage);
        }
        if (message == null)
            message = new PnMessage(pubnub, channel, callback);
        if (nativ) {
            try {
                message.put("test", "hi");
            } catch (JSONException e1) {

            }
        }

        try {
            message.publish();
        } catch (PubnubException e) {
            switch (e.getPubnubError().errorCode) {
            case PubnubError.PNERR_CHANNEL_MISSING:
                System.out.println("Channel name not set");
                break;
            case PubnubError.PNERR_CONNECTION_NOT_SET:
                System.out.println("Pubnub object not set");
                break;
            }

        }

        // alternate way

        if (apns && gcm) {
            message = new PnMessage(apnsMessage, gcmMessage);
        } else if (apns) {
            message = new PnMessage(apnsMessage);
        } else if (gcm) {
            message = new PnMessage(gcmMessage);
        }

        pubnub.publish(channel, message, callback);

        // Another way

        PnMessage pnm = new PnMessage();

        try {
            pnm.put("hello world", "foo");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pnm.setCallback(callback);
        pnm.setChannel(channel);
        pnm.setPubnub(pubnub);

        try {
            pnm.publish();
        } catch (PubnubException e) {
            switch (e.getPubnubError().errorCode) {
            case PubnubError.PNERR_CHANNEL_MISSING:
                System.out.println("Channel name not set");
                break;
            case PubnubError.PNERR_CONNECTION_NOT_SET:
                System.out.println("Pubnub object not set");
                break;
            }

        }

    }

}
