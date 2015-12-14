package com.pubnub.api;

import java.io.IOException;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

abstract class PubnubCoreSync extends PubnubCore implements PubnubSyncInterface {


	private HttpClient httpClient;

    protected HttpResponse fetch(String url) throws IOException, PubnubException {
        if (httpClient == null)
            return null;
        return httpClient.fetch(url);
    }

   public PubnubCoreSync(String publish_key, String subscribe_key,
           String secret_key, String cipher_key, boolean ssl_on, String initialization_vector) {
       super(publish_key, subscribe_key, secret_key, cipher_key, ssl_on, initialization_vector);
       init();
   }

   public PubnubCoreSync(String publish_key, String subscribe_key,
           String secret_key, String cipher_key, boolean ssl_on) {
       super(publish_key, subscribe_key, secret_key, cipher_key, ssl_on);
       init();
   }

   public PubnubCoreSync(String publish_key, String subscribe_key,
           String secret_key, boolean ssl_on) {
       super(publish_key, subscribe_key, secret_key, ssl_on);
       init();
   }


   public PubnubCoreSync(String publish_key, String subscribe_key) {
       super(publish_key, subscribe_key);
       init();
   }

   public PubnubCoreSync(String publish_key, String subscribe_key, boolean ssl) {
       super(publish_key, subscribe_key, ssl);
       init();
   }

   public PubnubCoreSync(String publish_key, String subscribe_key,
           String secret_key) {
       super(publish_key, subscribe_key, secret_key);
       init();
   }

    protected void init() {
        // sync client

        Hashtable headers = new Hashtable();
        headers.put("V", VERSION);
        headers.put("Accept-Encoding", "gzip");
        headers.put("User-Agent", getUserAgent());
        httpClient = HttpClient.getClient(5000, 5000, headers);

    }

   public Object publish(String channel, JSONObject message, boolean storeInHistory) {
	   return publish(channel, (Object)message, storeInHistory);
   }

   public Object publish(String channel, JSONArray message, boolean storeInHistory) {
	   return publish(channel, (Object)message, storeInHistory);
   }

   public Object publish(String channel, String message, boolean storeInHistory) {
	   return publish(channel, (Object)message, storeInHistory);
   }

   public Object publish(String channel, Integer message, boolean storeInHistory) {
	   return publish(channel, (Object) message, storeInHistory);
   }

   public Object publish(String channel, Double message, boolean storeInHistory) {
	   return publish(channel, (Object) message, storeInHistory);
   }

   
   protected Object publish(String channel, Object message, boolean storeInHistory) {
       Hashtable args = new Hashtable();
       args.put("channel", channel);
       args.put("message", message);
       args.put("storeInHistory", (storeInHistory)?"":"0");
       return _publish(args, true);
   }

   public Object publish(String channel, JSONObject message) {
	   return publish(channel, (Object) message);
   }

   public Object publish(String channel, JSONArray message) {
	   return publish(channel, (Object)message);
   }

   public Object publish(String channel, String message) {
	   return publish(channel, (Object)message);
   }

   public Object publish(String channel, Integer message) {
	   return publish(channel, (Object) message);
   }

   public Object publish(String channel, Double message) {
	   return publish(channel, (Object) message);
   }

   protected Object publish(String channel, Object message) {
       Hashtable args = new Hashtable();
       args.put("channel", channel);
       args.put("message", message);
       return _publish(args, true);
   }


	public JSONObject channelGroupAddChannel(String group, String[] channels) {
        return (JSONObject) _channelGroupUpdate("add", group, channels, null, true);
	}

	public JSONObject channelGroupAddChannel(String group, String channel) {
        return channelGroupAddChannel(group, new String[]{channel});
	}

	public JSONObject channelGroupHereNow(String[] groups, boolean state, boolean uuids) {
        return (JSONObject) _hereNow(null, groups, state, uuids, null, true);
	}

	public JSONObject channelGroupHereNow(String group, boolean state, boolean uuids) {
        return channelGroupHereNow(new String[]{group}, state, uuids);
	}

	public JSONObject channelGroupHereNow(String group) {
        return channelGroupHereNow(group, false, true);
	}
	

	public JSONObject channelGroupListChannels(String group) {
        return (JSONObject) _channelGroupListChannels(group, null, true);
	}

	public JSONObject channelGroupListGroups() {
        return (JSONObject) _channelGroupListGroups(null, null, true);
	}
	

	public JSONObject channelGroupListGroups(String namespace) {
        return (JSONObject) _channelGroupListGroups(namespace, null, true);
	}

	public JSONObject channelGroupListNamespaces() {
        return (JSONObject) _channelGroupListNamespaces(null, true);
	}

	public JSONObject channelGroupRemoveChannel(String group, String[] channels) {
        return (JSONObject) _channelGroupUpdate("remove", group, channels, null, true);
	}

	public JSONObject channelGroupRemoveChannel(String group, String channel) {
        return (JSONObject) _channelGroupUpdate("remove", group, new String[]{channel}, null, true);
	}

	public JSONObject channelGroupRemoveGroup(String group) {
        return (JSONObject) _channelGroupRemoveGroup(group, null, true);
	}

	public JSONObject channelGroupRemoveNamespace(String namespace) {
        return (JSONObject) _channelGroupRemoveNamespace(namespace, null, true);
	}

	public JSONObject getState(String channel, String uuid) {
        return (JSONObject) _getState(channel, null, uuid, null, true);
	}

    public JSONObject channelGroupGetState(String group, String uuid) {
        return (JSONObject) _getState(null, group, uuid, null, true);
    }

	public JSONObject hereNow(boolean state, boolean uuids) {
        return (JSONObject) _hereNow(null, null, state, uuids, null, true);
	}

	public JSONObject hereNow(String[] channels, String[] channelGroups, boolean state, boolean uuids) {
        return (JSONObject) _hereNow(channels, channelGroups, state, uuids, null, true);
	}

	public JSONObject hereNow(String channel, boolean state, boolean uuids) {
        return (JSONObject) _hereNow(new String[]{channel}, null, state, uuids, null, true);
	}

	public JSONObject hereNow(String channel) {
        return (JSONObject) _hereNow(new String[]{channel}, null, false, true, null, true);
	}

	public Object history(String channel, boolean reverse) {
        return _history(channel, -1, -1, -1, false, false, null, true);
	}

	public Object history(String channel, boolean includeTimetoken, int count) {
        return _history(channel, -1, -1, -1, false, includeTimetoken, null, true);
	}

	public Object history(String channel, int count, boolean reverse) {
        return _history(channel, -1, -1, count, reverse, false, null, true);
	}

	public Object history(String channel, int count) {
        return _history(channel, -1, -1, count, false, false, null, true);
	}

	public Object history(String channel, long start, boolean reverse) {
        return _history(channel, start, -1, -1, reverse, false, null, true);
	}

	public Object history(String channel, long start, int count, boolean reverse) {
        return _history(channel, start, -1, count, reverse, false, null, true);
	}

	public Object history(String channel, long start, int count) {
        return _history(channel, start, -1, count, false, false, null, true);
	}
	

	public Object history(String channel, long start, long end, boolean reverse) {
        return _history(channel, start, end, -1, reverse, false, null, true);
	}

	public Object history(String channel, long start, long end) {
        return _history(channel, start, end, -1, false, false, null, true);
	}

	public Object history(String channel, long start, long end, int count, boolean reverse, boolean includeTimetoken) {
        return _history(channel, start, end, count, reverse, includeTimetoken, null, true);
	}

	public Object history(String channel, long start, long end, int count, boolean reverse) {
        return _history(channel, start, end, count, reverse, false, null, true);
	}

	public Object history(String channel, long start, long end, int count) {
        return _history(channel, start, end, count ,false, false, null, true);
	}

	public JSONObject pamAudit() {
        return (JSONObject) _pamAudit(null, null, true);
	}

	public JSONObject pamAudit(String channel) {
        return (JSONObject) _pamAudit(channel, null, true);
	}

	public JSONObject pamAudit(String channel, String auth_key) {

        return (JSONObject) _pamAudit(channel, auth_key, null, true);
	}

	public JSONObject pamAuditChannelGroup(String group) {
        return (JSONObject) _pamAuditChannelGroup(group, null, null, true);
	}

	public JSONObject pamAuditChannelGroup(String group, String auth_key) {
        return (JSONObject) _pamAuditChannelGroup(group, auth_key, null, true);
	}

	public JSONObject pamGrant(String channel, boolean read, boolean write) {
        return (JSONObject) _pamGrant(channel, null,  read, write, -1, null, true);
	}

	public JSONObject pamGrant(String channel, boolean read, boolean write, int ttl) {
        return (JSONObject) _pamGrant(channel, null, read, write, -1, null, true);
	}

	public JSONObject pamGrant(String channel, String auth_key, boolean read, boolean write) {
        return (JSONObject) _pamGrant(channel, auth_key, read, write, -1, null, true);
	}

	public JSONObject pamGrant(String channel, String auth_key, boolean read, boolean write, int ttl) {
        return (JSONObject) _pamGrant(channel, auth_key, read, write, ttl, null, true);
	}

	public JSONObject pamGrantChannelGroup(String group, boolean read, boolean management) {
        return (JSONObject) _pamGrantChannelGroup(group, null, read, false, -1, null, true);
	}

	public JSONObject pamGrantChannelGroup(String group, boolean read, boolean management, int ttl) {
        return (JSONObject) _pamGrantChannelGroup(group, null, read, management, ttl, null, true);
	}

	public JSONObject pamGrantChannelGroup(String group, String auth_key, boolean read, boolean management) {
        return (JSONObject) _pamGrantChannelGroup(group, auth_key, read, management, -1, null, true);
	}

	public JSONObject pamGrantChannelGroup(String group, String auth_key, boolean read, boolean management, int ttl) {
        return (JSONObject) _pamGrantChannelGroup(group, auth_key, read, management, ttl, null, true);
	}

	public JSONObject pamRevoke(String channel) {
        return (JSONObject) _pamGrant(channel, null, false, false, -1, null, true);
	}

	public JSONObject pamRevoke(String channel, String auth_key) {
        return (JSONObject) _pamGrant(channel, auth_key, false, false, -1, null, true);
	}

	public JSONObject pamRevokeChannelGroup(String group) {
        return (JSONObject) _pamGrantChannelGroup(group, null, false, false, -1, null, true);
	}

	public JSONObject pamRevokeChannelGroup(String group, String auth_key) {

        return (JSONObject) _pamGrantChannelGroup(group, auth_key, false, false, -1, null, true);
	}

	public JSONArray time() {
		return _time(null, true);
	}

	public JSONObject whereNow() {
		return _whereNow(this.UUID, null, true);
	}

	public JSONObject whereNow(String uuid) {
		return _whereNow(uuid, null, true);
	}
	
    
}
