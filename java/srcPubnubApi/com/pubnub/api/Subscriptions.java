package com.pubnub.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author PubnubCore
 *
 */
class Subscriptions {
    private Hashtable items;

    JsonObject state;

    public Subscriptions() {
        items    = new Hashtable();
        state    = new JsonObject();
    }

    public void addItem(SubscriptionItem item) {
        items.put(item.name, item);
    }

    public void removeItem(String name) {
        items.remove(name);
    }

    public void removeAllItems() {
        items.clear();
    }

    public SubscriptionItem getFirstItem() {
        SubscriptionItem ch = null;
        synchronized (items) {
            if (items.size() > 0) {
                ch = (SubscriptionItem) items.elements().nextElement();
            }
        }
        return ch;
    }

    public SubscriptionItem getItem(String name) {
        return (SubscriptionItem) items.get(name);
    }

    public String[] getItemNames() {

        return PubnubUtil.hashtableKeysToArray(items);
    }

    public String getItemStringNoPresence() {
        return PubnubUtil.hashTableKeysToDelimitedString(items, ",", "-pnpres");
    }

    public String getItemString() {
        return PubnubUtil.hashTableKeysToDelimitedString(items, ",");
    }

    public void invokeConnectCallbackOnItems(String message) {
        invokeConnectCallbackOnItems(getItemNames(), message);
    }

    public void invokeDisconnectCallbackOnItems(String message) {
        invokeDisconnectCallbackOnItems(getItemNames(), message);
    }

    public void invokeErrorCallbackOnItems(PubnubError error) {
        /*
         * Iterate over all the items and call error callback for items
         */
        synchronized (items) {
            Enumeration itemsElements = items.elements();
            while (itemsElements.hasMoreElements()) {
                SubscriptionItem _item = (SubscriptionItem) itemsElements.nextElement();
                _item.error = true;
                _item.callback.errorCallback(_item.name, error);
            }
        }
    }

    public void invokeConnectCallbackOnItems(String[] items, String message) {
        synchronized (items) {
            for (int i = 0; i < items.length; i++) {
                SubscriptionItem _item = (SubscriptionItem) this.items.get(items[i]);
                if (_item != null) {
                    if (_item.connected == false) {
                        _item.connected = true;
                        if (_item.subscribed == false) {
                            final JsonArray jsonArray = new JsonArray();
                            jsonArray.add(new JsonPrimitive(1));
                            jsonArray.add(new JsonPrimitive("Subscribe connected"));
                            jsonArray.add(new JsonPrimitive(message));
                            _item.callback.connectCallback(_item.name, jsonArray);
                        } else {
                            _item.subscribed = true;
                            final JsonArray jsonArray = new JsonArray();
                            jsonArray.add(new JsonPrimitive(1));
                            jsonArray.add(new JsonPrimitive("Subscribe reconnected"));
                            jsonArray.add(new JsonPrimitive(message));
                            _item.callback.reconnectCallback(_item.name, jsonArray);
                        }
                    }
                }
            }
        }
    }

    public void invokeReconnectCallbackOnItems(String message) {
        invokeReconnectCallbackOnItems(getItemNames(), message);
    }

    public void invokeReconnectCallbackOnItems(String[] items, String message) {
        synchronized (items) {
            for (int i = 0; i < items.length; i++) {
                SubscriptionItem _item = (SubscriptionItem) this.items.get(items[i]);
                if (_item != null) {
                    _item.connected = true;
                    if ( _item.error ) {
                        JsonArray jsonArray = new JsonArray();
                        jsonArray.add(new JsonPrimitive(1));
                        jsonArray.add(new JsonPrimitive("Subscribe reconnected"));
                        jsonArray.add(new JsonPrimitive(message));
                        _item.callback.reconnectCallback(_item.name, jsonArray);
                        _item.error = false;
                    }
                }
            }
        }
    }

    public void invokeDisconnectCallbackOnItems(String[] items, String message) {
        synchronized (items) {
            for (int i = 0; i < items.length; i++) {
                SubscriptionItem _item = (SubscriptionItem) this.items.get(items[i]);
                if (_item != null) {
                    if (_item.connected == true) {
                        _item.connected = false;
                        JsonArray jsonArray = new JsonArray();
                        jsonArray.add(new JsonPrimitive(0));
                        jsonArray.add(new JsonPrimitive("Subscribe unable to connect"));
                        jsonArray.add(new JsonPrimitive(message));
                        _item.callback.disconnectCallback(_item.name,
                          jsonArray);
                    }
                }
            }
        }
    }
}
