package com.pubnub.api;

import java.util.Hashtable;
import java.util.UUID;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;



/**
 * Pubnub object facilitates querying channels for messages and listening on
 * channels for presence/message events
 *
 * @author Pubnub
 *
 */

public class Pubnub extends PubnubCoreShared {

    /**
     * Pubnub Constructor
     *
     * @param publish_key
     *            Publish Key
     * @param subscribe_key
     *            Subscribe Key
     * @param secret_key
     *            Secret Key
     * @param cipher_key
     *            Cipher Key
     * @param ssl_on
     *            SSL on ?
     */
    public Pubnub(String publish_key, String subscribe_key, String secret_key, String cipher_key, boolean ssl_on) {
        super(publish_key, subscribe_key, secret_key, cipher_key, ssl_on);
    }

    /**
     * Pubnub Constructor
     *
     * @param publish_key
     *            Publish key
     * @param subscribe_key
     *            Subscribe Key
     * @param secret_key
     *            Secret Key
     * @param ssl_on
     *            SSL on ?
     */
    public Pubnub(String publish_key, String subscribe_key, String secret_key, boolean ssl_on) {
        super(publish_key, subscribe_key, secret_key, "", ssl_on);
    }

    /**
     * Pubnub Constructor
     *
     * @param publish_key
     *            Publish Key
     * @param subscribe_key
     *            Subscribe Key
     */
    public Pubnub(String publish_key, String subscribe_key) {
        super(publish_key, subscribe_key, "", "", false);
    }

    /**
     * @param publish_key
     *            Publish Key
     * @param subscribe_key
     *            Subscribe Key
     * @param ssl
     */
    public Pubnub(String publish_key, String subscribe_key, boolean ssl) {
        super(publish_key, subscribe_key, "", "", ssl);
    }

    /**
     * @param publish_key
     * @param subscribe_key
     * @param secret_key
     */
    public Pubnub(String publish_key, String subscribe_key, String secret_key) {
        super(publish_key, subscribe_key, secret_key, "", false);
    }

    /**
     * @param publish_key
     * @param subscribe_key
     * @param secret_key
     * @param cipher_key
     */
    public Pubnub(String publish_key, String subscribe_key, String secret_key, String cipher_key) {
        super(publish_key, subscribe_key, secret_key, cipher_key, false);
    }

    /**
     *
     * Constructor for Pubnub Class
     *
     * @param publish_key
     *            Publish Key
     * @param subscribe_key
     *            Subscribe Key
     * @param secret_key
     *            Secret Key
     * @param cipher_key
     *            Cipher Key
     * @param ssl_on
     *            SSL enabled ?
     * @param initialization_vector
     *            Initialization vector
     */

    public Pubnub(String publish_key, String subscribe_key, String secret_key, String cipher_key, boolean ssl_on,
            String initialization_vector) {
        super(publish_key, subscribe_key, secret_key, cipher_key, ssl_on, initialization_vector);
    }

    /**
     *
     * Constructor for Pubnub Class from XML
     *
     * @param context
     *            Android Activity context
     * @param xmlResourceId
     *            The resource ID of an XML file describing PubNub configurations (e.g. R.xml.pn_config)
     */

    public Pubnub(Context context, int xmlResourceId) {
        String publish_key=null;
        String subscribe_key=null;
        String secret_key="";
        String cipher_key="";
        boolean ssl_on=false;

        String currentTag=null;
        try {
            XmlResourceParser xpp = context.getResources().getXml(xmlResourceId);
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("resources")){
                        eventType = xpp.next();
                        continue;
                    }
                    currentTag = xpp.getAttributeValue(null, "name");
                } else if (eventType == XmlPullParser.TEXT) {
                    if (currentTag == null){ // Ignore improper formatting
                        eventType = xpp.next();
                        continue;
                    }
                    if (currentTag.equals("pn_pubKey")){
                        publish_key = xpp.getText();
                    } else if (currentTag.equals("pn_subKey")) {
                        subscribe_key = xpp.getText();
                    } else if (currentTag.equals("pn_secretKey")) {
                        secret_key = xpp.getText();
                    } else if (currentTag.equals("pn_cipherKey")) {
                        cipher_key = xpp.getText();
                    } else if (currentTag.equals("pn_sslOn")){
                        ssl_on = Boolean.parseBoolean(xpp.getText());
                    }
                }
                eventType = xpp.next();
            }
        }
        catch (XmlPullParserException e){ e.printStackTrace(); }
        catch (IOException e){ e.printStackTrace(); }

        super(publish_key, subscribe_key, secret_key, cipher_key, ssl_on);
    }

    protected String getUserAgent() {
        return "(Android " + android.os.Build.VERSION.RELEASE + "; " + android.os.Build.MODEL
                + " Build) PubNub-Java/Android/" + VERSION;
    }

}
