package com.pubnub.api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Message object for APNS
 * @author Pubnub
 *
 */
public class PnApnsMessage extends JSONObject {

    /**
     * Constructor for APNS message object
     */
    public PnApnsMessage() {
        super();
    }

    private JSONObject getAps() {
        JSONObject aps = null;
        try {
            aps = (JSONObject) this.get("aps");
        } catch (JSONException e) {

        }

        if (aps == null) {
            aps = new JSONObject();
            try {
                this.put("aps", aps);
            } catch (JSONException e) {

            }
        }
        return aps;
    }
     /**
      * Set value of APS alert
      * @param alert
      *         String to be set as alert value for APNS message
      */
     public void setApsAlert(String alert) {

        try {
            JSONObject aps = (JSONObject) getAps();
            aps.put("alert", alert);
        } catch (JSONException e) {

        }

    }
    /**
     * Set value of APS badge
     * @param badge
     *         int to be set as badge value for APNS message
     */
    public void setApsBadge(int badge) {
        try {
            JSONObject aps = (JSONObject) (JSONObject) getAps();
            aps.put("badge", badge);
        } catch (JSONException e) {

        }

    }
}
