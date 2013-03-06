package com.pubnub.api;

import java.util.Enumeration;
import java.util.Hashtable;

import org.json.JSONArray;

/**
 * @author PubnubCore
 * 
 */
class Subscriptions {
    private Hashtable channels;

    public Subscriptions() {
        channels = new Hashtable();
    }

    public void addChannel(Channel channel) {
        channels.put(channel.name, channel);
    }

    public void removeChannel(String name) {
        channels.remove(name);
    }

    public void removeAllChannels() {
        channels.clear();
    }

    public Channel getFirstChannel() {
        Channel ch = null;
        synchronized (channels) {
            if (channels.size() > 0) {
                ch = (Channel) channels.elements().nextElement();
            }
        }
        return ch;

    }

    public Channel getChannel(String name) {
        return (Channel) channels.get(name);
    }

    public String[] getChannelNames() {

        return PubnubUtil.hashtableKeysToArray(channels);
    }

    public String getChannelString() {
        return PubnubUtil.hashTableKeysToDelimitedString(channels, ",");

    }

    public void invokeConnectCallbackOnChannels(Object message) {
        invokeConnectCallbackOnChannels(getChannelNames(), message);
    }

    public void invokeDisconnectCallbackOnChannels(Object message) {
    	invokeDisconnectCallbackOnChannels(getChannelNames(), message);
    }

    public void invokeErrorCallbackOnChannels(Object message) {
        /*
         * Iterate over all the channels and call error callback for channels
         */
        synchronized (channels) {
            Enumeration ch = channels.elements();
            while (ch.hasMoreElements()) {
                Channel _channel = (Channel) ch.nextElement();
                _channel.callback.errorCallback(_channel.name, message);
            }
        }
    }

    public void invokeConnectCallbackOnChannels(String[] channels, Object message) {
        synchronized (channels) {
            for (int i = 0; i < channels.length; i++) {
                Channel _channel = (Channel) this.channels.get(channels[i]);
                if (_channel.connected == false) {
                    _channel.connected = true;
                    if (_channel.subscribed == false) {
                        _channel.callback.connectCallback(_channel.name, 
                        		new JSONArray().put(1).put("Subscribe connected").put(message));
                    } else {
                        _channel.subscribed = true;
                        _channel.callback.reconnectCallback(_channel.name, 
                        		new JSONArray().put(1).put("Subscribe reconnected").put(message));
                    }
                }
            }
        }
    }
    
    public void invokeReconnectCallbackOnChannels(Object message) {
        invokeReconnectCallbackOnChannels(getChannelNames(), message);
    }
    
    public void invokeReconnectCallbackOnChannels(String[] channels, Object message) {
        synchronized (channels) {
            for (int i = 0; i < channels.length; i++) {
                Channel _channel = (Channel) this.channels.get(channels[i]);
                _channel.connected = true;
                _channel.callback.reconnectCallback(_channel.name, 
                		new JSONArray().put(1).put("Subscribe reconnected").put(message));
            }
        }
    }

    public void invokeDisconnectCallbackOnChannels(String[] channels, Object message) {
        synchronized (channels) {
            for (int i = 0; i < channels.length; i++) {
                Channel _channel = (Channel) this.channels.get(channels[i]);
                if (_channel.connected == true) {
                    _channel.connected = false;
                    _channel.callback.disconnectCallback(_channel.name, 
                    		new JSONArray().put(0).put("Subscribe unable to connect").put(message));
                }
            }
        }
    }
}
