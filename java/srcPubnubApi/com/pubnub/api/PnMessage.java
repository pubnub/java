package com.pubnub.api;

import org.json.JSONException;
import org.json.JSONObject;




/**
 * Pubnub Message Object
 * @author Pubnub
 *
 */
public class PnMessage extends JSONObject {
    private String channel;
    private Callback callback;
    private Pubnub pubnub;


    /**
     * Constructor for Pubnub Message Class
     * @param pubnub
     *         Pubnub object
     * @param channel
     *         Channel name
     * @param callback
     *         Callback object
     */
    public PnMessage(Pubnub pubnub, String channel, Callback callback) {
        super();
        this.channel  = channel;
        this.callback = callback;
        this.pubnub = pubnub;
    }

    /**
     * Constructor for Pubnub Message Class
     */
    public PnMessage() {
        super();
    }

    /**
     * Constructor for Pubnub Message Class
     * @param apnsMsg
     *         Pubnub APNS message object
     * @param gcmMsg
     *         Pubnub GCM message object
     */
    public PnMessage(PnApnsMessage apnsMsg, PnGcmMessage gcmMsg) {
        super();
        try {
            if (apnsMsg != null) {
                this.put("pn_apns", apnsMsg);
            }
            if (gcmMsg != null) {
                this.put("pn_gcm", gcmMsg);
            }
        } catch (JSONException e) {

        }
    }

    /**
     * Constructor for Pubnub Message Class
     * @param pubnub
     *         Pubnub object
     * @param callback
     *         Callback object
     * @param apnsMsg
     *         Pubnub APNS message object
     * @param gcmMsg
     *         Pubnub GCM message object
     */
    public PnMessage(Pubnub pubnub, String channel, Callback callback, PnApnsMessage apnsMsg, PnGcmMessage gcmMsg) {
        super();
        this.channel = channel;
        this.callback = callback;
        this.pubnub = pubnub;
        try {
            if (apnsMsg != null) {
                this.put("pn_apns", apnsMsg);
            }
            if (gcmMsg != null) {
                this.put("pn_gcm", gcmMsg);
            }
        } catch (JSONException e) {

        }
    }

    /**
     * Getter for channel set on PnMessage Object
     * @return channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * Setter for channel on PnMessage Object
     * @param channel
     *             Channel name
     */
    public void setChannel(String channel) {
        this.channel = channel;
    }

    /**
     * Getter for callback set on PnMessage object
     * @return  callback
     *
     */
    public Callback getCallback() {
        return callback;
    }

    /**
     * Setter for callback on PnMessage object
     * @param callback
     *             Callback
     */
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * Getter for pubnub set on PnMessage object
     * @return pubnub
     */
    public Pubnub getPubnub() {
        return pubnub;
    }

    /**
     * Setter for pubnub on PnMessage object
     * @param pubnub
     *           Pubnub object
     */
    public void setPubnub(Pubnub pubnub) {
        this.pubnub = pubnub;
    }

    /**
     * Constructor for Pubnub Message Class
     * @param gcmMsg
     *         Pubnub GCM message object
     */
    public PnMessage(PnGcmMessage gcmMsg) {
        super();
        try {
            if (gcmMsg != null) {
                this.put("pn_gcm", gcmMsg);
            }
        } catch (JSONException e) {

        }
    }

    /**
     * Constructor for Pubnub Message Class
     * @param pubnub
     *         Pubnub
     * @param channel
     *         Channel
     * @param callback
     *         Callback object
     * @param gcmMsg
     *         Pubnub GCM message object
     */
    public PnMessage(Pubnub pubnub, String channel, Callback callback, PnGcmMessage gcmMsg) {
        super();
        this.channel = channel;
        this.callback = callback;
        this.pubnub = pubnub;
        try {
            if (gcmMsg != null) {
                this.put("pn_gcm", gcmMsg);
            }
        } catch (JSONException e) {

        }
    }

    /**
     * Constructor for Pubnub Message Class
     * @param apnsMsg
     *         Pubnub APNS message object
     */
    public PnMessage(PnApnsMessage apnsMsg) {
        super();
        try {
            if (apnsMsg != null) {
                this.put("pn_apns", apnsMsg);
            }
        } catch (JSONException e) {

        }
    }

    /**
     * Constructor for Pubnub Message Class
     * @param pubnub
     *         Pubnub
     * @param channel
     *         Channel
     * @param callback
     *         Callback object
     * @param apnsMsg
     *         Pubnub APNS message object
     */
    public PnMessage(Pubnub pubnub, String channel, Callback callback, PnApnsMessage apnsMsg) {
        super();
        this.channel = channel;
        this.callback = callback;
        this.pubnub = pubnub;
        try {
            if (apnsMsg != null) {
                this.put("pn_apns", apnsMsg);
            }
        } catch (JSONException e) {

        }
    }

    /**
     * Publish Message
     * @param pubnub
     *         Pubnub object
     * @param channel
     *         Channel
     * @param callback
     *         Callback object
     * @throws PubnubException
     *         Exception if either channel or pubnub object is not set
     */
    public void publish(Pubnub pubnub, String channel, Callback callback) throws PubnubException  {
        this.channel = channel;
        this.callback = callback;
        this.pubnub = pubnub;
        if (this.channel == null) {
            throw new PubnubException(PubnubError.PNERROBJ_CHANNEL_MISSING);
        }
        if (this.pubnub == null) {
            throw new PubnubException(PubnubError.PNERROBJ_CONNECTION_NOT_SET);
        }
        pubnub.publish(channel, this, callback);
    }

    /**
     * Publish Message
     * @throws PubnubException
     *         Exception if either channel or pubnub object is not set
     */
    public void publish() throws PubnubException {
        if (this.channel == null) {
            throw new PubnubException(PubnubError.PNERROBJ_CHANNEL_MISSING);
        }
        if (this.pubnub == null) {
            throw new PubnubException(PubnubError.PNERROBJ_CONNECTION_NOT_SET);
        }
        pubnub.publish(channel, this, callback);
    }

}
