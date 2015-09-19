package com.pubnub.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Random;


/**
 * Pubnub object facilitates querying channels for messages and listening on
 * channels for presence/message events
 *
 * @author Pubnub
 *
 */

abstract class PubnubCore {

    private String HOSTNAME = "pubsub";
    private int HOSTNAME_SUFFIX = 1;
    private String DOMAIN = "pubnub.com";
    private String ORIGIN_STR = null;
    protected String PUBLISH_KEY = "";
    protected String SUBSCRIBE_KEY = "";
    protected String SECRET_KEY = "";
    private String CIPHER_KEY = "";
    private String IV = null;
    private volatile String AUTH_STR = null;
    private volatile boolean CACHE_BUSTING = true;
    protected Hashtable params;
    private volatile boolean resumeOnReconnect;

    private boolean SSL = true;
    protected String UUID = null;

    private Subscriptions channelSubscriptions;
    private Subscriptions channelGroupSubscriptions;

    protected SubscribeManager subscribeManager;
    protected NonSubscribeManager nonSubscribeManager;
    protected TimedTaskManager timedTaskManager;
    private volatile String _timetoken = "0";
    private volatile String _saved_timetoken = "0";

    protected static String PRESENCE_SUFFIX = "-pnpres";
    protected static String WILDCARD_SUFFIX = "*";
    protected static String WILDCARD_PRESENCE_SUFFIX = WILDCARD_SUFFIX + PRESENCE_SUFFIX;
    protected static String VERSION = "";
    private Random generator = new Random();

    private static Logger log = new Logger(PubnubCore.class);

    protected abstract String getUserAgent();

    private int PRESENCE_HEARTBEAT_TASK = 0;
    private int HEARTBEAT = 320;
    private volatile int PRESENCE_HB_INTERVAL = 0;

    /**
     * This method when called stops Pubnub threads
     */
    public void shutdown() {
        nonSubscribeManager.stop();
        subscribeManager.stop();
        timedTaskManager.stop();
    }

    /**
     * This method returns the state of Resume on Reconnect setting
     *
     * @return Current state of Resume On Reconnect Setting
     */
    public boolean isResumeOnReconnect() {
        return resumeOnReconnect;
    }

    /**
     * This method sets retry interval for subscribe. Pubnub API will make
     * maxRetries attempts to connect to pubnub servers. These attemtps will be
     * made at an interval of retryInterval milliseconds.
     *
     * @param retryInterval
     *            Retry Interval in milliseconds
     */
    public void setRetryInterval(int retryInterval) {
        subscribeManager.setRetryInterval(retryInterval);
    }

    /**
     * This method sets window interval for subscribe.
     *
     * @param windowInterval
     *            Window Interval in milliseconds
     */
    public void setWindowInterval(int windowInterval) {
        subscribeManager.setWindowInterval(windowInterval);
    }

    /**
     * Returns current retry interval for subscribe
     * @return Current Retry Interval in milliseconds
     */
    public int getRetryInterval() {
        return subscribeManager.retryInterval;
    }

    /**
     * Returns current window interval for subscribe
     * @return Current Window Interval in milliseconds
     */
    public int getWindowInterval() {
        return subscribeManager.windowInterval;
    }

    String[] getPresenceHeartbeatUrl() {
        String channelString = channelSubscriptions.getItemStringNoPresence();

        if (channelString.length() <= 0) {
            return null;
        }
        return new String[]{ getPubnubUrl(), "v2", "presence", "sub-key",
                this.SUBSCRIBE_KEY, "channel",
                PubnubUtil.urlEncode(channelString), "heartbeat"
        };
    }

    private String getState() {
        return (channelSubscriptions.state.length() > 0) ? channelSubscriptions.state.toString() : null;
    }

    class PresenceHeartbeatTask extends TimedTask {
        private Callback callback;

        PresenceHeartbeatTask(int interval, Callback callback) {
            super(interval);
            this.callback = callback;
        }

        public void run() {

            String[] urlComponents = getPresenceHeartbeatUrl();
            if (urlComponents == null)
                return;
            //String[] urlComponents = { getPubnubUrl(), "time", "0"};

            Hashtable parameters = PubnubUtil.hashtableClone(params);
            if (parameters.get("uuid") == null)
                parameters.put("uuid", UUID);

            String st  = getState();
            if (st != null) parameters.put("state", st);

            if (HEARTBEAT > 0 && HEARTBEAT < 320 ) parameters.put("heartbeat", String.valueOf(HEARTBEAT));

            HttpRequest hreq = new HttpRequest(urlComponents, parameters,
                    new ResponseHandler() {
                        public void handleResponse(HttpRequest hreq, String response) {
                            JSONObject jso;
                            try {
                                jso = new JSONObject(response);
                                response = jso.getString("message");
                            } catch (JSONException e) {
                                handleError(
                                        hreq,
                                        PubnubError.getErrorObject(
                                                PubnubError.PNERROBJ_INVALID_JSON, 1, response
                                        )
                                );
                                return;
                            }
                            callback.successCallback(
                                    channelSubscriptions.getItemStringNoPresence(),
                                    response
                            );
                        }

                        public void handleError(HttpRequest hreq, PubnubError error) {
                            callback.errorCallback(channelSubscriptions.getItemStringNoPresence(), error);
                        }
                    });

            _request(hreq, nonSubscribeManager);

        }

    }

    /**
     * This method sets presence expiry timeout.
     *
     * @param pnexpires
     *            Presence Expiry timeout in seconds
     */
    public void setPnExpires(int pnexpires, Callback callback) {
        setHeartbeat(pnexpires, callback);
    }

    /**
     * This method sets presence expiry timeout.
     *
     * @param heartbeat
     *            Presence Heartbeat value in seconds
     */
    public void setHeartbeat(int heartbeat, Callback callback) {
        Callback cb = getWrappedCallback(callback);

        HEARTBEAT = (heartbeat > 0 && heartbeat < 5)?5:heartbeat;
        if (PRESENCE_HB_INTERVAL == 0) {
            PRESENCE_HB_INTERVAL = (HEARTBEAT - 3 >= 1)?HEARTBEAT - 3:1;
        }
        if (PRESENCE_HEARTBEAT_TASK == 0) {
            PRESENCE_HEARTBEAT_TASK = timedTaskManager.addTask("Presence-Heartbeat",
                    new PresenceHeartbeatTask(PRESENCE_HB_INTERVAL, cb));
        } else if (PRESENCE_HB_INTERVAL == 0 || PRESENCE_HB_INTERVAL > 320) {
            timedTaskManager.removeTask(PRESENCE_HEARTBEAT_TASK);
        } else {
            timedTaskManager.updateTask(PRESENCE_HEARTBEAT_TASK, PRESENCE_HB_INTERVAL);
        }
        disconnectAndResubscribe();
    }

    public void setPnExpires(int pnexpires) {
        setPnExpires(pnexpires, null);
    }

    public void setHeartbeat(int heartbeat) {
        setHeartbeat(heartbeat, null);
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        setHeartbeatInterval(heartbeatInterval, null);
    }
    public void setHeartbeatInterval(int heartbeatInterval, Callback callback) {

        Callback cb = getWrappedCallback(callback);
        PRESENCE_HB_INTERVAL = heartbeatInterval;
        if (PRESENCE_HEARTBEAT_TASK == 0) {
            PRESENCE_HEARTBEAT_TASK = timedTaskManager.addTask("Presence-Heartbeat",
                    new PresenceHeartbeatTask(PRESENCE_HB_INTERVAL, cb));
        } else if (PRESENCE_HB_INTERVAL == 0 || PRESENCE_HB_INTERVAL > 320) {
            timedTaskManager.removeTask(PRESENCE_HEARTBEAT_TASK);
        } else {
            timedTaskManager.updateTask(PRESENCE_HEARTBEAT_TASK, PRESENCE_HB_INTERVAL);
        }

    }

    public int getHeartbeatInterval() {
        return PRESENCE_HB_INTERVAL;
    }


    /**
     * Returns presence expiry timeout value
     * @return Current presence expiry timeout value
     */
    public int getPnExpires() {
        return getHeartbeat();
    }

    /**
     * Returns presence heartbeat value
     * @return Current presence heartbeat value
     */
    public int getHeartbeat() {
        return HEARTBEAT;
    }

    /**
     * This methods sets maximum number of retries for subscribe. Pubnub API
     * will make maxRetries attempts to connect to pubnub servers before timing
     * out.
     *
     * @param maxRetries
     *            Max number of retries
     */
    public void setMaxRetries(int maxRetries) {
        subscribeManager.setMaxRetries(maxRetries);
    }

    /**
     * Returns current max retries for Subscribe
     * @return Current max retries
     */
    public int getMaxRetries() {
        return subscribeManager.maxRetries;
    }

    protected String getPubnubUrl() {

        if (ORIGIN_STR == null) {
            // SSL On?
            if (this.SSL) {
                ORIGIN_STR = "https://";
            } else {
                ORIGIN_STR = "http://";
            }
            ORIGIN_STR  += HOSTNAME;
            ORIGIN_STR  += ((!this.CACHE_BUSTING)?"":"-" + String.valueOf(HOSTNAME_SUFFIX));
            ORIGIN_STR  += "." + DOMAIN;
        }
        return ORIGIN_STR;
    }
    private Callback voidCallback = new Callback()
    {public void successCallback(String channel, Object message) {}};

    protected Callback getWrappedCallback(Callback callback){
        if (callback == null) {
            return voidCallback;
        } else
            return callback;
    }

    private boolean validateInput(String name, Object input, Callback callback) {

        if (input == null) {
            callback.errorCallback("", PubnubError.getErrorObject(
                    PubnubError.PNERROBJ_INVALID_ARGUMENTS, 1, name + " cannot be null"));
            return false;
        }

        if (input instanceof String && ((String)input).length() == 0) {
            callback.errorCallback("", PubnubError.getErrorObject(
                    PubnubError.PNERROBJ_INVALID_ARGUMENTS, 2, name + " cannot be zero length"));
            return false;
        }
        return true;
    }


    /**
     * Enable/Disable Cache Busting
     *
     * @param cacheBusting
     */
    public void setCacheBusting(boolean cacheBusting) {
        this.CACHE_BUSTING = cacheBusting;
    }

    /**
     * Get Cache Busting value
     * @return current cache busting setting
     */
    public boolean getCacheBusting() {
        return this.CACHE_BUSTING;
    }

    /**
     * This method returns all channel names currently subscribed to in form of
     * a comma separated String
     *
     * @return Comma separated string with all channel names currently
     *         subscribed
     */
    public String getCurrentlySubscribedChannelNames() {
        String currentChannels = channelSubscriptions.getItemString();
        return currentChannels.equals("") ? "no channels." : currentChannels;
    }

    /**
     * If Resume on Reconnect is set to true, then Pubnub catches up on
     * reconnection after disconnection. If false, then messages sent on the
     * channel between disconnection and reconnection are not received.
     *
     * @param resumeOnReconnect
     *            True or False setting for Resume on Reconnect
     */
    public void setResumeOnReconnect(boolean resumeOnReconnect) {
        this.resumeOnReconnect = resumeOnReconnect;
    }

    /**
     * Returns Resume on Reconnect current setting
     * @return Resume on Reconnect setting
     */
    public boolean getResumeOnReconnect() {
        return this.resumeOnReconnect;
    }

    /**
     * This method returns unique identifier.
     * @return Unique Identifier .
     */
    abstract String uuid();

    /**
     * Sets value for UUID
     *
     * @param uuid
     *            UUID value for Pubnub client
     */
    public void setUUID(String uuid) {
        this.UUID = uuid;
    }

    /**
     * Gets current UUID
     *
     * @return uuid
     *            current UUID value for Pubnub client
     */
    public String getUUID() {
        return this.UUID;
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

    public PubnubCore(String publish_key, String subscribe_key,
            String secret_key, String cipher_key, boolean ssl_on, String initialization_vector) {
        this.init(publish_key, subscribe_key, secret_key, cipher_key, ssl_on, initialization_vector);
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
     */

    public PubnubCore(String publish_key, String subscribe_key,
            String secret_key, String cipher_key, boolean ssl_on) {
        this.init(publish_key, subscribe_key, secret_key, cipher_key, ssl_on);
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
     * @param ssl_on
     *            SSL enabled ?
     */

    public PubnubCore(String publish_key, String subscribe_key,
            String secret_key, boolean ssl_on) {
        this.init(publish_key, subscribe_key, secret_key, "", ssl_on);
    }

    /**
     *
     * Constructor for Pubnub Class
     *
     * @param publish_key
     *            Publish Key
     * @param subscribe_key
     *            Subscribe Key
     */

    public PubnubCore(String publish_key, String subscribe_key) {
        this.init(publish_key, subscribe_key, "", "", false);
    }

    /**
     *
     * Constructor for Pubnub Class
     *
     * @param publish_key
     *            Publish Key
     * @param subscribe_key
     *            Subscribe Key
     */

    public PubnubCore(String publish_key, String subscribe_key, boolean ssl) {
        this.init(publish_key, subscribe_key, "", "", ssl);
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
     */
    public PubnubCore(String publish_key, String subscribe_key,
            String secret_key) {
        this.init(publish_key, subscribe_key, secret_key, "", false);
    }

    /**
     *
     * Initialize PubNub Object State.
     *
     * @param publish_key
     * @param subscribe_key
     * @param secret_key
     * @param cipher_key
     * @param ssl_on
     */
    private void init(String publish_key, String subscribe_key,
            String secret_key, String cipher_key, boolean ssl_on) {
        this.init(publish_key, subscribe_key, secret_key, cipher_key, ssl_on, null);
    }


    /**
     *
     * Initialize PubNub Object State.
     *
     * @param publish_key
     * @param subscribe_key
     * @param secret_key
     * @param cipher_key
     * @param ssl_on
     */
    private void init(String publish_key, String subscribe_key,
            String secret_key, String cipher_key, boolean ssl_on, String initialization_vector) {
        this.PUBLISH_KEY = publish_key;
        this.SUBSCRIBE_KEY = subscribe_key;
        this.SECRET_KEY = secret_key;
        this.CIPHER_KEY = cipher_key;
        this.SSL = ssl_on;

        if (UUID == null)
            UUID = uuid();

        if (channelSubscriptions == null)
            channelSubscriptions = new Subscriptions();

        if (channelGroupSubscriptions == null)
            channelGroupSubscriptions = new Subscriptions();

        if (subscribeManager == null)
            subscribeManager = new SubscribeManager("Subscribe-Manager-"
                    + System.identityHashCode(this), 10000, 310000);

        if (nonSubscribeManager == null)
            nonSubscribeManager = new NonSubscribeManager(
                    "Non-Subscribe-Manager-" + System.identityHashCode(this),
                    10000, 15000);

        if (timedTaskManager == null)
            timedTaskManager = new TimedTaskManager("TimedTaskManager");

        if (params == null)
            params = new Hashtable();

        params.put("pnsdk", getUserAgent());
        subscribeManager.setHeader("V", VERSION);
        subscribeManager.setHeader("Accept-Encoding", "gzip");
        subscribeManager.setHeader("User-Agent", getUserAgent());

        nonSubscribeManager.setHeader("V", VERSION);
        nonSubscribeManager.setHeader("Accept-Encoding", "gzip");
        nonSubscribeManager.setHeader("User-Agent", getUserAgent());

    }

    /**
     * This method sets timeout value for subscribe/presence. Default value is
     * 310000 milliseconds i.e. 310 seconds
     *
     * @param timeout
     *            Timeout value in milliseconds for subscribe/presence
     */
    protected void setSubscribeTimeout(int timeout) {
        subscribeManager.setRequestTimeout(timeout);
        this.disconnectAndResubscribe();
    }

    protected int getSubscribeTimeout() {
        return subscribeManager.requestTimeout;
    }

    /**
     * This method set timeout value for non subscribe operations like publish,
     * history, hereNow. Default value is 15000 milliseconds i.e. 15 seconds.
     *
     * @param timeout
     *            Timeout value in milliseconds for Non subscribe operations
     *            like publish, history, hereNow
     */
    protected void setNonSubscribeTimeout(int timeout) {
        nonSubscribeManager.setRequestTimeout(timeout);
    }

    protected int getNonSubscribeTimeout() {
        return nonSubscribeManager.requestTimeout;
    }

    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            JSONObject to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, JSONObject message, boolean storeInHistory, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        args.put("storeInHistory", (storeInHistory)?"":"0");
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            JSONOArray to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, JSONArray message, boolean storeInHistory, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        args.put("storeInHistory", (storeInHistory)?"":"0");
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            String to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, String message, boolean storeInHistory, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        args.put("storeInHistory", (storeInHistory)?"":"0");
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            Integer to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, Integer message, boolean storeInHistory, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        args.put("storeInHistory", (storeInHistory)?"":"0");
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            Double to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, Double message, boolean storeInHistory, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        args.put("storeInHistory", (storeInHistory)?"":"0");
        publish(args);
    }


    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            JSONObject to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, JSONObject message, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            JSONOArray to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, JSONArray message, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            String to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, String message, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            Integer to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, Integer message, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param channel
     *            Channel name
     * @param message
     *            Double to be published
     * @param callback
     *            object of sub class of Callback class
     */
    public void publish(String channel, Double message, Callback callback) {
        Hashtable args = new Hashtable();
        args.put("channel", channel);
        args.put("message", message);
        args.put("callback", callback);
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param args
     *            Hashtable containing channel name, message.
     * @param callback
     *            object of sub class of Callback class
     */
    protected void publish(Hashtable args, Callback callback) {
        args.put("callback", callback);
        publish(args);
    }

    /**
     * Send a message to a channel.
     *
     * @param args
     *            Hashtable containing channel name, message, callback
     */

    protected void publish(Hashtable args) {

        final String channel = (String) args.get("channel");
        final Object message = args.get("message");
        final Callback callback = getWrappedCallback((Callback) args.get("callback"));
        String storeInHistory = (String) args.get("storeInHistory");
        String msgStr = message.toString();
        Hashtable parameters = PubnubUtil.hashtableClone(params);

        if (storeInHistory != null && storeInHistory.length() > 0) parameters.put("store", storeInHistory);

        if (this.CIPHER_KEY.length() > 0) {
            // Encrypt Message
            PubnubCrypto pc = new PubnubCrypto(this.CIPHER_KEY, this.IV);
            try {
                if (message instanceof String) {
                    msgStr = "\"" + msgStr + "\"";
                }
                msgStr = "\"" + pc.encrypt(msgStr) + "\"";
            } catch (PubnubException e) {
                callback.errorCallback(channel, getPubnubError(e, PubnubError.PNERROBJ_ENCRYPTION_ERROR, 4, msgStr + " : " + e.toString()));
                return;
            }
        } else {
            if (message instanceof String) {
                msgStr = "\"" + msgStr + "\"";
            }
        }

        // Generate String to Sign
        String signature = "0";

        if (this.SECRET_KEY.length() > 0) {
            StringBuffer string_to_sign = new StringBuffer();
            string_to_sign.append(this.PUBLISH_KEY).append('/')
            .append(this.SUBSCRIBE_KEY).append('/')
            .append(this.SECRET_KEY).append('/').append(channel)
            .append('/').append(msgStr);

            // Sign Message
            try {
                signature = new String(PubnubCrypto.hexEncode(PubnubCrypto
                        .md5(string_to_sign.toString())), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                PubnubError pe = PubnubError.getErrorObject(PubnubError.PNERROBJ_ENCRYPTION_ERROR, 6, msgStr + " : " + e.toString());
                callback.errorCallback(channel, pe);
            } catch (PubnubException e) {
                callback.errorCallback(channel, getPubnubError(e, PubnubError.PNERROBJ_ENCRYPTION_ERROR, 5, msgStr + " : " + e.toString()));
            }
        }
        String[] urlComponents = { getPubnubUrl(), "publish", this.PUBLISH_KEY,
                this.SUBSCRIBE_KEY, PubnubUtil.urlEncode(signature),
                PubnubUtil.urlEncode(channel), "0",
                PubnubUtil.urlEncode(msgStr)
        };

        class PublishResponseHandler extends ResponseHandler {
            public void handleResponse(HttpRequest hreq, String response) {
                JSONArray jsarr;
                try {
                    jsarr = new JSONArray(response);
                } catch (JSONException e) {
                    handleError(hreq,
                            PubnubError.getErrorObject(PubnubError.PNERROBJ_INVALID_JSON, 1, response));
                    return;
                }
                callback.successCallback(channel, jsarr);
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                callback.errorCallback(channel, error);
                return;
            }
        }
        HttpRequest hreq = new HttpRequest(urlComponents, parameters, new PublishResponseHandler());

        _request(hreq, nonSubscribeManager);
    }

    /**
     *
     * Listen for presence of subscribers on a channel
     *
     * @param channel
     *            Name of the channel on which to listen for join/leave i.e.
     *            presence events
     * @param callback
     *            object of sub class of Callback class
     * @exception PubnubException
     *                Throws PubnubException if Callback is null
     */
    public void presence(String channel, Callback callback)
            throws PubnubException {
        Hashtable args = new Hashtable(2);

        args.put("channels", new String[] {channel + PRESENCE_SUFFIX});
        args.put("callback", callback);

        subscribe(args);
    }

    public void channelGroupPresence(String group, Callback callback)
            throws PubnubException {
        Hashtable args = new Hashtable(2);

        args.put("groups", new String[] {group + PRESENCE_SUFFIX});
        args.put("callback", callback);

        subscribe(args);
    }

    /**
     * Read presence information for uuid
     *
     * @param uuid
     *            UUID
     * @param callback
     *            object of sub class of Callback class
     */
    public void whereNow(final String uuid, Callback callback) {
        final Callback cb = getWrappedCallback(callback);
        String[] urlargs = { getPubnubUrl(), "v2", "presence", "sub_key",
                this.SUBSCRIBE_KEY, "uuid", PubnubUtil.urlEncode(uuid)
        };

        HttpRequest hreq = new HttpRequest(urlargs, params,
                new ResponseHandler() {
            public void handleResponse(HttpRequest hreq, String response) {
                invokeCallback("", response, "payload", cb, 4);
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback("", error);
                return;
            }
        });

        _request(hreq, nonSubscribeManager);
    }

    public void whereNow(Callback callback) {
        whereNow(this.UUID, callback);
    }

    public void setState(String channel, String uuid, JSONObject state, Callback callback) {
        _setState(channelSubscriptions, PubnubUtil.urlEncode(channel), null, uuid, state, callback);
    }

    public void channelGroupSetState(String group, String uuid, JSONObject state, Callback callback) {
        _setState(channelSubscriptions, ".", group, uuid, state, callback);
    }

    protected void _setState(Subscriptions sub, String channel, String group, String uuid, JSONObject state, Callback callback) {
        SubscriptionItem item = sub.getItem(channel);
        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);

        String[] urlArgs = { getPubnubUrl(), "v2", "presence", "sub-key",
                this.SUBSCRIBE_KEY, "channel", channel, "uuid", PubnubUtil.urlEncode(uuid),
                "data"
        };

        if (state != null) parameters.put("state", state.toString());
        if (group != null) parameters.put("channel-group", group);

        if (item != null) {
            try {
                sub.state.put(channel, state);
            } catch (JSONException e) {

            }
        }

        HttpRequest hreq = new HttpRequest(urlArgs, parameters,
                new ResponseHandler() {
            public void handleResponse(HttpRequest hreq, String response) {
                invokeCallback("", response, "payload", cb, 2 );
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback("", error);
            }
        });

        _request(hreq, nonSubscribeManager);
    }

    public void getState(String channel, String uuid, Callback callback) {
        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);

        String[] urlArgs = { getPubnubUrl(), "v2", "presence", "sub-key",
                this.SUBSCRIBE_KEY, "channel", PubnubUtil.urlEncode(channel),
                "uuid", PubnubUtil.urlEncode(uuid)
        };

        HttpRequest hreq = new HttpRequest(urlArgs, parameters,
                new ResponseHandler() {
                    public void handleResponse(HttpRequest hreq, String response) {
                        invokeCallback("", response, "payload", cb, 1 );
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback("", error);
                    }
                });

        _request(hreq, nonSubscribeManager);
    }

    protected void invokeCallback(String channel, String response, String key,
            Callback callback, int extendedErrorCode) {
        invokeCallback(channel, response, key, callback, extendedErrorCode, false);
    }

    protected void invokeCallback(String channel, String response, String key,
            Callback callback, int extendedErrorCode, boolean key_strict) {
        JSONObject responseJso = null;
        try {
            responseJso = new JSONObject(response);
        } catch (JSONException e) {
            callback.errorCallback(channel,
                    PubnubError.getErrorObject(PubnubError.PNERROBJ_JSON_ERROR, extendedErrorCode, response));
            return;
        }

        JSONObject payloadJso = null;

        if (key != null && key.length() > 0) {
            try {
                payloadJso = (JSONObject) responseJso.get(key);
            } catch (JSONException e) {
                if (!key_strict) {
                    callback.successCallback(channel, responseJso);
                } else {
                    callback.errorCallback(channel,
                            PubnubError.getErrorObject(PubnubError.PNERROBJ_JSON_ERROR, extendedErrorCode, response));
                }
                return;

            }
            callback.successCallback(channel, payloadJso);
            return;
        }
    }

    protected void invokeJSONStringCallback(String response, String key, Callback callback) {
        String responseJSON;

        try {
            responseJSON = (new JSONObject(response)).getString(key);
            callback.successCallback(null, responseJSON);
        } catch (JSONException e) {
            callback.errorCallback(
                    null,
                    PubnubError.getErrorObject(PubnubError.PNERROBJ_JSON_ERROR, 0, response)
            );
        }
    }

    /**
     * Get all namespaces
     *
     * @param callback to invoke
     */
    public void channelGroupListNamespaces(Callback callback) {
        final Callback cb = getWrappedCallback(callback);

        String[] url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                    this.SUBSCRIBE_KEY, "namespace"};

        Hashtable parameters = PubnubUtil.hashtableClone(params);

        HttpRequest hreq = new HttpRequest(url, parameters, new ResponseHandler() {

            public void handleResponse(HttpRequest hreq, String response) {
                invokeCallback("", response, "payload", cb, 0);
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback(null, error);
            }
        });

        _request(hreq, nonSubscribeManager);
    }

    /**
     * Remove namespace
     *
     * @param namespace to remove
     * @param callback to invoke
     */
    public void channelGroupRemoveNamespace(String namespace, Callback callback) {
        final Callback cb = getWrappedCallback(callback);

        String[] url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                this.SUBSCRIBE_KEY, "namespace", namespace, "remove"};

        Hashtable parameters = PubnubUtil.hashtableClone(params);

        HttpRequest hreq = new HttpRequest(url, parameters, new ResponseHandler() {

            public void handleResponse(HttpRequest hreq, String response) {
                invokeJSONStringCallback(response, "message", cb);
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback(null, error);
            }
        });

        _request(hreq, nonSubscribeManager);
    }

    /**
     * Get the list of groups in the global namespace
     *
     * @param callback to invoke
     */
    public void channelGroupListGroups(Callback callback) {
        channelGroupListGroups(null, callback);
    }

    /**
     * Get the list of groups in the namespace
     *
     * @param namespace name
     * @param callback to invoke
     */
    public void channelGroupListGroups(String namespace, Callback callback) {
        final Callback cb = getWrappedCallback(callback);
        String[] url;

        if (namespace != null) {
            url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                    this.SUBSCRIBE_KEY, "namespace", namespace, "channel-group"};
        } else {
            url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                    this.SUBSCRIBE_KEY, "channel-group"};
        }

        Hashtable parameters = PubnubUtil.hashtableClone(params);

        HttpRequest hreq = new HttpRequest(url, parameters, new ResponseHandler() {

            public void handleResponse(HttpRequest hreq, String response) {
                invokeCallback("", response, "payload", cb, 0);
             }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback(null, error);
            }
        });

        _request(hreq, nonSubscribeManager);
    }

    /**
     * Get the list of channels in the namespaced group
     *
     * @param group name
     * @param callback to invoke
     */
    public void channelGroupListChannels(String group, Callback callback) {
        final Callback cb = getWrappedCallback(callback);
        ChannelGroup channelGroup;
        String[] url;

        try {
            channelGroup =  new ChannelGroup(group);
        } catch (PubnubException e) {
            cb.errorCallback(null, PubnubError.PNERROBJ_CHANNEL_GROUP_PARSING_ERROR);
            return;
        }

        if (channelGroup.namespace != null) {
            url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                    this.SUBSCRIBE_KEY, "namespace", channelGroup.namespace, "channel-group", channelGroup.group};
        } else {
            url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                    this.SUBSCRIBE_KEY, "channel-group", channelGroup.group};
        }

        Hashtable parameters = PubnubUtil.hashtableClone(params);

        HttpRequest hreq = new HttpRequest(url, parameters, new ResponseHandler() {

            public void handleResponse(HttpRequest hreq, String response) {
                invokeCallback("", response, "payload", cb, 0);
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback(null, error);
            }
        });

        _request(hreq, nonSubscribeManager);
    }

    public void channelGroupAddChannel(String group, String channel, Callback callback) {
        channelGroupUpdate("add", group, new String[]{channel}, callback);
    }

    public void channelGroupAddChannel(String group, String[] channels, Callback callback) {
        channelGroupUpdate("add", group, channels, callback);
    }

    public void channelGroupRemoveChannel(String group, String channel, Callback callback) {
        channelGroupUpdate("remove", group, new String[]{channel}, callback);
    }

    public void channelGroupRemoveChannel(String group, String[] channels, Callback callback) {
        channelGroupUpdate("remove", group, channels, callback);
    }

    private void channelGroupUpdate(String action, String group, String[] channels, final Callback callback) {
        final Callback cb = getWrappedCallback(callback);
        ChannelGroup channelGroup;
        String[] url;

        try {
            channelGroup =  new ChannelGroup(group);
        } catch (PubnubException e) {
            cb.errorCallback(null, PubnubError.PNERROBJ_CHANNEL_GROUP_PARSING_ERROR);
            return;
        }

        if (channelGroup.namespace != null) {
            url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                    this.SUBSCRIBE_KEY, "namespace", channelGroup.namespace, "channel-group", channelGroup.group};
        } else {
            url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                    this.SUBSCRIBE_KEY, "channel-group", channelGroup.group};
        }

        Hashtable parameters = PubnubUtil.hashtableClone(params);

        if (channels.length > 0) {
            parameters.put(action, PubnubUtil.joinString(channels, ","));
        }

        HttpRequest hreq = new HttpRequest(url, parameters, new ResponseHandler() {

            public void handleResponse(HttpRequest hreq, String response) {
                invokeJSONStringCallback(response, "message", cb);
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback(null, error);
            }

        });

        _request(hreq, nonSubscribeManager);
    }

    public void channelGroupRemoveGroup(String group, Callback callback) {
        final Callback cb = getWrappedCallback(callback);
        ChannelGroup channelGroup;
        String[] url;

        try {
            channelGroup =  new ChannelGroup(group);
        } catch (PubnubException e) {
            cb.errorCallback(null, PubnubError.PNERROBJ_CHANNEL_GROUP_PARSING_ERROR);
            return;
        }

        if (channelGroup.namespace != null) {
            url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                    this.SUBSCRIBE_KEY, "namespace",
                    channelGroup.namespace, "channel-group", channelGroup.group, "remove"};
        } else {
            url = new String[]{getPubnubUrl(), "v1", "channel-registration", "sub-key",
                    this.SUBSCRIBE_KEY, "channel-group", channelGroup.group, "remove"};
        }

        Hashtable parameters = PubnubUtil.hashtableClone(params);

        HttpRequest hreq = new HttpRequest(url, parameters, new ResponseHandler() {

            public void handleResponse(HttpRequest hreq, String response) {
                invokeJSONStringCallback(response, "message", cb);
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback(null, error);
            }

        });

        _request(hreq, nonSubscribeManager);
    }

    /**
     * Read presence information from a channel
     *
     * @param channel
     *            Channel name
     * @param callback
     *            object of sub class of Callback class
     */
    public void hereNow(final String channel, Callback callback) {
        hereNow(new String[]{channel}, null, false, true, callback);
    }

    public void hereNow(boolean state, boolean uuids, Callback callback) {
        hereNow(null, null, state, uuids, callback);
    }

    public void hereNow(final String channel, boolean state, boolean uuids, Callback callback) {
        hereNow(new String[]{channel}, null, state, uuids, callback);
    }

    public void channelGroupHereNow(String group, Callback callback) {
        channelGroupHereNow(group, false, true, callback);
    }

    public void channelGroupHereNow(String group, boolean state, boolean uuids, Callback callback) {
        channelGroupHereNow(new String[]{group}, state, uuids, callback);
    }

    public void channelGroupHereNow(String[] groups, boolean state, boolean uuids, Callback callback) {
        hereNow(null, groups, state, uuids, callback);
    }

    /**
     * Read presence information from a channel or a channel group
     *
     * @param channels      array
     * @param channelGroups array
     * @param state         state enabled ?
     * @param uuids         enable / disable returning uuids in response ?
     * @param callback      object of sub class of Callback class
     */
    public void hereNow(String[] channels, String[] channelGroups, boolean state,
                        boolean uuids, Callback callback) {

        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);
        ArrayList urlArgs = new ArrayList();

        urlArgs.add(getPubnubUrl());
        urlArgs.add("v2");
        urlArgs.add("presence");
        urlArgs.add("sub_key");
        urlArgs.add(this.SUBSCRIBE_KEY);

        if (channels != null || channelGroups != null) {
            String channelsString = PubnubUtil.joinString(channels, ",");
            if ("".equals(channelsString)) {
                channelsString = ",";
            } else {
                channelsString = PubnubUtil.urlEncode(channelsString);
            }

            urlArgs.add("channel");
            urlArgs.add(channelsString);
        }

        if (state) parameters.put("state", "1");
        if (!uuids) parameters.put("disable_uuids", "1");
        if (channelGroups != null && channelGroups.length > 0) {
            parameters.put("channel-group", PubnubUtil.joinString(channelGroups, ","));
        }

        String[] path = (String[]) urlArgs.toArray(new String[urlArgs.size()]);

        HttpRequest hreq = new HttpRequest(path, parameters,
                new ResponseHandler() {
                    public void handleResponse(HttpRequest hreq, String response) {
                        invokeCallback(null, response, "payload", cb, 1);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback(null, error);
                    }
                });

        _request(hreq, nonSubscribeManager);
    }

    /**
    *
    * Read History for a channel.
    *
    * @param channel
    *            Channel name for which history is required
    * @param start
    *            Start time
    * @param end
    *            End time
    * @param count
    *            Upper limit on number of messages to be returned
    * @param reverse
    *            True if messages need to be in reverse order
    * @param callback
    *            Callback
    */
   public void history(final String channel, long start, long end,
           int count, boolean reverse, Callback callback) {
       history(channel, start, end, count, reverse, false, callback);
   }

   private PubnubError getPubnubError(PubnubException px, PubnubError error, int code, String message) {
       PubnubError pe = px.getPubnubError();
       if (pe == null) {
           pe = PubnubError.getErrorObject(error, code, message);
       }
       return pe;
   }

    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param start
     *            Start time
     * @param end
     *            End time
     * @param count
     *            Upper limit on number of messages to be returned
     * @param reverse
     *            True if messages need to be in reverse order
     * @param includeTimetoken
     *            True/False whether to include timetokens in response
     * @param callback
     *            Callback
     */
    public void history(final String channel, long start, long end,
            int count, boolean reverse, boolean includeTimetoken, Callback callback) {
        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);
        if (count == -1)
            count = 100;

        parameters.put("count", String.valueOf(count));
        parameters.put("reverse", String.valueOf(reverse));
        parameters.put("include_token", String.valueOf(includeTimetoken));

        if (start != -1)
            parameters.put("start", Long.toString(start).toLowerCase());

        if (end != -1)
            parameters.put("end", Long.toString(end).toLowerCase());

        String[] urlargs = { getPubnubUrl(), "v2", "history", "sub-key",
                this.SUBSCRIBE_KEY, "channel", PubnubUtil.urlEncode(channel)
        };


        class HistoryResponseHandler extends ResponseHandler {

            public void handleResponse(HttpRequest hreq, String response) {
                JSONArray respArr;
                try {
                    respArr = new JSONArray(response);
                    decryptJSONArray((JSONArray) respArr.get(0));
                    cb.successCallback(channel, respArr);
                } catch (JSONException e) {
                    cb.errorCallback(channel,
                            PubnubError.getErrorObject(PubnubError.PNERROBJ_JSON_ERROR, 3));
                } catch (IOException e) {
                    cb.errorCallback(channel,
                            PubnubError.getErrorObject(PubnubError.PNERROBJ_DECRYPTION_ERROR, 9, response));
                } catch (PubnubException e) {
                    cb.errorCallback(channel, getPubnubError(e, PubnubError.PNERROBJ_DECRYPTION_ERROR, 10, response + " : " + e.toString()));
                } catch (Exception e) {
                    cb.errorCallback(channel,
                            PubnubError.getErrorObject(PubnubError.PNERROBJ_DECRYPTION_ERROR, 11, response + " : " + e.toString()));
                }

            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback(channel, error);
                return;
            }
        }

        HttpRequest hreq = new HttpRequest(urlargs, parameters, new HistoryResponseHandler());
        _request(hreq, nonSubscribeManager);
    }

    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param start
     *            Start time
     * @param end
     *            End time
     * @param reverse
     *            True if messages need to be in reverse order
     *
     * @param callback
     *            Callback
     */
    public void history(String channel, long start, long end,
            boolean reverse, Callback callback) {
        history(channel, start, end, -1, reverse, callback);
    }

    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param count
     *            Maximum number of messages
     * @param callback
     *            Callback object
     */
    public void history(String channel, int count, Callback callback) {
        history(channel, -1, -1, count, false, callback);
    }

    /**
    *
    * Read History for a channel.
    *
    * @param channel
    *            Channel name for which history is required
    * @param includeTimetoken
    *            True/False whether to include timetokens in response
    * @param count
    *            Maximum number of messages
    * @param callback
    *            Callback object
    */
   public void history(String channel, boolean includeTimetoken, int count, Callback callback) {
       history(channel, -1, -1, count, false, includeTimetoken, callback);
   }


    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param start
     *            Start time
     * @param reverse
     *            True if messages need to be in reverse order
     * @param callback
     *            Callback
     */
    public void history(String channel, long start, boolean reverse,
            Callback callback) {
        history(channel, start, -1, -1, reverse, callback);
    }

    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param start
     *            Start time
     * @param end
     *            End time
     * @param callback
     *            Callback
     */
    public void history(String channel, long start, long end,
            Callback callback) {
        history(channel, start, end, -1, false, callback);
    }

    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param start
     *            Start time
     * @param end
     *            End time
     * @param count
     *            Upper limit on number of messages to be returned
     * @param callback
     *            Callback
     */
    public void history(String channel, long start, long end, int count,
            Callback callback) {
        history(channel, start, end, count, false, callback);
    }

    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param start
     *            Start time
     * @param count
     *            Upper limit on number of messages to be returned
     * @param reverse
     *            True if messages need to be in reverse order
     *
     * @param callback
     *            Callback
     */
    public void history(String channel, long start, int count,
            boolean reverse, Callback callback) {
        history(channel, start, -1, count, reverse, callback);
    }

    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param start
     *            Start time
     * @param count
     *            Upper limit on number of messages to be returned
     * @param callback
     *            Callback
     */
    public void history(String channel, long start, int count,
            Callback callback) {
        history(channel, start, -1, count, false, callback);
    }

    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param count
     *            Upper limit on number of messages to be returned
     * @param reverse
     *            True if messages need to be in reverse order
     * @param callback
     *            Callback
     */
    public void history(String channel, int count, boolean reverse,
            Callback callback) {
        history(channel, -1, -1, count, reverse, callback);
    }

    /**
     *
     * Read History for a channel.
     *
     * @param channel
     *            Channel name for which history is required
     * @param reverse
     *            True if messages need to be in reverse order
     * @param callback
     *            Callback
     */
    public void history(String channel, boolean reverse,
            Callback callback) {
        history(channel, -1, -1, -1, reverse, callback);
    }

    /**
     * Read current time from PubNub Cloud.
     *
     * @param callback
     *            Callback object
     */
    public void time(Callback callback) {
        final Callback cb = getWrappedCallback(callback);

        String[] url = { getPubnubUrl(), "time", "0" };
        HttpRequest hreq = new HttpRequest(url, params, new ResponseHandler() {

            public void handleResponse(HttpRequest hreq, String response) {
                cb.successCallback(null, response);
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                cb.errorCallback(null, error);
            }

        });

        _request(hreq, nonSubscribeManager);
    }

    private void keepOnlyPluralSubscriptionItems(Hashtable args) {
        String _channel = (String) args.get("channel");
        String _group = (String) args.get("group");

        if (_channel != null && !(_channel.equals(""))) {
            args.put("channels", new String[]{_channel});
            args.remove("channel");
        }

        if (_group != null && !(_group.equals(""))) {
            args.put("groups", new String[]{_group});
            args.remove("group");
        }
    }

    private boolean inputsValid(Hashtable args) throws PubnubException {
        boolean channelsOk;
        boolean groupsOk;

        if (!(args.get("callback") instanceof Callback) || args.get("callback") == null) {
            throw new PubnubException("Invalid Callback");
        }

        String[] _channels = (String[]) args.get("channels");
        String[] _groups = (String[]) args.get("groups");

        channelsOk = (_channels != null && _channels.length > 0);
        groupsOk = (_groups != null && _groups.length > 0);

        if (!channelsOk && !groupsOk) {
            throw new PubnubException("Channel or Channel Group Missing");
        }

        return true;
    }

    
    private void _leave(String channel, Callback callback) {
        _leave(channel, null, PubnubUtil.hashtableClone(this.params), callback);
    }

    private void _leave(String channel) {
        _leave(channel, null);
    }
    
    private void channelGroupLeave(String group) {
    	channelGroupLeave(group, null);
    }

    private void channelGroupLeave(String group, Callback callback) {
        _leave(null, group, PubnubUtil.hashtableClone(this.params), callback);
    }
   
    private void _leave(String[] channels, String[] channelGroups, Hashtable params) {
    	_leave(channels, channelGroups, params, null);
    }
    
    private void _leave(String[] channels, String[] channelGroups, Hashtable params, Callback callback) {
    	_leave(PubnubUtil.joinString(channels, ","),
    			PubnubUtil.joinString(channelGroups, ","), params, callback);
    }
    
    private void _leave(String[] channels, String[] channelGroups) {
    	_leave(channels, channelGroups, PubnubUtil.hashtableClone(this.params), null);
    } 
    
    
    private void _leave(String[] channels, String[] channelGroups, Callback callback) {
    	_leave(PubnubUtil.joinString(channels, ","),
    			PubnubUtil.joinString(channelGroups, ","), PubnubUtil.hashtableClone(this.params), callback);
    }    
    
    private void _leave(String channel, String channelGroup, Callback callback) {
    	_leave(channel, channelGroup, PubnubUtil.hashtableClone(this.params), callback);
    }
    
    private void _leave(String channel, String channelGroup, Hashtable params, Callback callback) {
    	
    	final Callback cb = getWrappedCallback(callback);
    	
    	if (PubnubUtil.isEmptyString(channel) &&
    			PubnubUtil.isEmptyString(channelGroup))
    		return;
    	
    	if (PubnubUtil.isEmptyString(channel)) channel = ",";
    	
        String[] urlArgs = {getPubnubUrl(), "v2/presence/sub_key",
                this.SUBSCRIBE_KEY, "channel", PubnubUtil.urlEncode(channel), "leave"
        };

        params.put("uuid", UUID);
        

        if (!PubnubUtil.isEmptyString(channelGroup))
        	params.put("channel-group", channelGroup);

        HttpRequest hreq = new HttpRequest(urlArgs, params,
                new ResponseHandler() {
                    public void handleResponse(HttpRequest hreq, String response) {
                    	cb.successCallback(null, response);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                    	cb.errorCallback(null, error);
                    }
                });

        _request(hreq, nonSubscribeManager);
    }

    /**
     * Unsubscribe from channels.
     *
     * @param channels String array containing channel names
     */
    public void unsubscribe(String[] channels, Callback callback) {
        for (int i = 0; i < channels.length; i++) {
            String channel = channels[i];
            channelSubscriptions.removeItem(channel);
            channelSubscriptions.state.remove(channel);
        }
        _leave(channels, null, callback);
        resubscribe();
    }
    
    /**
     * Unsubscribe from channels.
     *
     * @param channels String array containing channel names
     */
    public void unsubscribe(String[] channels) {
    	unsubscribe(channels, null);
    }

    /**
     * Unsubscribe/Disconnect from channel.
     *
     * @param channel channel name as String.
     */
    public void unsubscribe(String channel) {
        unsubscribe(channel, null);
    }

    
    /**
     * Unsubscribe/Disconnect from channel.
     *
     * @param channel channel name as String.
     */
    public void unsubscribe(String channel, Callback callback) {
        unsubscribe(new String[]{channel}, callback);
    }
    
    /**
     * Unsubscribe/Disconnect from channel.
     *
     * @param args Hashtable containing channel name.
     */
    protected void unsubscribe(Hashtable args) {
        String[] channelList = (String[]) args.get("channels");
        if (channelList == null) {
            channelList = new String[]{(String) args.get("channel")};
        }
        unsubscribe(channelList);
    }

    /**
     * Unsubscribe from channel group
     *
     * @param group to unsubscribe
     */
    public void channelGroupUnsubscribe(String group) {
        channelGroupUnsubscribe(group, null);
    }

    /**
     * Unsubscribe from channel group
     *
     * @param group to unsubscribe
     * @param callback Callback
     */
    public void channelGroupUnsubscribe(String group, Callback callback) {
        channelGroupUnsubscribe(new String[]{group}, callback);
    }
    
    
    /**
     * Unsubscribe from multiple channel groups
     *
     * @param groups to unsubscribe
     * @param callback Callback
     */
    public void channelGroupUnsubscribe(String[] groups, Callback callback) {
        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
            channelGroupSubscriptions.removeItem(group);
        }
        _leave(null, groups, callback);
        resubscribe();
    }
    
    /**
     * Unsubscribe from multiple channel groups
     *
     * @param groups to unsubscribe
     */
    public void channelGroupUnsubscribe(String[] groups) {
    	channelGroupUnsubscribe(groups, null);
    }
    

    /**
     * Unsubscribe from presence channel.
     *
     * @param channel channel name as String.
     * @param callback Callback
     */
    public void unsubscribePresence(String channel, Callback callback) {
        unsubscribe(new String[]{channel + PRESENCE_SUFFIX}, callback);
    }

    /**
     * Unsubscribe from presence channel.
     *
     * @param channel channel name as String.
     */
    public void unsubscribePresence(String channel) {
    	unsubscribePresence(channel, null);
    }
    
    /**
     * Unsubscribe from all channels and channel groups.
     * 
     * @param callback
     */
    public void unsubscribeAll(Callback callback) {
        String[] channels = channelSubscriptions.getItemNames();
        String[] groups = channelGroupSubscriptions.getItemNames();

        for (int i = 0; i < channels.length; i++) {
            String channel = channels[i];
            channelSubscriptions.removeItem(channel);
        }

        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
            channelGroupSubscriptions.removeItem(group);
        }
        _leave(channels, groups, callback);
        disconnectAndResubscribe();
    }
    
    /**
     * Unsubscribe from all channels and channel groups.
     */    
    public void unsubscribeAll() {
    	unsubscribeAll(null);
    }

    /**
     * Unsubscribe from all channels.
     */
    public void unsubscribeAllChannels() {
    	unsubscribeAllChannels(null);
    }
    
    /**
     * Unsubscribe from all channels.
     * 
     * @param callback Callback
     */
    public void unsubscribeAllChannels(Callback callback) {
        String[] channels = channelSubscriptions.getItemNames();

        for (int i = 0; i < channels.length; i++) {
            String channel = channels[i];
            channelSubscriptions.removeItem(channel);
        }
        _leave(channels, null, callback);

        disconnectAndResubscribe();
    }

    /**
     * Unsubscribe from all channel groups.
     */
    public void channelGroupUnsubscribeAllGroups() {
    	channelGroupUnsubscribeAllGroups(null);
    }
    
    /**
     * Unsubscribe from all channel groups.
     * 
     * @param callback Callback
     */
    public void channelGroupUnsubscribeAllGroups(Callback callback) {
        String[] groups = channelGroupSubscriptions.getItemNames();

        for (int i = 0; i < groups.length; i++) {
            String group = groups[i];
            channelGroupSubscriptions.removeItem(group);
        }
        _leave(null, groups, callback);

        disconnectAndResubscribe();
    }

    /**
     * Listen for a message on a channel.
     *
     * @param args     hashtable
     * @param callback to call
     * @throws PubnubException
     */
    protected void subscribe(Hashtable args, Callback callback)
            throws PubnubException {
        args.put("callback", callback);

        subscribe(args);
    }

    /**
     * Listen for a message on a channel.
     *
     * @param args hashtable
     * @throws PubnubException
     */
    protected void subscribe(Hashtable args) throws PubnubException {

        keepOnlyPluralSubscriptionItems(args);

        if (!inputsValid(args)) {
            return;
        }

        _subscribe(args);
    }

    /**
     * Listen for a message on a channel.
     *
     * @param channels array to listen on
     * @param callback to call
     * @throws PubnubException
     */
    public void subscribe(String[] channels, Callback callback)
            throws PubnubException {
        subscribe(channels, callback, "0");
    }

    /**
     * Listen for a message on a channel.
     *
     * @param channels  array to listen on
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String[] channels, Callback callback, String timetoken)
            throws PubnubException {

        Hashtable args = new Hashtable();

        args.put("channels", channels);
        args.put("callback", callback);
        args.put("timetoken", timetoken);

        subscribe(args);
    }

    /**
     * Listen for a message on a channel.
     *
     * @param channels  array to listen on
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException Throws PubnubException if Callback is null
     */
    public void subscribe(String[] channels, Callback callback, long timetoken)
            throws PubnubException {
        subscribe(channels, callback, String.valueOf(timetoken));
    }

    /**
     * Listen for a message on a channel.
     *
     * @param channel  name
     * @param callback to call
     * @throws PubnubException
     */
    public void subscribe(String channel, Callback callback)
            throws PubnubException {
        subscribe(channel, callback, "0");
    }

    /**
     * Listen for a message on a channel.
     *
     * @param channel  name
     * @param callback to call
     * @throws PubnubException
     */
    public void subscribe(String channel, Callback callback, String timetoken)
            throws PubnubException {
        subscribe(new String[]{channel}, callback, timetoken);
    }

    /**
     * Listen for a message on a channel.
     *
     * @param channel   name
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String channel, Callback callback, long timetoken)
            throws PubnubException {
        subscribe(channel, callback, String.valueOf(timetoken));
    }

    /**
     * Listen for a message on a channel and on a channel group.
     *
     * @param channel  name
     * @param group    name
     * @param callback to call
     * @throws PubnubException
     */
    public void subscribe(String channel, String group, Callback callback)
            throws PubnubException {
        subscribe(channel, group, callback, "0");
    }

    /**
     * Listen for a message on a channel and on a channel group.
     *
     * @param channel   name
     * @param group     name
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String channel, String group, Callback callback, long timetoken)
            throws PubnubException {
        subscribe(channel, group, callback, String.valueOf(timetoken));
    }

    /**
     * Listen for a message on a channel and on a channel group.
     *
     * @param channel   name
     * @param group     name
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String channel, String group, Callback callback, String timetoken)
            throws PubnubException {
        subscribe(new String[]{channel}, new String[]{group}, callback, timetoken);
    }

    /**
     * Listen for a message on a multiple channels and a single channel group.
     *
     * @param channels array to listen on
     * @param group    name
     * @param callback to call
     * @throws PubnubException
     */
    public void subscribe(String[] channels, String group, Callback callback)
            throws PubnubException {
        subscribe(channels, group, callback, "0");
    }

    /**
     * Listen for a message on a multiple channels and a single channel group.
     *
     * @param channels  array to listen on
     * @param group     name
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String[] channels, String group, Callback callback, long timetoken)
            throws PubnubException {
        subscribe(channels, group, callback, String.valueOf(timetoken));
    }

    /**
     * Listen for a message on a multiple channels and a single channel group.
     *
     * @param channels  array to listen on
     * @param group     name
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String[] channels, String group, Callback callback, String timetoken)
            throws PubnubException {
        subscribe(channels, new String[]{group}, callback, timetoken);
    }

    /**
     * Listen for a message on a channel and a multiple channel groups.
     *
     * @param channel  name
     * @param groups   array to listen on
     * @param callback to call
     * @throws PubnubException
     */
    public void subscribe(String channel, String[] groups, Callback callback)
            throws PubnubException {
        subscribe(channel, groups, callback, "0");
    }

    /**
     * Listen for a message on a channel and a multiple channel groups.
     *
     * @param channel   name
     * @param groups    array to listen on
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String channel, String[] groups, Callback callback, long timetoken)
            throws PubnubException {
        subscribe(channel, groups, callback, String.valueOf(timetoken));
    }

    /**
     * Listen for a message on a channel and a multiple channel groups.
     *
     * @param channel   name
     * @param groups    array to listen on
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String channel, String[] groups, Callback callback, String timetoken)
            throws PubnubException {
        subscribe(new String[]{channel}, groups, callback, timetoken);
    }

    /**
     * Listen for a message on a multiple channels and a multiple channel groups
     *
     * @param channels array to listen on
     * @param groups   array to listen on
     * @param callback to call
     * @throws PubnubException
     */
    public void subscribe(String[] channels, String[] groups, Callback callback)
            throws PubnubException {
        subscribe(channels, groups, callback, "0");
    }

    /**
     * Listen for a message on a multiple channels and a multiple channel groups
     *
     * @param channels  array to listen on
     * @param groups    array to listen on
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String[] channels, String[] groups, Callback callback, long timetoken)
            throws PubnubException {
        subscribe(channels, groups, callback, String.valueOf(timetoken));
    }

    /**
     * Listen for a message on a multiple channels and a multiple channel groups
     *
     * @param channels  array to listen on
     * @param groups    array to listen on
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void subscribe(String[] channels, String[] groups, Callback callback, String timetoken)
            throws PubnubException {
        Hashtable args = new Hashtable();

        args.put("channels", channels);
        args.put("groups", groups);
        args.put("callback", callback);
        args.put("timetoken", timetoken);

        subscribe(args);
    }

    /**
     * Listen for a message on a channel group.
     *
     * @param group    name to subscribe
     * @param callback to call
     * @throws PubnubException
     */
    public void channelGroupSubscribe(String group, Callback callback)
            throws PubnubException {
        channelGroupSubscribe(group, callback, "0");
    }

    /**
     * Listen for a message on multiple channel groups.
     *
     * @param groups   to subscribe
     * @param callback to call
     * @throws PubnubException if Callback is null
     */
    public void channelGroupSubscribe(String[] groups, Callback callback)
            throws PubnubException {
        channelGroupSubscribe(groups, callback, "0");
    }

    /**
     * Listen for a message on a channel group.
     *
     * @param group     name to subscribe
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void channelGroupSubscribe(String group, Callback callback, long timetoken)
            throws PubnubException {
        channelGroupSubscribe(group, callback, String.valueOf(timetoken));
    }

    /**
     * Listen for a message on a channel group.
     *
     * @param group     name to subscribe
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void channelGroupSubscribe(String group, Callback callback, String timetoken)
            throws PubnubException {
        channelGroupSubscribe(new String[]{group}, callback, timetoken);
    }

    /**
     * Listen for a message on multiple channel group.
     *
     * @param groups    to subscribe
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void channelGroupSubscribe(String[] groups, Callback callback, long timetoken)
            throws PubnubException {
        channelGroupSubscribe(groups, callback, String.valueOf(timetoken));
    }

    /**
     * Listen for a message on multiple channel group.
     *
     * @param groups    to subscribe
     * @param callback  to call
     * @param timetoken to use for subscribing
     * @throws PubnubException
     */
    public void channelGroupSubscribe(String[] groups, Callback callback, String timetoken)
            throws PubnubException {

        Hashtable args = new Hashtable();

        args.put("groups", groups);
        args.put("callback", callback);
        args.put("timetoken", timetoken);

        subscribe(args);
    }

    private void callErrorCallbacks(String[] channelList, PubnubError error) {
        for (int i = 0; i < channelList.length; i++) {
            String channel = channelList[i];
            Callback cb = channelSubscriptions.getItem(channel).callback;
            cb.errorCallback(channel, error);
        }
    }

    private void decryptJSONArray(JSONArray messages) throws JSONException, IllegalStateException, IOException, PubnubException {

        if (CIPHER_KEY.length() > 0) {
            for (int i = 0; i < messages.length(); i++) {
                PubnubCrypto pc = new PubnubCrypto(CIPHER_KEY, IV);

                String message;
                message = pc.decrypt(messages.get(i).toString());
                messages.put(i, PubnubUtil.stringToJSON(message));
            }
        }
    }

    /**
     * @param args
     *            Hashtable
     */
    private void _subscribe(Hashtable args) {

        String[] channelList = (String[]) args.get("channels");
        String[] groupList = (String[]) args.get("groups");

        if (channelList == null) {
            channelList = new String[0];
        }

        if (groupList == null) {
            groupList = new String[0];
        }

        Callback callback = (Callback) args.get("callback");
        String timetoken = (String) args.get("timetoken");

        if (!_timetoken.equals("0"))
            _saved_timetoken = _timetoken;
        _timetoken = (timetoken == null) ? "0" : timetoken;

        /*
         * Scan through the channels array. If a channel does not exist in
         * hashtable create a new item with default values. If already exists
         * and connected, then return
         */

        for (int i = 0; i < channelList.length; i++) {
            String channel = channelList[i];

            if (channel.endsWith(WILDCARD_SUFFIX + PRESENCE_SUFFIX)) {
                String messagesChannel = channel.substring(0, channel.indexOf(PRESENCE_SUFFIX));

                SubscriptionItem wildcardMessagesObj = (SubscriptionItem) channelSubscriptions.getItem(messagesChannel);
                SubscriptionItem wildcardPresenceObj = (SubscriptionItem) channelSubscriptions.getItem(channel);

                if (wildcardMessagesObj == null) {
                    SubscriptionItem ch = new SubscriptionItem(messagesChannel, callback);

                    channelSubscriptions.addItem(ch);
                }

                if (wildcardPresenceObj == null) {
                    SubscriptionItem pr = new SubscriptionItem(channel, callback);

                    channelSubscriptions.addItem(pr);
                }
            } else {
                SubscriptionItem channelObj = (SubscriptionItem) channelSubscriptions.getItem(channel);

                if (channelObj == null) {
                    SubscriptionItem ch = new SubscriptionItem(channel, callback);

                    channelSubscriptions.addItem(ch);
                }
            }
        }

        for (int i = 0; i < groupList.length; i++) {
            String group = groupList[i];
            SubscriptionItem channelGroupObj = (SubscriptionItem) channelGroupSubscriptions.getItem(group);

            if (channelGroupObj == null) {
                SubscriptionItem chg = new SubscriptionItem(group, callback);

                channelGroupSubscriptions.addItem(chg);
            }
        }

        _subscribe_base(true);
    }

    private void _subscribe_base(boolean fresh) {
        _subscribe_base(fresh, false, null);
    }
    private void _subscribe_base(boolean fresh, boolean dar) {
        _subscribe_base(fresh, dar, null);
    }

    private void _subscribe_base(Worker worker) {
        _subscribe_base(false, false, worker);
    }

    private void _subscribe_base(boolean fresh, Worker worker) {
        _subscribe_base(fresh, false, worker);
    }

    private boolean isWorkerDead(HttpRequest hreq) {
        return (hreq == null || hreq.getWorker() == null)?false:hreq.getWorker()._die;
    }
    private void _subscribe_base(boolean fresh, boolean dar, Worker worker) {
        String channelString = channelSubscriptions.getItemString(WILDCARD_PRESENCE_SUFFIX);
        String groupString = channelGroupSubscriptions.getItemString();
        String[] channelsArray = channelSubscriptions.getItemNames(WILDCARD_PRESENCE_SUFFIX);
        String[] groupsArray = channelGroupSubscriptions.getItemNames();

        if (channelsArray.length <= 0 && groupsArray.length <= 0) {
            subscribeManager.resetHttpManager();
            return;
        }

        if (channelString == null) {
            callErrorCallbacks(channelsArray,
                    PubnubError.PNERROBJ_PARSING_ERROR);
            return;
        }

        if (channelString.equals("")) {
            channelString = ",";
        } else {
            channelString = PubnubUtil.urlEncode(channelString);
        }

        String[] urlComponents = { getPubnubUrl(), "subscribe",
                PubnubCore.this.SUBSCRIBE_KEY,
                channelString, "0", _timetoken
        };

        Hashtable params = PubnubUtil.hashtableClone(this.params);
        params.put("uuid", UUID);

        if (groupsArray.length > 0) {
            params.put("channel-group", groupString);
        }

        String st  = getState();
        if (st != null) params.put("state", st);

        if (HEARTBEAT > 5 && HEARTBEAT < 320) params.put("heartbeat", String.valueOf(HEARTBEAT));
        log.verbose("Subscribing with timetoken : " + _timetoken);

        HttpRequest hreq = new HttpRequest(urlComponents, params,
                new ResponseHandler() {

            public void handleResponse(HttpRequest hreq, String response) {

                /*
                 * Check if response has channel names. A JSON response
                 * with more than 2 items means the response contains
                 * the channel names as well. The channel names are in a
                 * comma delimted string. Call success callback on all
                 * he channels passing the corresponding response
                 * message.
                 */

                JSONArray jsa;
                try {
                    jsa = new JSONArray(response);

                    _timetoken = (!_saved_timetoken.equals("0") && isResumeOnReconnect()) ? _saved_timetoken
                            : jsa.get(1).toString();
                    log.verbose("Resume On Reconnect is "
                            + isResumeOnReconnect());
                    log.verbose("Saved Timetoken : " + _saved_timetoken);
                    log.verbose("In Response Timetoken : "
                            + jsa.get(1).toString());
                    log.verbose("Timetoken value set to " + _timetoken);
                    _saved_timetoken = "0";
                    log.verbose("Saved Timetoken reset to 0");

                    if (!hreq.isDar()) {
                        channelSubscriptions.invokeConnectCallbackOnItems(_timetoken);
                        channelGroupSubscriptions.invokeConnectCallbackOnItems(_timetoken);
                    } else {
                        channelSubscriptions.invokeReconnectCallbackOnItems(_timetoken);
                        channelGroupSubscriptions.invokeReconnectCallbackOnItems(_timetoken);
                    }

                    JSONArray messages = new JSONArray(jsa.get(0).toString());

                    if (jsa.length() == 4) {
                        /*
                         * Response has multiple channels or/and groups
                         */
                        String[] _groups = PubnubUtil.splitString(jsa.getString(2), ",");
                        String[] _channels = PubnubUtil.splitString(jsa.getString(3), ",");

                        for (int i = 0; i < _channels.length; i++) {
                            handleFourElementsSubscribeResponse(_groups[i], _channels[i], messages.get(i),
                                    _timetoken, hreq);
                        }
                    } else if(jsa.length() == 3) {
                        /*
                         * Response has multiple channels
                         */

                        String[] _channels = PubnubUtil.splitString(jsa.getString(2), ",");

                        for (int i = 0; i < _channels.length; i++) {
                            SubscriptionItem _channel = channelSubscriptions.getItem(_channels[i]);
                            Object message = messages.get(i);

                            if (_channel != null) {
                                invokeSubscribeCallback(_channel.name, _channel.callback,
                                        message, _timetoken, hreq);
                            }
                        }
                    } else if(jsa.length() < 3) {
                        /*
                         * Response for single channel Callback on
                         * single channel
                         */
                        SubscriptionItem _channel = channelSubscriptions.getFirstItem();

                        if (_channel != null) {
                            for (int i = 0; i < messages.length(); i++) {
                                Object message = messages.get(i);
                                invokeSubscribeCallback(_channel.name, _channel.callback,
                                        message, _timetoken, hreq);
                            }
                        }

                    }
                    if (hreq.isSubzero()) {
                        log.verbose("Response of subscribe 0 request. Need to do dAr process again");
                        _subscribe_base(false, hreq.isDar(), hreq.getWorker());
                    } else
                        _subscribe_base(false);
                } catch (JSONException e) {
                    if (hreq.isSubzero()) {
                        log.verbose("Response of subscribe 0 request. Need to do dAr process again");
                        _subscribe_base(false, hreq.isDar(), hreq.getWorker());
                    } else
                        _subscribe_base(false, hreq.getWorker());
                }

            }

            public void handleBackFromDar(HttpRequest hreq) {
                _subscribe_base(false, hreq.getWorker());
            }

            public void handleError(HttpRequest hreq, PubnubError error) {
                disconnectAndResubscribe(error);
            }

            public void handleTimeout(HttpRequest hreq) {
                log.verbose("Timeout Occurred, Calling disconnect callbacks on the channels");
                String timeoutTimetoken = (isResumeOnReconnect()) ? (_timetoken
                        .equals("0")) ? _saved_timetoken : _timetoken
                                : "0";
                log.verbose("Timeout Timetoken : " + timeoutTimetoken);
                channelSubscriptions.invokeDisconnectCallbackOnItems(timeoutTimetoken);
                channelGroupSubscriptions.invokeDisconnectCallbackOnItems(timeoutTimetoken);
                channelSubscriptions.invokeErrorCallbackOnItems(
                        PubnubError.getErrorObject(PubnubError.PNERROBJ_TIMEOUT, 1)
                );
                channelGroupSubscriptions.invokeErrorCallbackOnItems(
                        PubnubError.getErrorObject(PubnubError.PNERROBJ_TIMEOUT, 1)
                );
                // disconnectAndResubscribe();

                // channelSubscriptions.removeAllItems();
            }

            public String getTimetoken() {
                return _timetoken;
            }
        });
        if (_timetoken.equals("0")) {
            hreq.setSubzero(true);
            log.verbose("This is a subscribe 0 request");
        }
        hreq.setDar(dar);
        if ( worker != null && worker instanceof Worker )
            hreq.setWorker(worker);
        _request(hreq, subscribeManager, fresh);
    }

    /**
     * Handle 4-elements success response
     *
     * @param thirdString element of JSON response
     * @param fourthString element of JSON response
     * @param message from JSON response
     * @param timetoken from response
     * @param hreq request
     * @throws JSONException
     */
    private void handleFourElementsSubscribeResponse(String thirdString, String fourthString, Object message,
                                            String timetoken, HttpRequest hreq) throws JSONException {

        SubscriptionItem thirdChannelGroup = channelGroupSubscriptions.getItem(thirdString);
        SubscriptionItem thirdChannel = channelSubscriptions.getItem(thirdString);
        SubscriptionItem fourthChannel = channelSubscriptions.getItem(fourthString);

        if (isWorkerDead(hreq)) return;

        if (thirdString.equals(fourthString) && fourthChannel != null) {
            invokeSubscribeCallback(fourthString, fourthChannel.callback, message, timetoken, hreq);
        } else if (thirdString.endsWith("*")) {
            if (fourthChannel != null && fourthString.endsWith(PRESENCE_SUFFIX)) {
                invokeSubscribeCallback(fourthString, fourthChannel.callback, message, timetoken, hreq);
            } else if (thirdChannelGroup != null && !fourthString.endsWith(PRESENCE_SUFFIX)) {
                invokeSubscribeCallback(fourthString, thirdChannelGroup.callback, message, timetoken, hreq);
            } else if (thirdChannel != null && thirdString.endsWith(WILDCARD_SUFFIX)) {
                invokeSubscribeCallback(fourthString, thirdChannel.callback, message, timetoken, hreq);
            } else {
                System.out.println("ERROR: Unable to handle wildcard response: " + message);
            }
        } else if (!thirdString.equals(fourthString) && thirdChannelGroup != null) {
            invokeSubscribeCallback(fourthString, thirdChannelGroup.callback,
                    message, timetoken, hreq);
        } else {
            System.out.println("ERROR: Unable to handle response: " + message);
        }
    }

    private void invokeSubscribeCallback(String channel, Callback callback, Object message, String timetoken,
                                         HttpRequest hreq) throws JSONException {
        if (CIPHER_KEY.length() > 0
                && !channel
                .endsWith(PRESENCE_SUFFIX)) {
            PubnubCrypto pc = new PubnubCrypto(
                    CIPHER_KEY, IV);
            try {
                message = pc.decrypt(message.toString());
                if (!isWorkerDead(hreq)) callback
                        .invokeSuccessCallbacks(
                                channel,
                                PubnubUtil.parseJSON(PubnubUtil.stringToJSON(message.toString())), timetoken);
            } catch (IllegalStateException e) {
                if (!isWorkerDead(hreq)) callback
                        .errorCallback(
                                channel,
                                PubnubError.getErrorObject(
                                        PubnubError.PNERROBJ_DECRYPTION_ERROR, 12,
                                        message.toString()));
            } catch (PubnubException e) {
                if (!isWorkerDead(hreq)) callback
                        .errorCallback(
                                channel,
                                getPubnubError(e, PubnubError.PNERROBJ_DECRYPTION_ERROR, 16,
                            message.toString() + " : " + e.toString()));
            } catch (Exception e) {
                if (!isWorkerDead(hreq)) callback
                        .errorCallback(
                                channel,
                                PubnubError.getErrorObject(
                                        PubnubError.PNERROBJ_DECRYPTION_ERROR, 15,
                                        message.toString() + " : " + e.toString()));
            }
        } else {
            if (!isWorkerDead(hreq)) callback.invokeSuccessCallbacks(
                    channel,
                    PubnubUtil.parseJSON(message), timetoken);
        }
    }

    /**
     * @param hreq
     * @param connManager
     * @param abortExisting
     */
    private void _request(final HttpRequest hreq, RequestManager connManager,
            boolean abortExisting) {
        if (abortExisting) {
            connManager.resetHttpManager();
        }
        connManager.queue(hreq);
    }

    /**
     * @param hreq
     * @param simpleConnManager
     */
    protected void _request(final HttpRequest hreq,
            RequestManager simpleConnManager) {
        _request(hreq, simpleConnManager, false);
    }

    private int getRandom() {
        return Math.abs(this.generator.nextInt());
    }

    private void changeOrigin() {
        this.ORIGIN_STR = null;
        this.HOSTNAME_SUFFIX = getRandom();
    }

    private void resubscribe() {
        changeOrigin();
        if (!_timetoken.equals("0"))
            _saved_timetoken = _timetoken;
        _timetoken = "0";
        log.verbose("Before Resubscribe Timetoken : " + _timetoken);
        log.verbose("Before Resubscribe Saved Timetoken : " + _saved_timetoken);
        _subscribe_base(true, true);
    }

    private void resubscribe(String timetoken) {
        changeOrigin();
        if (!timetoken.equals("0"))
            _saved_timetoken = timetoken;
        _timetoken = "0";
        log.verbose("Before Resubscribe Timetoken : " + _timetoken);
        log.verbose("Before Resubscribe Saved Timetoken : " + _saved_timetoken);
        _subscribe_base(true, true);
    }

    /**
     * Disconnect from all channels, and resubscribe
     *
     */
    public void disconnectAndResubscribeWithTimetoken(String timetoken) {
        disconnectAndResubscribeWithTimetoken(timetoken,
                PubnubError.PNERROBJ_DISCONN_AND_RESUB);
    }

    /**
     * Disconnect from all channels, and resubscribe
     *
     */
    public void disconnectAndResubscribeWithTimetoken(String timetoken,
            PubnubError error) {
        log.verbose("Received disconnectAndResubscribeWithTimetoken");
        channelSubscriptions.invokeErrorCallbackOnItems(error);
        channelGroupSubscriptions.invokeErrorCallbackOnItems(error);
        resubscribe(timetoken);
    }

    /**
     * Disconnect from all channels, and resubscribe
     *
     */
    public void disconnectAndResubscribe() {
        disconnectAndResubscribe(PubnubError.PNERROBJ_DISCONNECT);
    }

    /**
     * Disconnect from all channels, and resubscribe
     *
     */
    public void disconnectAndResubscribe(PubnubError error) {
        log.verbose("Received disconnectAndResubscribe");
        channelSubscriptions.invokeErrorCallbackOnItems(error);
        channelGroupSubscriptions.invokeErrorCallbackOnItems(error);
        resubscribe();
    }

    /**
     * This method returns array of channel names, currently subscribed to
     *
     * @return Array of channel names
     */
    public String[] getSubscribedChannelsArray() {
        return channelSubscriptions.getItemNames();
    }

    /**
     * Sets origin value, default is "pubsub"
     *
     * @param origin
     *            Origin value
     */
    public void setOrigin(String origin) {
        this.HOSTNAME = origin;
    }

    /**
     * Returns origin
     * @return origin
     */
    public String getOrigin() {
        return this.HOSTNAME;
    }

    /**
     * Sets domain value, default is "pubnub.com"
     *
     * @param domain
     *            Domain value
     */
    public void setDomain(String domain) {
        this.DOMAIN = domain;
    }

    /**
     * Returns domain
     * @return domain
     */
    public String getDomain() {
        return this.DOMAIN;
    }

    /**
     * This method returns auth key. Return null if not set
     *
     * @return Auth Key. null if auth key not set
     */
    public String getAuthKey() {
        return this.AUTH_STR;
    }

    /**
     * This method sets auth key.
     *
     * @param authKey
     *            . 0 length string or null unsets auth key
     */
    public void setAuthKey(String authKey) {

        this.AUTH_STR = authKey;
        if (authKey == null || authKey.length() == 0) {
            params.remove("auth");
        } else {
            params.put("auth", this.AUTH_STR);
        }
        resubscribe();
    }

    /**
     * This method unsets auth key.
     *
     */
    public void unsetAuthKey() {
        this.AUTH_STR = null;
        params.remove("auth");
        resubscribe();
    }

}
