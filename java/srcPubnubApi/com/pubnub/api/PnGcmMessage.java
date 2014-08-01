package com.pubnub.api;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Message object for GCM
 * @author Pubnub
 *
 */
public class PnGcmMessage extends JSONObject {

    /**
     * Constructor for PnGcmMessage
     */
    public PnGcmMessage() {
        super();
    }

    /**
     * Constructor for PnGcmMessage
     * @param json
     *         json object to be set as data for GCM message
     */
    public PnGcmMessage(JSONObject json) {
        super();
        setData(json);
    }

    /**
     * Set Data for PnGcmMessage
     * @param json
     *         json object to be set as data for GCM message
     */
    public void setData(JSONObject json) {
        try {
            this.put("data", json);
        } catch (JSONException e) {

        }
    }
}
