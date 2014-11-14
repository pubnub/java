package com.pubnub.api;

import java.util.Hashtable;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;

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
    public Pubnub(String publish_key, String subscribe_key, String secret_key,
                  String cipher_key, boolean ssl_on) {
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
    public Pubnub(String publish_key, String subscribe_key, String secret_key,
                  boolean ssl_on) {
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

    public Pubnub(String publish_key, String subscribe_key,
                  String secret_key, String cipher_key, boolean ssl_on, String initialization_vector) {
        super(publish_key, subscribe_key, secret_key, cipher_key, ssl_on, initialization_vector);
    }

    protected String getUserAgent() {
        return "(Android " + android.os.Build.VERSION.RELEASE +
               "; " + android.os.Build.MODEL +
               " Build) PubNub-Java/Android/" + VERSION;
    }


    /**
     * Enable Push Notifications (Google Cloud Messaging)
     * @param channel
     *             Channel for which to enable push notifications
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     */
    public void enablePushNotificationsOnChannel(String channel, String gcmRegistrationId) {
        enablePushNotificationsOnChannels(new String[]{channel}, gcmRegistrationId, null);
    }

    /**
     * Enable Push Notifications (Google Cloud Messaging)
     * @param channels
     *             Channels for which to enable push notifications
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     */
    public void enablePushNotificationsOnChannels(String[] channels, String gcmRegistrationId) {
        enablePushNotificationsOnChannels(channels, gcmRegistrationId, null);
    }

    /**
     * Enable Push Notifications (Google Cloud Messaging)
     * @param channel
     *             Channel for which to enable push notifications
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     * @param callback
     *             Callback object
     */
    public void enablePushNotificationsOnChannel(String channel, String gcmRegistrationId, Callback callback) {
        enablePushNotificationsOnChannels(new String[]{channel}, gcmRegistrationId, callback);
    }


    /**
     * Enable Push Notifications (Google Cloud Messaging)
     * @param channels
     *             Channels for which to enable push notifications
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     * @param callback
     *             Callback object
     */
    public void enablePushNotificationsOnChannels(final String[] channels, String gcmRegistrationId, final Callback callback) {
        final Callback cb = getWrappedCallback(callback);

        Hashtable parameters = PubnubUtil.hashtableClone(params);
        String[] urlargs = null;
        urlargs = new String[]{ getPubnubUrl(), "v1", "push", "sub-key",
                this.SUBSCRIBE_KEY, "devices", gcmRegistrationId
        };

        parameters.put("type", "gcm");
        parameters.put("add", PubnubUtil.joinString(channels, ","));

        HttpRequest hreq = new HttpRequest(urlargs, parameters,
                new ResponseHandler() {
            public void handleResponse(HttpRequest hreq, String response) {
                JSONArray jsarr;
                try {
                    jsarr = new JSONArray(response);
                } catch (JSONException e) {
                    handleError(hreq,
                            PubnubError.getErrorObject(PubnubError.PNERROBJ_INVALID_JSON, 1, response));
                    return;
                }
                if (callback != null) {
                    callback.successCallback("", jsarr);
                }
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                if (callback != null) {
                    callback.errorCallback("", error);
                }
                return;
            }
        });

        _request(hreq, nonSubscribeManager);
    }

    /**
     * Disable Push Notifications (Google Cloud Messaging)
     * @param channel
     *             Channel for which to disable push notifications
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     */
    public void disablePushNotificationsOnChannel(String channel, String gcmRegistrationId) {
        disablePushNotificationsOnChannels(new String[]{channel}, gcmRegistrationId, null);
    }

    /**
     * Disable Push Notifications (Google Cloud Messaging)
     * @param channels
     *             Channels for which to disable push notifications
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     */
    public void disablePushNotificationsOnChannels(String[] channels, String gcmRegistrationId) {
        disablePushNotificationsOnChannels(channels, gcmRegistrationId, null);
    }

    /**
     * Disable Push Notifications (Google Cloud Messaging)
     * @param channel
     * @param gcmRegistrationId
     * @param callback
     */
    public void disablePushNotificationsOnChannel(String channel, String gcmRegistrationId, Callback callback) {
        disablePushNotificationsOnChannels(new String[]{channel}, gcmRegistrationId, callback);
    }

    /**
     * Disable Push Notifications (Google Cloud Messaging)
     * @param channel
     * @param callback
     */
    /**
     * @param channels
     *             Channels for which to disable push notifications
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     * @param callback
     *             Callback object
     */
    public void disablePushNotificationsOnChannels(final String[] channels, String gcmRegistrationId, final Callback callback) {
        final Callback cb = getWrappedCallback(callback);


        Hashtable parameters = PubnubUtil.hashtableClone(params);
        String[] urlargs = null;
        urlargs = new String[]{ getPubnubUrl(), "v1", "push", "sub-key",
                this.SUBSCRIBE_KEY, "devices", gcmRegistrationId
        };


        parameters.put("type", "gcm");
        parameters.put("remove", PubnubUtil.joinString(channels, ","));

        HttpRequest hreq = new HttpRequest(urlargs, parameters,
                new ResponseHandler() {
            public void handleResponse(HttpRequest hreq, String response) {
                JSONArray jsarr;
                try {
                    jsarr = new JSONArray(response);
                } catch (JSONException e) {
                    handleError(hreq,
                            PubnubError.getErrorObject(PubnubError.PNERROBJ_INVALID_JSON, 1, response));
                    return;
                }
                if (callback != null) {
                    callback.successCallback("", jsarr);
                }
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                if (callback != null) {
                    callback.errorCallback("", error);
                }
                return;
            }
        });

        _request(hreq, nonSubscribeManager);
    }


    /**
     * Get channels for which push notification is enabled (Google Cloud Messaging)
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     * @param callback
     *             Callback object
     */
    public void requestPushNotificationEnabledChannelsForDeviceRegistrationId(String gcmRegistrationId, final Callback callback) {
        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);
        String[] urlargs = null;
        urlargs = new String[]{ getPubnubUrl(), "v1", "push", "sub-key",
                this.SUBSCRIBE_KEY, "devices", gcmRegistrationId
        };


        parameters.put("type", "gcm");

        HttpRequest hreq = new HttpRequest(urlargs, parameters,
                new ResponseHandler() {
            public void handleResponse(HttpRequest hreq, String response) {
                JSONArray jsarr;
                try {
                    jsarr = new JSONArray(response);
                } catch (JSONException e) {
                    handleError(hreq,
                            PubnubError.getErrorObject(PubnubError.PNERROBJ_INVALID_JSON, 1, response));
                    return;
                }
                callback.successCallback("", jsarr);
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                callback.errorCallback("", error);
                return;
            }
        });
    }

    /**
     * Disable push notifications for all channels (Google Cloud Messaging)
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     */
    public void removeAllPushNotificationsForDeviceRegistrationId(String gcmRegistrationId) {
        removeAllPushNotificationsForDeviceRegistrationId(gcmRegistrationId, null);
    }

    /**
     * Disable push notifications for all channels (Google Cloud Messaging)
     * @param gcmRegistrationId
     *             Google Cloud Messaging registration id
     * @param callback
     *             Callback object
     */
    public void removeAllPushNotificationsForDeviceRegistrationId(String gcmRegistrationId, final Callback callback) {
        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);
        String[] urlargs = null;
        urlargs = new String[]{ getPubnubUrl(), "v1", "push", "sub-key",
                this.SUBSCRIBE_KEY, "devices", gcmRegistrationId, "remove"
        };


        parameters.put("type", "gcm");

        HttpRequest hreq = new HttpRequest(urlargs, parameters,
                new ResponseHandler() {
            public void handleResponse(HttpRequest hreq, String response) {
                JSONArray jsarr;
                try {
                    jsarr = new JSONArray(response);
                } catch (JSONException e) {
                    handleError(hreq,
                            PubnubError.getErrorObject(PubnubError.PNERROBJ_INVALID_JSON, 1, response));
                    return;
                }
                if (callback != null) {
                    callback.successCallback("", jsarr);
                }
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                if (callback != null) {
                    callback.errorCallback("", error);
                }
                return;
            }
        });
    }
}
