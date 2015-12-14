package com.pubnub.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;



abstract class PubnubCore implements PubnubInterface {

    protected static String VERSION = "";
    protected volatile boolean CACHE_BUSTING = true;

    protected String HOSTNAME = "pubsub";
    protected int HOSTNAME_SUFFIX = 1;
    protected String DOMAIN = "pubnub.com";
    protected String ORIGIN_STR = null;
    protected String PUBLISH_KEY = "";
    protected String SUBSCRIBE_KEY = "";
    protected String SECRET_KEY = "";
    protected String CIPHER_KEY = "";
    protected String IV = null;
    protected volatile String AUTH_STR = null;
    private Random generator = new Random();

    protected Hashtable params;

    private boolean SSL = true;
    protected String UUID = null;


    protected SubscribeManager subscribeManager;
    protected NonSubscribeManager nonSubscribeManager;

    protected abstract String getUserAgent();
    protected HttpResponse fetch(String url) throws IOException, PubnubException {
        return null;
    }


    //abstract String uuid();


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

    public void setOrigin(String origin) {
        this.HOSTNAME = origin;
    }

    public String getOrigin() {
        return this.HOSTNAME;
    }

    public void setDomain(String domain) {
        this.DOMAIN = domain;
    }

    public String getDomain() {
        return this.DOMAIN;
    }

    public String getAuthKey() {
        return this.AUTH_STR;
    }

    public void setAuthKey(String authKey) {

        this.AUTH_STR = authKey;
        if (authKey == null || authKey.length() == 0) {
            params.remove("auth");
        } else {
            params.put("auth", this.AUTH_STR);
        }
    }

    public void unsetAuthKey() {
        this.AUTH_STR = null;
        params.remove("auth");
    }


    protected int getRandom() {
        return Math.abs(this.generator.nextInt());
    }

    protected Callback voidCallback = new Callback()
    {public void successCallback(String channel, Object message) {}};

    protected Callback getWrappedCallback(Callback callback){
        if (callback == null) {
            return voidCallback;
        } else
            return callback;
    }


    protected PubnubError getPubnubError(PubnubException px, PubnubError error, int code, String message) {
        PubnubError pe = px.getPubnubError();
        if (pe == null) {
            pe = PubnubError.getErrorObject(error, code, message);
        }
        return pe;
    }



    protected void decryptJSONArray(JSONArray messages) throws JSONException, IllegalStateException, IOException, PubnubException {

        if (CIPHER_KEY.length() > 0) {
            for (int i = 0; i < messages.length(); i++) {
                PubnubCrypto pc = new PubnubCrypto(CIPHER_KEY, IV);

                String message;
                message = pc.decrypt(messages.get(i).toString());
                messages.put(i, PubnubUtil.stringToJSON(message));
            }
        }
    }

    public PubnubCore(String publish_key, String subscribe_key,
                      String secret_key, String cipher_key, boolean ssl_on, String initialization_vector) {
        this.init(publish_key, subscribe_key, secret_key, cipher_key, ssl_on, initialization_vector);
    }

    public PubnubCore(String publish_key, String subscribe_key,
                      String secret_key, String cipher_key, boolean ssl_on) {
        this.init(publish_key, subscribe_key, secret_key, cipher_key, ssl_on);
    }

    public PubnubCore(String publish_key, String subscribe_key,
                      String secret_key, boolean ssl_on) {
        this.init(publish_key, subscribe_key, secret_key, "", ssl_on);
    }

    public PubnubCore(String publish_key, String subscribe_key) {
        this.init(publish_key, subscribe_key, "", "", false);
    }

    public PubnubCore(String publish_key, String subscribe_key, boolean ssl) {
        this.init(publish_key, subscribe_key, "", "", ssl);
    }

    public PubnubCore(String publish_key, String subscribe_key,
                      String secret_key) {
        this.init(publish_key, subscribe_key, secret_key, "", false);
    }


    private void init(String publish_key, String subscribe_key,
                      String secret_key, String cipher_key, boolean ssl_on) {
        this.init(publish_key, subscribe_key, secret_key, cipher_key, ssl_on, null);
    }


    private void init(String publish_key, String subscribe_key,
                      String secret_key, String cipher_key, boolean ssl_on, String initialization_vector) {
        this.PUBLISH_KEY = publish_key;
        this.SUBSCRIBE_KEY = subscribe_key;
        this.SECRET_KEY = secret_key;
        this.CIPHER_KEY = cipher_key;
        this.SSL = ssl_on;

        if (UUID == null)
            UUID = uuid();


        if (params == null)
            params = new Hashtable();

        params.put("pnsdk", getUserAgent());



    }


    public void setUUID(String uuid) {
        this.UUID = uuid;
    }


    public String getUUID() {
        return this.UUID;
    }


    protected Object _publish(Hashtable args, boolean sync) {

        final String channel = (String) args.get("channel");
        final Object message = args.get("message");
        Callback cb = (Callback) args.get("callback");

        String storeInHistory = (String) args.get("storeInHistory");
        String msgStr = message.toString();
        Hashtable parameters = PubnubUtil.hashtableClone(params);

        if (storeInHistory != null && storeInHistory.length() > 0) parameters.put("store", storeInHistory);



        final Callback callback = getWrappedCallback(cb);

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
                return null;
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

        return _request(hreq, (sync)?null:nonSubscribeManager);

    }

    JSONObject _whereNow(final String uuid, Callback callback, boolean sync) {
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
        return (JSONObject) _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _request(final HttpRequest hreq, RequestManager connManager,
                            boolean abortExisting) {
        if (abortExisting) {
            connManager.resetHttpManager();
        }
        if (connManager == null) {
            try {
                HttpResponse resp = fetch(hreq.getUrl());
                return PubnubUtil.stringToJSON(resp.getResponse());

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            } catch (PubnubException e) {
            	//System.out.println(e);
            	return e.getErrorJsonObject();
            }
        }
        connManager.queue(hreq);
        return null;
    }

    protected Object _request(final HttpRequest hreq,
                              RequestManager simpleConnManager) {
        return _request(hreq, simpleConnManager, false);
    }

    protected JSONArray _time(Callback callback, boolean sync) {
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

        return (JSONArray) _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected void keepOnlyPluralSubscriptionItems(Hashtable args) {
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

    protected boolean inputsValid(Hashtable args) throws PubnubException {
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

    protected Object _history(final String channel, long start, long end,
                           int count, boolean reverse, boolean includeTimetoken, Callback callback, boolean sync) {
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
        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _hereNow(String[] channels, String[] channelGroups, boolean state,
                           boolean uuids, Callback callback, boolean sync) {

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

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }




    protected boolean validateInput(String name, Object input, Callback callback) {

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


    protected Object _setState(Subscriptions sub, String channel,
                               String group, String uuid, JSONObject state, Callback callback, boolean sync) {
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

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }


    protected Object _getState(String channel, String group, String uuid, Callback callback, boolean sync) {
        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);
        String encodedChannel = channel == null ? "," : PubnubUtil.urlEncode(channel);

        String[] urlArgs = { getPubnubUrl(), "v2", "presence", "sub-key",
                this.SUBSCRIBE_KEY, "channel", encodedChannel,
                "uuid", PubnubUtil.urlEncode(uuid)
        };

        if (group != null) parameters.put("channel-group", group);

        HttpRequest hreq = new HttpRequest(urlArgs, parameters,
                new ResponseHandler() {
                    public void handleResponse(HttpRequest hreq, String response) {
                        invokeCallback("", response, "payload", cb, 1 );
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback("", error);
                    }
                });

        return _request(hreq, (sync)?null:nonSubscribeManager);
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

    protected Object _channelGroupRemoveNamespace(String namespace, Callback callback, boolean sync) {
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

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _channelGroupListGroups(String namespace, Callback callback, boolean sync) {
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

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _channelGroupListChannels(String group, Callback callback, boolean sync) {
        final Callback cb = getWrappedCallback(callback);
        ChannelGroup channelGroup;
        String[] url;

        try {
            channelGroup =  new ChannelGroup(group);
        } catch (PubnubException e) {
            cb.errorCallback(null, PubnubError.PNERROBJ_CHANNEL_GROUP_PARSING_ERROR);
            return null;
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

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _channelGroupUpdate(String action, String group,
                                         String[] channels, final Callback callback, boolean sync) {
        final Callback cb = getWrappedCallback(callback);
        ChannelGroup channelGroup;
        String[] url;

        try {
            channelGroup =  new ChannelGroup(group);
        } catch (PubnubException e) {
            cb.errorCallback(null, PubnubError.PNERROBJ_CHANNEL_GROUP_PARSING_ERROR);
            return null;
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

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _channelGroupRemoveGroup(String group, Callback callback, boolean sync) {
        final Callback cb = getWrappedCallback(callback);
        ChannelGroup channelGroup;
        String[] url;

        try {
            channelGroup =  new ChannelGroup(group);
        } catch (PubnubException e) {
            cb.errorCallback(null, PubnubError.PNERROBJ_CHANNEL_GROUP_PARSING_ERROR);
            return null;
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

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _channelGroupListNamespaces(Callback callback, boolean sync) {
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

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _disablePushNotificationsOnChannels(
            final String[] channels, String gcmRegistrationId, final Callback callback, boolean sync) {
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
                        cb.successCallback("", jsarr);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback("", error);
                        return;
                    }
                });

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _requestPushNotificationEnabledChannelsForDeviceRegistrationId(
            String gcmRegistrationId, final Callback callback, boolean sync) {
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
                        cb.successCallback("", jsarr);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback("", error);
                        return;
                    }
                });
        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _removeAllPushNotificationsForDeviceRegistrationId(
            String gcmRegistrationId, final Callback callback, boolean sync) {
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
                        cb.successCallback("", jsarr);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback("", error);
                        return;
                    }
                });
        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _enablePushNotificationsOnChannels(final String[] channels,
                                                        String gcmRegistrationId, final Callback callback, boolean sync) {
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
                        cb.successCallback("", jsarr);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback("", error);
                        return;
                    }
                });

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected String pamSign(String key, String data) throws PubnubException {
        return null;
    }

    protected Object _pamAuditChannelGroup(final String group, String auth_key, Callback callback, boolean sync) {
        String signature;
        final Callback cb = getWrappedCallback(callback);

        Hashtable parameters = PubnubUtil.hashtableClone(params);
        parameters.remove("auth");

        int timestamp = (int) ((new Date().getTime()) / 1000);

        if (this.SECRET_KEY.length() == 0) {
            callback.errorCallback(group, PubnubError.getErrorObject(PubnubError.PNERROBJ_SECRET_KEY_MISSING, 3));
            return null;
        }

        String sign_input = this.SUBSCRIBE_KEY + "\n" + this.PUBLISH_KEY + "\n" + "audit" + "\n";

        if (auth_key != null && auth_key.length() > 0)
            sign_input += "auth=" + auth_key + "&"  ;

        sign_input += "channel-group=" + PubnubUtil.urlEncode(group) + "&"
                + "pnsdk=" + PubnubUtil.urlEncode(getUserAgent()) + "&"
                + "timestamp=" + timestamp;

        try {
            signature = pamSign(this.SECRET_KEY, sign_input);
        } catch (PubnubException e1) {
            callback.errorCallback(group, e1.getPubnubError());
            return null;
        }

        parameters.put("timestamp", String.valueOf(timestamp));
        parameters.put("signature", signature);
        parameters.put("channel-group", group);

        if (auth_key != null && auth_key.length() > 0 ) parameters.put("auth", auth_key);

        String[] urlComponents = {getPubnubUrl(), "v1", "auth", "audit", "sub-key",
                this.SUBSCRIBE_KEY
        };

        HttpRequest hreq = new HttpRequest(urlComponents, parameters,
                new ResponseHandler() {
                    public void handleResponse(HttpRequest hreq, String response) {
                        invokeCallback(group, response, "payload", cb, 6);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback(group, error);
                    }
                });

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _pamAudit(final String channel, String auth_key,
                         Callback callback, boolean sync) {

        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);

        String signature = "0";

        int timestamp = (int) ((new Date().getTime()) / 1000);

        if (this.SECRET_KEY.length() == 0) {
            callback.errorCallback(channel,
                    PubnubError.getErrorObject(PubnubError.PNERROBJ_SECRET_KEY_MISSING, 4));
            return null;
        }

        String sign_input = this.SUBSCRIBE_KEY + "\n" + this.PUBLISH_KEY + "\n"
                + "audit" + "\n" + "auth=" + PubnubUtil.urlEncode(auth_key) + "&" + "channel="
                + PubnubUtil.urlEncode(channel) + "&" + "pnsdk=" + PubnubUtil.urlEncode(getUserAgent()) + "&" + "timestamp=" + timestamp;


        try {
            signature = pamSign(this.SECRET_KEY, sign_input);
        } catch (PubnubException e1) {
            callback.errorCallback(channel,
                    e1.getPubnubError());
            return null;
        }

        parameters.put("timestamp", String.valueOf(timestamp));
        parameters.put("signature", signature);
        parameters.put("channel", channel);
        parameters.put("auth", auth_key);

        String[] urlComponents = { getPubnubUrl(), "v1", "auth", "audit", "sub-key",
                this.SUBSCRIBE_KEY
        };

        HttpRequest hreq = new HttpRequest(urlComponents, parameters,
                new ResponseHandler() {
                    public void handleResponse(HttpRequest hreq, String response) {
                        invokeCallback(channel, response, "payload", cb, 2);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback(channel, error);
                        return;
                    }
                });

        return _request(hreq, (sync)?null:nonSubscribeManager);

    }

    protected Object _pamAudit(final String channel,
                         Callback callback, boolean sync) {

        final Callback cb = getWrappedCallback(callback);

        Hashtable parameters = PubnubUtil.hashtableClone(params);
        parameters.remove("auth");

        String signature = "0";

        int timestamp = (int) ((new Date().getTime()) / 1000);

        if (this.SECRET_KEY.length() == 0) {
            callback.errorCallback(channel,
                    PubnubError.getErrorObject(PubnubError.PNERROBJ_SECRET_KEY_MISSING , 3));
            return null;
        }
        String sign_input = null;
        if (channel != null) {
            sign_input = this.SUBSCRIBE_KEY + "\n" + this.PUBLISH_KEY + "\n"
                    + "audit" + "\n" + "channel="
                    + PubnubUtil.pamEncode(channel) + "&" + "pnsdk=" + PubnubUtil.pamEncode(getUserAgent()) + "&" + "timestamp=" + timestamp;
        } else {
            sign_input = this.SUBSCRIBE_KEY + "\n" + this.PUBLISH_KEY + "\n"
                    + "audit" + "\n" + "pnsdk=" + PubnubUtil.pamEncode(getUserAgent()) + "&"
                    + "timestamp=" + timestamp;
        }

        try {
            signature = pamSign(this.SECRET_KEY, sign_input);
        } catch (PubnubException e1) {
            callback.errorCallback(channel,
                    e1.getPubnubError());
            return null;
        }

        parameters.put("timestamp", String.valueOf(timestamp));
        parameters.put("signature", signature);
        if (channel != null) parameters.put("channel", channel);

        String[] urlComponents = { getPubnubUrl(), "v1", "auth", "audit", "sub-key",
                this.SUBSCRIBE_KEY
        };

        HttpRequest hreq = new HttpRequest(urlComponents, parameters,
                new ResponseHandler() {
                    public void handleResponse(HttpRequest hreq, String response) {
                        invokeCallback(channel, response, "payload", cb, 6);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback(channel, error);
                        return;
                    }
                });

        return _request(hreq, (sync)?null:nonSubscribeManager);

    }

    protected Object _pamGrantChannelGroup(final String group, String auth_key, boolean read, boolean management, int ttl,
                                     Callback callback, boolean sync) {
        String signature;
        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);

        String r = (read) ? "1" : "0";
        String m = (management) ? "1" : "0";

        int timestamp = (int) ((new Date().getTime()) / 1000);

        if (this.SECRET_KEY.length() == 0) {
            callback.errorCallback(group, PubnubError.getErrorObject(PubnubError.PNERROBJ_SECRET_KEY_MISSING, 1));
            return null;
        }

        String sign_input = this.SUBSCRIBE_KEY + "\n" + this.PUBLISH_KEY + "\n" + "grant" + "\n";

        if (auth_key != null && auth_key.length() > 0)
            sign_input += "auth=" + PubnubUtil.pamEncode(auth_key) + "&"  ;

        sign_input += "channel-group=" + PubnubUtil.pamEncode(group) + "&"
                + "m=" + m + "&"
                + "pnsdk=" + PubnubUtil.pamEncode(getUserAgent()) + "&"
                + "r=" + r + "&"
                + "timestamp=" + timestamp
                + ((ttl >= -1)?"&" + "ttl=" + ttl:"");

        try {
            signature = pamSign(this.SECRET_KEY, sign_input);
        } catch (PubnubException e1) {
            callback.errorCallback(group, e1.getPubnubError());
            return null;
        }

        parameters.put("timestamp", String.valueOf(timestamp));
        parameters.put("signature", signature);
        parameters.put("r", r);
        parameters.put("m", m);
        parameters.put("channel-group", group);

        if (ttl >= -1) parameters.put("ttl", String.valueOf(ttl));
        if (auth_key != null && auth_key.length() > 0 ) parameters.put("auth", auth_key);

        String[] urlComponents = { getPubnubUrl(), "v1", "auth", "grant", "sub-key",
                this.SUBSCRIBE_KEY
        };

        HttpRequest hreq = new HttpRequest(urlComponents, parameters,
                new ResponseHandler() {
                    public void handleResponse(HttpRequest hreq, String response) {
                        invokeCallback(group, response, "payload", cb, 4);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback(group, error);
                    }
                });

        return _request(hreq, (sync)?null:nonSubscribeManager);
    }

    protected Object _pamGrant(final String channel, String auth_key, boolean read,
                         boolean write, int ttl, Callback callback, boolean sync) {
        final Callback cb = getWrappedCallback(callback);
        Hashtable parameters = PubnubUtil.hashtableClone(params);

        String r = (read) ? "1" : "0";
        String w = (write) ? "1" : "0";

        String signature = "0";

        int timestamp = (int) ((new Date().getTime()) / 1000);

        if (this.SECRET_KEY.length() == 0) {
            callback.errorCallback(channel,
                    PubnubError.getErrorObject(PubnubError.PNERROBJ_SECRET_KEY_MISSING, 1));
            return null;
        }

        String sign_input = this.SUBSCRIBE_KEY + "\n" + this.PUBLISH_KEY + "\n" + "grant" + "\n" ;

        if (auth_key != null && auth_key.length() > 0)
            sign_input += "auth=" + PubnubUtil.pamEncode(auth_key) + "&"  ;

        sign_input += "channel=" + PubnubUtil.pamEncode(channel) + "&" + "pnsdk=" + PubnubUtil.pamEncode(getUserAgent()) + "&" + "r=" + r + "&" + "timestamp=" + timestamp
                + ((ttl >= -1)?"&" + "ttl=" + ttl:"")
                + "&" + "w=" + w;


        try {
            signature = pamSign(this.SECRET_KEY, sign_input);
        } catch (PubnubException e1) {
            callback.errorCallback(channel,
                    e1.getPubnubError());
            return null;
        }


        parameters.put("w", w);
        parameters.put("timestamp", String.valueOf(timestamp));
        parameters.put("signature", signature);
        parameters.put("r", r);
        parameters.put("channel", channel);

        if (auth_key != null && auth_key.length() > 0 ) parameters.put("auth", auth_key);
        if (ttl >= -1) parameters.put("ttl", String.valueOf(ttl));

        String[] urlComponents = { getPubnubUrl(), "v1", "auth", "grant", "sub-key",
                this.SUBSCRIBE_KEY
        };

        HttpRequest hreq = new HttpRequest(urlComponents, parameters,
                new ResponseHandler() {
                    public void handleResponse(HttpRequest hreq, String response) {
                        invokeCallback(channel, response, "payload", cb, 4);
                    }

                    public void handleError(HttpRequest hreq, PubnubError error) {
                        cb.errorCallback(channel, error);
                        return;
                    }
                });

        return _request(hreq, (sync)?null:nonSubscribeManager);

    }


}
